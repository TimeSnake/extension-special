/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.GsonFile;
import de.timesnake.library.commands.simple.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class LocationInteractionManagerBasis<I extends LocationInteraction> implements Listener {

  public static final String FILE_PREFIX = "location_interactions_";

  private final Logger logger;

  protected final String name;
  protected final HashMap<ExWorld, Set<I>> moversByWorld = new HashMap<>();

  public LocationInteractionManagerBasis(String name) {
    this.name = name;
    this.logger = LogManager.getLogger("special.loc_int." + name);
  }

  public String getName() {
    return name;
  }

  public List<Integer> getIds(ExWorld world) {
    return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).stream()
        .map(LocationInteraction::getId).collect(Collectors.toList());
  }

  public int newId(ExWorld world) {
    List<Integer> ids = this.getIds(world);

    int id = 0;

    while (ids.contains(id)) {
      id++;
    }

    return id;
  }

  public int loadLocInteractionsOfWorld(ExWorld world) {
    File interactionsFile = new File(this.getFilePath(world));
    if (interactionsFile.exists()) {
      GsonFile file = this.newGsonFile(world);

      LinkedList<Integer> loadedLocInteractions = new LinkedList<>();
      List<I> locationInteractions = file.read(new TypeToken<List<I>>() {
      }.getType());

      for (I locationInteraction : locationInteractions) {
        locationInteraction.world = world;
        this.addLocInteraction(locationInteraction, world);
        loadedLocInteractions.add(locationInteraction.getId());
      }

      this.logger.info("Loaded {} in world '{}': {}", this.name, world.getName(),
          String.join(",", loadedLocInteractions.stream().map(String::valueOf).toList()));

      return loadedLocInteractions.size();
    }
    return 0;
  }

  public void saveLocInteractionsOfWorld(ExWorld world) {
    Set<I> locInteractions = this.moversByWorld.remove(world);
    if (locInteractions != null && !locInteractions.isEmpty()) {
      this.newGsonFile(world).write(locInteractions);
      this.logger.info("Saved {} in world '{}'", this.name, world.getName());
    }
  }

  private GsonFile newGsonFile(ExWorld world) {
    return new GsonFile(new File(this.getFilePath(world)), this.newGson());
  }

  private Gson newGson() {
    return Server.getDefaultGsonBuilder().create();
  }

  private String getFilePath(ExWorld world) {
    return world.getWorldFolder().getAbsolutePath() + File.separator + FILE_PREFIX + name + ".json";
  }

  public abstract LocInteractionResult<?> trigger(User user, Trigger type);

  public abstract void handleCommand(Sender sender, User user, Arguments<Argument> args);

  public abstract List<String> handleTabComplete(Arguments<Argument> args);

  public abstract void addLocInteraction(I mover, ExWorld world);

  public enum Trigger {
    SNEAK,
    JUMP,
    MOVE
  }

}
