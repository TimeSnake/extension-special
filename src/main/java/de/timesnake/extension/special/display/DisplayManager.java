/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.display;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldLoadEvent;
import de.timesnake.extension.special.chat.Plugin;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.basic.util.GsonFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DisplayManager implements Listener {

  public static final String FILE_NAME = "holo_displays";

  public static DisplayManager getInstance() {
    return instance;
  }

  private static DisplayManager instance;

  private final Logger logger = LogManager.getLogger("server.special.displays");

  private final Map<ExWorld, List<Display>> displaysByWorld = new HashMap<>();

  public DisplayManager() {
    instance = this;

    for (ExWorld world : Server.getWorlds()) {
      this.loadDisplaysOfWorld(world);
    }

    Server.registerListener(this, ExSpecial.getPlugin());

    Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "holodisplay", List.of("holod"),
            new DisplayCmd(), Plugin.SPECIAL);
  }

  private void loadDisplaysOfWorld(ExWorld world) {
    File displaysFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + FILE_NAME + ".json");
    if (displaysFile.exists()) {
      GsonFile file = this.newGsonFile(world.getWorldFolder().getAbsolutePath());

      List<Display> displays = file.readList(Display.class);
      this.displaysByWorld.put(world, displays);

      for (Display display : displays) {
        display.setWorld(world);
        display.loadEntity();
      }

      this.logger.info("Loaded displays in world '{}': {}", world.getName(),
          String.join(",", displays.stream().map(String::valueOf).toList()));
    }
  }

  private void saveDisplassOfWorld(ExWorld world) {
    List<Display> worldDisplays = this.displaysByWorld.remove(world);
    if (worldDisplays != null && !worldDisplays.isEmpty()) {
      this.newGsonFile(world.getWorldFolder().getAbsolutePath()).write(worldDisplays);
    }
  }

  public int addDisplay(ExLocation loc, List<String> lines) {
    Display display = new Display(this, loc.getExWorld(), loc.toPosition(), lines);
    this.displaysByWorld.computeIfAbsent(loc.getExWorld(), k -> new ArrayList<>()).add(display);

    Server.getEntityManager().registerEntity(display.getPacketEntity());
    return display.getId();
  }

  public List<Integer> getIds(ExWorld world) {
    return this.displaysByWorld.computeIfAbsent(world, w -> List.of()).stream()
        .map(Display::getId).collect(Collectors.toList());
  }

  public int newId(ExWorld world) {
    List<Integer> ids = this.getIds(world);

    int id = 0;

    while (ids.contains(id)) {
      id++;
    }

    return id;
  }

  private GsonFile newGsonFile(String folderPath) {
    return new GsonFile(new File(folderPath + File.separator + FILE_NAME + ".json"), this.newGson());
  }

  private Gson newGson() {
    return new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create();
  }

  public @Nullable Integer removeDisplay(ExLocation loc, double range) {
    for (Display display : this.displaysByWorld.getOrDefault(loc.getExWorld(), List.of())) {
      if (display.getPacketEntity().getLocation().distance(loc) <= range) {
        this.displaysByWorld.get(loc.getExWorld()).remove(display);
        Server.getEntityManager().unregisterEntity(display.getPacketEntity());
        return display.getId();
      }
    }
    return null;
  }

  public boolean removeDisplay(ExWorld world, Integer id) {
    for (Display display : this.displaysByWorld.getOrDefault(world, List.of())) {
      if (display.getId() == id) {
        this.displaysByWorld.get(world).remove(display);
        Server.getEntityManager().unregisterEntity(display.getPacketEntity());
        return true;
      }
    }

    return false;
  }

  @EventHandler
  public void onWorldLoad(ExWorldLoadEvent e) {
    this.loadDisplaysOfWorld(e.getWorld());
  }

  @EventHandler
  public void onWorldUnload(WorldUnloadEvent e) {
    ExWorld world = Server.getWorld(e.getWorld());

    if (world != null) {
      this.saveDisplassOfWorld(world);
      this.logger.info("Saved displays of world '{}'", world.getName());
    }
  }
}
