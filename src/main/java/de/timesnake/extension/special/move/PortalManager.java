/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.commands.simple.Arguments;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.*;

public class PortalManager extends MoverManager<Portal> {

  public static final String NAME = "portal";
  public static final double RADIUS = 0.9;

  private final HashMap<UUID, Tuple<ExLocation, Color>> firstPortalByUuid = new HashMap<>();

  public PortalManager() {
    super(NAME);
    this.startPortalEffects();
  }

  public Set<Portal> getPortals(ExWorld world) {
    return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>());
  }

  @Override
  public void addMover(Mover mover, ExWorld world) {
    this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).add(((Portal) mover));
  }

  public Integer addPortal(ExLocation firstLoc, ExLocation secondLoc, Color firstColor, Color secondColor) {
    Portal portal = new Portal(this, firstLoc.getExWorld(), firstLoc.toFacingPosition(),
        secondLoc.toFacingPosition(), firstColor, secondColor);
    this.moversByWorld.computeIfAbsent(portal.getWorld(), (w) -> new HashSet<>()).add(portal);

    return portal.getId();
  }

  public boolean removePortal(ExWorld world, Integer id) {
    Portal portal = this.getPortals(world).stream().filter(p -> p.getId() == id).findFirst()
        .orElse(null);

    for (Portal port : this.getPortals(world)) {
      if (port.getId() == id) {
        portal = port;
        break;
      }
    }

    if (portal == null) {
      return false;
    }

    this.getPortals(world).remove(portal);
    return true;
  }

  public Integer removePortal(ExLocation loc, double range) {
    Portal portal = this.getPortals(loc.getExWorld()).stream()
        .filter(p -> p.getWorld().equals(loc.getExWorld())
                     && p.getFirst().toLocation(loc.getExWorld()).distanceSquared(loc) < range * range).findFirst()
        .orElse(null);

    if (portal == null) {
      return null;
    }

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
                                                 && p.getFirst().toLocation(loc.getExWorld()).distanceSquared(loc) < RADIUS * RADIUS).findFirst().orElse(null);

    if (portal != null) {
      user.teleport(portal.getSecond().toLocation(loc.getExWorld()));
    }

    portal = portals.stream().filter(p -> p.getWorld().equals(loc.getExWorld())
                                          && p.getSecond().toLocation(loc.getExWorld()).distanceSquared(loc) < RADIUS * RADIUS).findFirst().orElse(null);

    if (portal != null) {
      user.teleport(portal.getFirst().toLocation(loc.getExWorld()));
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
          sender.sendTDMessageCommandHelp("Add first portal", "movers portal add first <hexColor>");
        } else {
          sender.sendTDMessageCommandHelp("Add second portal", "movers portal add second <hexColor>");
        }
        return;
      }

      if (isFirst) {
        this.firstPortalByUuid.put(user.getUniqueId(), new Tuple<>(user.getExLocation(),
            args.get(2).toColorFromHex()));
        sender.sendPluginTDMessage("§sSaved first location");
        sender.sendTDMessageCommandHelp("Add second portal", "movers portal add second <hexColor>");
      } else if (this.firstPortalByUuid.containsKey(user.getUniqueId())) {
        Tuple<ExLocation, Color> first = this.firstPortalByUuid.remove(user.getUniqueId());
        ExLocation secondLoc = user.getExLocation();
        Color color = args.get(2).toColorFromHex();

        Integer id = this.addPortal(first.getA(), secondLoc, first.getB(), color);

        sender.sendPluginTDMessage("§sCreated portal with id §v" + id);
      } else {
        sender.sendTDMessageCommandHelp("Create portal", "movers portal add first <hexColor>");
      }

    } else if (action.equalsIgnoreCase("remove")) {
      if (args.isLengthEquals(1, false)) {
        Integer removedId = this.removePortal(user.getExLocation(), 2);
        if (removedId != null) {
          sender.sendPluginTDMessage("§sRemoved portal with id §v" + removedId);
        } else {
          sender.sendPluginTDMessage("§wNo portal found");
        }
      } else if (args.get(1).isInt(true)) {
        Integer removeId = args.get(1).toInt();
        boolean removed = this.removePortal(user.getExWorld(), removeId);
        if (removed) {
          sender.sendPluginTDMessage("§sRemoved portal with id §v" + removeId);
        } else {
          sender.sendPluginTDMessage("§wNo portal with found with id §v" + removeId);
        }
      } else {
        sender.sendTDMessageCommandHelp("Remove portal", "movers portal remove [id]");
      }
    } else {
      sender.sendTDMessageCommandHelp("Create portal", "movers portal add <first/second> <hexColor>");
      sender.sendTDMessageCommandHelp("Remove portal", "movers portal remove [id]");
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
    Server.runTaskTimerAsynchrony(() -> {
      for (Set<Portal> portals : this.moversByWorld.values()) {
        for (Portal portal : portals) {
          this.spawnPortalParticles(portal.getFirst().toLocation(portal.getWorld()), portal.getFirstColor());
          this.spawnPortalParticles(portal.getSecond().toLocation(portal.getWorld()), portal.getSecondColor());
        }
      }
    }, 0, 10, ExSpecial.getPlugin());
  }

  private void spawnPortalParticles(Location location, Color color) {
    Particle.DustOptions dust = new Particle.DustOptions(color, 2);
    location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(),
        8, 0, 1, 0, 1, dust);
  }
}
