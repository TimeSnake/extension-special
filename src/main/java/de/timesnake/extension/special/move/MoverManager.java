/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.GsonFile;
import de.timesnake.library.commands.simple.Arguments;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MoverManager<M extends Mover> implements Listener {

  protected final String name;
  protected final HashMap<ExWorld, Set<M>> moversByWorld = new HashMap<>();

  public MoverManager(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Integer> getIds(ExWorld world) {
    return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).stream()
        .map(Mover::getId).collect(Collectors.toList());
  }

  public int newId(ExWorld world) {
    List<Integer> ids = this.getIds(world);

    int id = 0;

    while (ids.contains(id)) {
      id++;
    }

    return id;
  }

  public abstract void trigger(User user, Trigger type);

  public abstract void handleCommand(Sender sender, User user, Arguments<Argument> args);

  public abstract List<String> handleTabComplete(Arguments<Argument> args);

  public abstract void addMover(Mover mover, ExWorld world);

  public GsonFile getFile(ExWorld world) {
    return MoversManager.getInstance().getMoveFile(world);
  }

  public enum Trigger {
    SNEAK,
    JUMP,
    MOVE
  }

}
