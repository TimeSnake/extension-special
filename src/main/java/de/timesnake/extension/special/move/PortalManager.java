/*
 * extension-special.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PortalManager extends MoverManager<Portal> {

    public static final String NAME = "portal";

    public static final double RADIUS = 0.9;
    public static final int DELAY = (int) (0.6 * 20);

    private final HashMap<User, Portal> usedPortalsByUser = new HashMap<>();

    private final HashMap<UUID, Tuple<ExLocation, Color>> firstPortalByUuid = new HashMap<>();

    private BukkitTask portalEffectTask;

    public PortalManager() {
        super(NAME);
        this.startPortalEffects();
    }

    public Set<Portal> getPortals(ExWorld world) {
        return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>());
    }

    @Override
    public void addMover(ExFile file, int id, ExWorld world) throws WorldNotExistException {
        Portal portal = new Portal(this, id, world);
        this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).add(portal);
    }

    public Integer addPortal(ExLocation firstLoc, ExLocation secondLoc, Color firstColor, Color secondColor) {
        Portal portal = new Portal(this, firstLoc, secondLoc, firstColor, secondColor);
        this.moversByWorld.computeIfAbsent(portal.getWorld(), (w) -> new HashSet<>()).add(portal);

        return portal.getId();
    }

    public boolean removePortal(ExWorld world, Integer id) {
        Portal portal = this.getPortals(world).stream().filter(p -> p.getId() == id).findFirst().orElse(null);


        for (Portal port : this.getPortals(world)) {
            if (port.getId() == id) {
                portal = port;
                break;
            }
        }

        if (portal == null) {
            return false;
        }

        ExFile file = MoversManager.getInstance().getMoveFile(world);

        portal.removeFromFile(file);

        this.getPortals(world).remove(portal);

        return true;
    }

    public Integer removePortal(ExLocation loc, double range) {
        Portal portal = this.getPortals(loc.getExWorld()).stream().filter(p -> p.getWorld().equals(loc.getExWorld())
                && p.getFirst().distanceSquared(loc) < range * range).findFirst().orElse(null);


        if (portal == null) {
            return null;
        }

        ExFile file = MoversManager.getInstance().getMoveFile(loc.getExWorld());

        portal.removeFromFile(file);

        this.getPortals(loc.getExWorld()).remove(portal);

        return portal.getId();
    }

    @Override
    public void trigger(User user, Trigger type) {
        if (!type.equals(Trigger.SNEAK)) {
            return;
        }

        ExLocation loc = user.getExLocation();

        Set<Portal> portals = this.moversByWorld.get(user.getExWorld());

        if (portals == null) {
            return;
        }

        if (!user.getPlayer().isSneaking()) {
            return;
        }

        Portal portal = portals.stream().filter(p -> p.getWorld().equals(loc.getExWorld())
                && p.getFirst().distanceSquared(loc) < RADIUS * RADIUS).findFirst().orElse(null);

        if (portal != null) {
            user.teleport(portal.getSecond());
            this.usedPortalsByUser.put(user, portal);
            Server.runTaskLaterSynchrony(() -> this.usedPortalsByUser.remove(user), DELAY, ExSpecial.getPlugin());
        }

        portal = portals.stream().filter(p -> p.getWorld().equals(loc.getExWorld())
                && p.getSecond().distanceSquared(loc) < RADIUS * RADIUS).findFirst().orElse(null);

        if (portal != null) {
            user.teleport(portal.getFirst());
            this.usedPortalsByUser.put(user, portal);
            Server.runTaskLaterSynchrony(() -> this.usedPortalsByUser.remove(user), DELAY, ExSpecial.getPlugin());
        }
    }

    @Override
    public void handleCommand(Sender sender, User user, Arguments<Argument> args) {
        String action = args.getString(0);

        if (action.equalsIgnoreCase("add")) {
            if (!args.isLengthHigherEquals(2, true)) {
                return;
            }

            String number = args.getString(1);

            boolean isFirst = !number.equalsIgnoreCase("second");

            if (!args.isLengthEquals(3, true) || !args.get(2).isHexColor(true)) {
                if (isFirst) {
                    sender.sendMessageCommandHelp("Add first portal", "movers portal first <hexColor>");
                } else {
                    sender.sendMessageCommandHelp("Add second portal", "movers portal second <hexColor>");
                }
                return;
            }

            if (isFirst) {
                this.firstPortalByUuid.put(user.getUniqueId(), new Tuple<>(user.getExLocation(),
                        args.get(2).toColorFromHex()));
                sender.sendPluginMessage(Component.text("Saved first location", ExTextColor.PERSONAL));
                sender.sendMessageCommandHelp("Add second portal", "movers portal second <hexColor>");
            } else if (this.firstPortalByUuid.containsKey(user.getUniqueId())) {

                Tuple<ExLocation, Color> first = this.firstPortalByUuid.remove(user.getUniqueId());
                ExLocation secondLoc = user.getExLocation();
                Color color = args.get(2).toColorFromHex();

                Integer id = this.addPortal(first.getA(), secondLoc, first.getB(), color);

                sender.sendPluginMessage(Component.text("Created portal with id ", ExTextColor.PERSONAL)
                        .append(Component.text(id, ExTextColor.VALUE)));
            } else {
                sender.sendMessageCommandHelp("Create portal", "movers portal first <hexColor>");
            }

        } else if (action.equalsIgnoreCase("remove")) {
            if (args.isLengthEquals(1, false)) {
                Integer removedId = this.removePortal(user.getExLocation(), 2);
                if (removedId != null) {
                    sender.sendPluginMessage(Component.text("Removed portal with id ", ExTextColor.PERSONAL)
                            .append(Component.text(removedId, ExTextColor.VALUE)));
                } else {
                    sender.sendPluginMessage(Component.text("No portal found", ExTextColor.WARNING));
                }
            } else if (args.get(1).isInt(true)) {
                Integer removeId = args.get(1).toInt();
                boolean removed = this.removePortal(user.getExWorld(), removeId);
                if (removed) {
                    sender.sendPluginMessage(Component.text("Removed portal with id ", ExTextColor.PERSONAL)
                            .append(Component.text(removeId, ExTextColor.VALUE)));
                } else {
                    sender.sendPluginMessage(Component.text("No portal with found with id ", ExTextColor.WARNING)
                            .append(Component.text(removed, ExTextColor.VALUE)));
                }
            } else {
                sender.sendMessageCommandHelp("Remove portal", "movers portal remove [id]");
            }
        } else {
            sender.sendMessageCommandHelp("Create portal", "movers portal add <first/second> <hexColor>");
            sender.sendMessageCommandHelp("Remove portal", "movers portal remove [id]");
        }
    }

    @Override
    public List<String> handleTabComplete(Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("add", "remove");
        }

        if (args.length() == 2) {
            return List.of("first", "second");
        }

        return List.of();
    }

    public void startPortalEffects() {
        this.portalEffectTask = Server.runTaskTimerAsynchrony(() -> {
            for (Set<Portal> portals : this.moversByWorld.values()) {
                for (Portal portal : portals) {
                    this.spawnPortalParticles(portal.getFirst(), portal.getFirstColor());
                    this.spawnPortalParticles(portal.getSecond(), portal.getSecondColor());
                }
            }

        }, 0, 10, ExSpecial.getPlugin());
    }

    private void spawnPortalParticles(Location location, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 2);
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 8, 0,
                1.5, 0, 1, dust);
    }
}
