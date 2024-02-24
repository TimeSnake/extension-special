/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.entity.EntityManager;
import de.timesnake.basic.bukkit.util.world.entity.HoloDisplay;
import de.timesnake.extension.special.chat.Plugin;
import de.timesnake.extension.special.main.ExSpecial;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DisplayManager {

  public static final String FILE_NAME = "holo_displays";

  public static final String DISPLAYS = "displays";

  public static DisplayManager getInstance() {
    return instance;
  }

  public static String getDisplayPath(int id) {
    return DISPLAYS + "." + id;
  }

  private static DisplayManager instance;

  private final Logger logger = LogManager.getLogger("server.special.displays");

  private final Map<ExWorld, ExFile> displayFilesByWorld = new HashMap<>();

  public DisplayManager() {
    instance = this;

    for (ExWorld world : Server.getWorlds()) {
      File gameFile = new File(
          world.getWorldFolder().getAbsolutePath() + File.separator + FILE_NAME + ".yml");
      if (gameFile.exists()) {
        this.displayFilesByWorld.put(world,
            new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));
      }
    }

    for (Map.Entry<ExWorld, ExFile> entry : this.getDisplayFilesPerWorld().entrySet()) {

      ExWorld world = entry.getKey();
      ExFile file = entry.getValue();

      EntityManager entityManager = Server.getEntityManager();

      LinkedList<Integer> loadedDisplays = new LinkedList<>();

      for (Integer id : file.getPathIntegerList(DISPLAYS)) {
        try {

          entityManager.registerEntity(new Display(file, id));
          loadedDisplays.add(id);
        } catch (WorldNotExistException e) {
          this.logger.warn("Can not load display with id {} in world {}", id, world.getName());
        }
      }

      if (!loadedDisplays.isEmpty()) {
        this.logger.info("Loaded displays: {} in world {}",
            String.join(",", loadedDisplays.stream().map(String::valueOf).toList()), world.getName());
      }
    }

    Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "holodisplay", List.of("holod"),
            new DisplayCmd(), Plugin.SPECIAL);
  }

  public Map<ExWorld, ExFile> getDisplayFilesPerWorld() {
    return this.displayFilesByWorld;
  }

  public int addDisplay(ExLocation loc, List<String> lines) {
    ExFile file = this.displayFilesByWorld.computeIfAbsent(loc.getExWorld(),
        (w) -> new ExFile(loc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

    Display display = new Display(loc, lines, file);

    Server.getEntityManager().registerEntity(display);

    return display.getId();
  }

  public Integer removeDisplay(ExLocation loc, double range) {
    Display display = null;

    for (Display holoDisplay : Server.getEntityManager()
        .getEntitiesByWorld(loc.getExWorld(), Display.class)) {
      if (holoDisplay.getLocation().distance(loc) <= range) {
        display = holoDisplay;
        break;
      }
    }

    if (display == null) {
      return null;
    }

    ExFile file = this.displayFilesByWorld.computeIfAbsent(loc.getExWorld(),
        (w) -> new ExFile(loc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

    display.removeFromFile(file);

    Server.getEntityManager().unregisterEntity(display);

    return display.getId();
  }

  public boolean removeDisplay(ExWorld world, Integer id) {
    Display display = null;

    for (HoloDisplay holoDisplay : Server.getEntityManager()
        .getEntitiesByWorld(world, HoloDisplay.class)) {
      if (holoDisplay instanceof Display && ((Display) holoDisplay).getId() == id) {
        display = (Display) holoDisplay;
        break;
      }
    }

    if (display == null) {
      return false;
    }

    ExFile file = this.displayFilesByWorld.computeIfAbsent(world,
        (w) -> new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));

    display.removeFromFile(file);

    Server.getEntityManager().registerEntity(display);

    return true;
  }
}
