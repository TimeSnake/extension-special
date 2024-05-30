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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElevatorManager extends LocationInteractionManagerBasis<Elevator> {

  public static final double RADIUS = 0.9;
  public static final String NAME = "elevator";

  public ElevatorManager() {
    super(NAME);
  }

  public Elevator addElevator(ExWorld world, double x, double z) {
    Elevator elevator = new Elevator(this, world, x, z);
    this.moversByWorld.computeIfAbsent(world, (w) -> new HashSet<>()).add(elevator);
    return elevator;
  }

  public boolean removeElevator(ExWorld world, Integer id) {
    Elevator elevator = this.getElevators(world).stream().filter(e -> e.getId() == id)
        .findFirst().orElse(null);

    if (elevator == null) {
      return false;
    }

    this.getElevators(world).remove(elevator);

    return true;
  }

  @Override
  public void addLocInteraction(Elevator mover, ExWorld world) {
    this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).add(mover);
  }

  public Integer removeElevator(ExLocation loc, double range) {
    Elevator elevator = this.getElevators(loc.getExWorld()).stream().filter(e ->
        loc.getExWorld().equals(e.getWorld()) && Math.abs(e.getX() - loc.getX()) <= range &&
            Math.abs(e.getZ() - loc.getZ()) <= range).findFirst().orElse(null);

    if (elevator == null) {
      return null;
    }

    this.getElevators(loc.getExWorld()).remove(elevator);
    return elevator.getId();
  }

  public Set<Elevator> getElevators(ExWorld world) {
    return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>());
  }

  @Override
  public LocInteractionResult<?> trigger(User user, Trigger type) {
    ExLocation location = user.getExLocation();

    if (type.equals(Trigger.JUMP) || (type.equals(Trigger.SNEAK) && user.isSneaking())) {
      Set<Elevator> elevators = this.moversByWorld.get(user.getExWorld());

      if (elevators != null) {
        for (Elevator elevator : elevators) {
          if (elevator.getWorld().equals(location.getExWorld())
              && Math.abs(elevator.getX() - location.getX()) < RADIUS * RADIUS
              && Math.abs(elevator.getZ() - location.getZ()) < RADIUS * RADIUS) {

            if (type.equals(Trigger.JUMP)) {
              Integer higher = elevator.higher(location.getBlockY());

              if (higher == null) {
                return null;
              }

              return new LocInteractionResult<>(elevator, u -> u.teleport(location.getX(), higher, location.getZ()));
            } else {
              Integer lower = elevator.lower(location.getBlockY());

              if (lower == null) {
                return null;
              }

              return new LocInteractionResult<>(elevator, u -> u.teleport(location.getX(), lower, location.getZ()));
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public void handleCommand(Sender sender, User user, Arguments<Argument> args) {
    String action = args.getString(0);

    if (action.equalsIgnoreCase("add")) {

      ExLocation location = user.getExLocation();

      ExWorld world = location.getExWorld();
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();

      Elevator elevator = this.getElevators(location.getExWorld()).stream()
          .filter(e -> location.getExWorld().equals(e.getWorld()) && Math.abs(e.getX() - x) <= RADIUS
                       && Math.abs(e.getZ() - z) <= RADIUS).findFirst().orElse(null);

      if (elevator == null) {
        elevator = this.addElevator(world, x, z);
        sender.sendPluginTDMessage("§sCreated elevator §v" + elevator.getId());
      }

      elevator.addLevel((int) y);
      sender.sendPluginTDMessage("§sAdded level §v" + ((int) y) + "§s at elevator §v" + elevator.getId());


    } else if (action.equalsIgnoreCase("remove")) {
      if (args.isLengthEquals(1, false)) {
        Integer removedId = this.removeElevator(user.getExLocation(), 2);
        if (removedId != null) {
          sender.sendPluginTDMessage("§sRemoved elevator with id §v" + removedId);
        } else {
          sender.sendPluginTDMessage("§wNo elevator found");
        }
      } else if (args.get(1).isInt(true)) {
        Integer removeId = args.get(1).toInt();
        boolean removed = this.removeElevator(user.getExWorld(), removeId);
        if (removed) {
          sender.sendPluginTDMessage("§sRemoved elevator with id §v" + removeId);
        } else {
          sender.sendPluginTDMessage("§wNo elevator with found with id §v" + removeId);
        }
      } else {
        sender.sendTDMessageCommandHelp("Remove elevator", "movers elevator remove [id]");
      }
    } else {
      sender.sendTDMessageCommandHelp("Create elevator", "movers elevator add");
      sender.sendTDMessageCommandHelp("Remove elevator", "movers elevator remove [id]");
    }
  }

  @Override
  public List<String> handleTabComplete(Arguments<Argument> args) {
    if (args.length() == 1) {
      return List.of("add", "remove");
    }
    return List.of();
  }
}
