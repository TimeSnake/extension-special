/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.commands.simple.Arguments;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JumpPadManager extends LocationInteractionManagerBasis<JumpPad> {

  public static final String NAME = "jump_pad";
  public static final double RADIUS = 0.7;

  public JumpPadManager() {
    super(NAME);
  }

  @Override
  public void addLocInteraction(JumpPad mover, ExWorld world) {
    this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).add(mover);
  }

  public Integer addJumpPad(ExLocation loc, double vecX, double vecY, double vecZ, double speed) {
    JumpPad jumpPad = new JumpPad(this, loc.getExWorld(), loc.toFacingPosition(), speed, vecX, vecY, vecZ);
    this.moversByWorld.computeIfAbsent(jumpPad.getWorld(), (w) -> new HashSet<>()).add(jumpPad);
    return jumpPad.getId();
  }

  public Integer removeJumpPad(ExLocation loc, double range) {
    JumpPad jumpPad = this.getJumpPads(loc.getExWorld()).stream()
        .filter(p -> p.getWorld().equals(loc.getExWorld())
                     && p.getPosition().toLocation(loc.getExWorld()).distanceSquared(loc) < range * range).findFirst()
        .orElse(null);

    if (jumpPad == null) {
      return null;
    }

    this.getJumpPads(loc.getExWorld()).remove(jumpPad);
    return jumpPad.getId();
  }

  public boolean removeJumpPad(ExWorld world, Integer id) {
    JumpPad jumpPad = this.getJumpPads(world).stream().filter(p -> p.getId() == id).findFirst()
        .orElse(null);

    if (jumpPad == null) {
      return false;
    }

    this.getJumpPads(world).remove(jumpPad);
    return true;
  }

  public Set<JumpPad> getJumpPads(ExWorld world) {
    return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>());
  }


  @Override
  public LocInteractionResult<?> trigger(User user, Trigger type) {
    if (!type.equals(Trigger.MOVE)) {
      return null;
    }

    ExLocation location = user.getExLocation();

    Set<JumpPad> jumpPads = this.moversByWorld.get(user.getExWorld());

    if (jumpPads != null) {
      JumpPad jumpPad =
          this.getJumpPads(location.getExWorld()).stream()
              .filter(p -> p.getWorld().equals(location.getExWorld())
                           && p.getPosition().toLocation(location.getExWorld()).distanceSquared(location) < RADIUS * RADIUS)
              .findFirst()
              .orElse(null);

      if (jumpPad != null) {
        double speed = jumpPad.getSpeed();
        return new LocInteractionResult<>(jumpPad, u -> u.setVelocity(new Vector(jumpPad.getX(), jumpPad.getY(),
            jumpPad.getZ()).normalize().multiply(speed)));
      }
    }
    return null;
  }

  @Override
  public void handleCommand(Sender sender, User user, Arguments<Argument> args) {
    String action = args.getString(0);

    if (action.equalsIgnoreCase("add")) {
      if (args.isLengthEquals(5, false)) {
        if (args.get(1).isDouble(true) && args.get(2).isDouble(true)
            && args.get(3).isDouble(true) && args.get(4).isDouble(true)) {
          double vecX = args.get(1).toDouble();
          double vecY = args.get(2).toDouble();
          double vecZ = args.get(3).toDouble();
          double speed = args.get(4).toDouble();

          Integer id = this.addJumpPad(user.getExLocation(), vecX, vecY, vecZ, speed);

          sender.sendPluginTDMessage("§sAdded jump pad with id §v" + id);
        }
      } else if (args.isLengthEquals(3, true)) {
        if (args.get(1).isDouble(true) && args.get(2).isDouble(true)) {
          double height = args.get(1).toDouble();
          double speed = args.get(2).toDouble();

          ExLocation loc = user.getExLocation();

          Vector vector = loc.getDirection().normalize();
          vector.setY(height);

          Integer id = this.addJumpPad(user.getExLocation(), vector.getX(), vector.getY(),
              vector.getZ(),
              speed);

          sender.sendPluginTDMessage("§sAdded jump pad with id " + id);
        }
      } else {
        sender.sendTDMessageCommandHelp("Create jump pad", "movers jumppad add <dX> <dY> <dZ> <speed>");
        sender.sendTDMessageCommandHelp("Create jump pad with look direction", "movers jumppad add <height> <speed>");
      }

    } else if (action.equalsIgnoreCase("remove")) {
      if (args.isLengthEquals(1, false)) {
        Integer removedId = this.removeJumpPad(user.getExLocation(), 2);
        if (removedId != null) {
          sender.sendPluginTDMessage("§sRemoved jump pad with id §v" + removedId);
        } else {
          sender.sendPluginTDMessage("§wNo jump pad found");
        }
      } else if (args.get(1).isInt(true)) {
        Integer removeId = args.get(1).toInt();
        boolean removed = this.removeJumpPad(user.getExWorld(), removeId);
        if (removed) {
          sender.sendPluginTDMessage("§sRemoved jump pad with id §v" + removeId);
        } else {
          sender.sendPluginTDMessage("§wNo jump pad with found with id §v" + removeId);
        }
      } else {
        sender.sendTDMessageCommandHelp("Remove jump pad", "movers jumppad remove [id]");
      }
    } else {
      sender.sendTDMessageCommandHelp("Create jump pad", "movers jumppad add <dX> <dY> <dZ> <speed>");
      sender.sendTDMessageCommandHelp("Create jump pad with look direction", "movers jumppad add <height> <speed>");
      sender.sendTDMessageCommandHelp("Remove jump pad", "movers jumppad remove [id]");
    }
  }

  @Override
  public List<String> handleTabComplete(Arguments<Argument> args) {
    if (args.length() == 1) {
      return List.of("add", "remove");
    }

    if (args.length() == 2) {
      return List.of("<directionX>", "<height>");
    }
    if (args.length() == 3) {
      return List.of("<directionY>", "<speed>");
    }
    if (args.length() == 4) {
      return List.of("<directionZ>");
    }
    if (args.length() == 5) {
      return List.of("<speed>");
    }

    return List.of();
  }
}
