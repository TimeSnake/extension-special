package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.extension.special.chat.Plugin;
import de.timesnake.extension.special.main.ExSpecial;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class MoveManager implements Listener {

    public static final String FILE_NAME = "movers";

    public static final String MOVERS = "movers";
    public static final String TYPE = "type";

    // portal
    public static final double PORTAL_RADIUS = 0.9;
    public static final int PORTAL_DELAY = (int) (0.6 * 20);

    // jump pad
    public static final double JUMP_PAD_RADIUS = 0.7;

    public enum MoveType {
        PORTAL, JUMP_PAD
    }

    private static MoveManager instance;

    public static MoveManager getInstance() {
        return instance;
    }

    private final Map<ExWorld, ExFile> moveFilesByWorld = new HashMap<>();

    private final HashMap<ExWorld, Set<Portal>> portalsByWorld = new HashMap<>();
    private final HashMap<ExWorld, Set<JumpPad>> jumpPadsByWorld = new HashMap<>();

    private BukkitTask portalEffectTask;
    private final HashMap<User, Portal> usedPortalsByUser = new HashMap<>();

    public MoveManager() {
        instance = this;

        for (ExWorld world : Server.getWorlds()) {
            File gameFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + FILE_NAME + ".yml");
            if (gameFile.exists()) {
                this.moveFilesByWorld.put(world, new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));
            }
        }

        for (Map.Entry<ExWorld, ExFile> entry : this.getMoveFilesByWorld().entrySet()) {

            ExWorld world = entry.getKey();
            ExFile file = entry.getValue();

            Set<Portal> portals = this.portalsByWorld.computeIfAbsent(world, (w) -> new HashSet<>());
            Set<JumpPad> jumpPads = this.jumpPadsByWorld.computeIfAbsent(world, (w) -> new HashSet<>());

            for (Integer id : file.getPathIntegerList(MOVERS)) {
                MoveType type;
                try {
                    type = MoveType.valueOf(file.getString(getMoverPath(id) + "." + TYPE).toUpperCase());
                } catch (IllegalArgumentException e) {
                    Server.printWarning(Plugin.SPECIAL, "Can not load type of mover " + id);
                    continue;
                }

                if (type == null) {
                    continue;
                }

                switch (type) {
                    case PORTAL:
                        try {
                            portals.add(new Portal(file, id));
                            Server.printText(Plugin.SPECIAL, "Loaded portal " + id);
                        } catch (WorldNotExistException e) {
                            Server.printWarning(Plugin.SPECIAL, "Can not load portal with id " + id + " in world " + world.getName());
                        }
                        break;
                    case JUMP_PAD:
                        try {
                            jumpPads.add(new JumpPad(file, id));
                            Server.printText(Plugin.SPECIAL, "Loaded jump_pad " + id);
                        } catch (WorldNotExistException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }

        Server.registerListener(this, ExSpecial.getPlugin());

        this.startPortalEffects();

        Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "movers", List.of("mvs", "mover"), new MoveCmd(), Plugin.SPECIAL);
    }

    public Map<ExWorld, ExFile> getMoveFilesByWorld() {
        return moveFilesByWorld;
    }

    public Integer addJumpPad(ExLocation loc, double vecX, double vecY, double vecZ, double speed) {
        ExFile file = this.moveFilesByWorld.computeIfAbsent(loc.getExWorld(), (w) -> new ExFile(loc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

        JumpPad jumpPad = new JumpPad(loc, speed, vecX, vecY, vecZ, file);

        this.jumpPadsByWorld.computeIfAbsent(loc.getExWorld(), (w) -> new HashSet<>()).add(jumpPad);

        return jumpPad.getId();
    }

    public Integer removeJumpPad(ExLocation loc, double range) {
        JumpPad jumpPad = null;

        for (JumpPad pad : this.getJumpPads(loc.getExWorld())) {
            if (pad.getLocation().distance(loc) <= range) {
                jumpPad = pad;
                break;
            }
        }

        if (jumpPad == null) {
            return null;
        }

        ExFile file = this.moveFilesByWorld.computeIfAbsent(loc.getExWorld(), (w) -> new ExFile(loc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

        jumpPad.removeFromFile(file);

        this.getJumpPads(loc.getExWorld()).remove(jumpPad);

        return jumpPad.getId();
    }

    public boolean removeJumpPad(ExWorld world, Integer id) {
        JumpPad jumpPad = null;

        for (JumpPad pad : this.getJumpPads(world)) {
            if (pad.getId() == id) {
                jumpPad = pad;
                break;
            }
        }

        if (jumpPad == null) {
            return false;
        }

        ExFile file = this.moveFilesByWorld.computeIfAbsent(world, (w) -> new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));

        jumpPad.removeFromFile(file);

        this.getJumpPads(world).remove(jumpPad);

        return true;
    }

    public Set<JumpPad> getJumpPads(ExWorld world) {
        Set<JumpPad> jumpPads = this.jumpPadsByWorld.get(world);
        return jumpPads != null ? jumpPads : new HashSet<>();
    }

    public Integer addPortal(ExLocation firstLoc, ExLocation secondLoc, Color firstColor, Color secondColor) {
        ExFile file = this.moveFilesByWorld.computeIfAbsent(firstLoc.getExWorld(), (w) -> new ExFile(firstLoc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

        Portal portal = new Portal(firstLoc, secondLoc, firstColor, secondColor, file);

        this.portalsByWorld.computeIfAbsent(firstLoc.getExWorld(), (w) -> new HashSet<>()).add(portal);

        return portal.getId();
    }

    public Integer removePortal(ExLocation loc, double range) {
        Portal portal = null;

        for (Portal port : this.getPortals(loc.getExWorld())) {
            if (port.getFirst().distance(loc) <= range || port.getSecond().distance(loc) <= range) {
                portal = port;
                break;
            }
        }

        if (portal == null) {
            return null;
        }

        ExFile file = this.moveFilesByWorld.computeIfAbsent(loc.getExWorld(), (w) -> new ExFile(loc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

        portal.removeFromFile(file);

        this.getPortals(loc.getExWorld()).remove(portal);

        return portal.getId();
    }

    public boolean removePortal(ExWorld world, Integer id) {
        Portal portal = null;

        for (Portal port : this.getPortals(world)) {
            if (port.getId() == id) {
                portal = port;
                break;
            }
        }

        if (portal == null) {
            return false;
        }

        ExFile file = this.moveFilesByWorld.computeIfAbsent(world, (w) -> new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));

        portal.removeFromFile(file);

        this.getPortals(world).remove(portal);

        return true;
    }

    public Set<Portal> getPortals(ExWorld world) {
        Set<Portal> portals = this.portalsByWorld.get(world);
        return portals != null ? portals : new HashSet<>();
    }


    @EventHandler
    public void onUserMove(UserMoveEvent e) {

        User user = e.getUser();

        Set<JumpPad> jumpPads = this.jumpPadsByWorld.get(user.getExWorld());

        if (jumpPads != null) {
            for (JumpPad jumpPad : jumpPads) {
                if (jumpPad.getLocation().getWorld().equals(e.getTo().getWorld()) && jumpPad.getLocation().distanceSquared(e.getFrom()) < JUMP_PAD_RADIUS * JUMP_PAD_RADIUS) {
                    double speed = jumpPad.getSpeed();
                    user.setVelocity(new Vector(jumpPad.getX(), jumpPad.getY(), jumpPad.getZ()).normalize().multiply(speed));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        User user = Server.getUser(e.getPlayer());
        Location loc = user.getLocation();

        Set<Portal> portals = this.portalsByWorld.get(user.getExWorld());

        if (portals != null) {
            if (!user.getPlayer().isSneaking()) {
                return;
            }

            for (Portal portal : portals) {
                Portal usedPortal = this.usedPortalsByUser.get(user);

                if (usedPortal == null || !usedPortal.equals(portal)) {
                    if (portal.getFirst().getWorld().equals(loc.getWorld()) && portal.getFirst().distanceSquared(loc) < PORTAL_RADIUS * PORTAL_RADIUS) {
                        user.teleport(portal.getSecond());
                        Server.runTaskLaterSynchrony(() -> this.usedPortalsByUser.remove(user), PORTAL_DELAY, ExSpecial.getPlugin());
                        this.usedPortalsByUser.put(user, portal);
                        break;
                    }

                    if (portal.getSecond().getWorld().equals(loc.getWorld()) && portal.getSecond().distanceSquared(loc) < PORTAL_RADIUS * PORTAL_RADIUS) {
                        user.teleport(portal.getFirst());
                        this.usedPortalsByUser.put(user, portal);
                        Server.runTaskLaterSynchrony(() -> this.usedPortalsByUser.remove(user), PORTAL_DELAY, ExSpecial.getPlugin());
                        break;
                    }
                }
            }
        }
    }

    public static String getMoverPath(int id) {
        return MOVERS + "." + id;
    }

    public void startPortalEffects() {
        this.portalEffectTask = Server.runTaskTimerAsynchrony(() -> {
            for (Set<Portal> portals : this.portalsByWorld.values()) {
                for (Portal portal : portals) {
                    this.spawnPortalParticles(portal.getFirst(), portal.getFirstColor());
                    this.spawnPortalParticles(portal.getSecond(), portal.getSecondColor());
                }
            }

        }, 0, 10, ExSpecial.getPlugin());
    }

    private void spawnPortalParticles(Location location, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 2);
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 8, 0, 1.5, 0, 1, dust);
    }
}
