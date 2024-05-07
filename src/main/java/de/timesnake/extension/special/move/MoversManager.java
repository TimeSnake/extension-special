/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldLoadEvent;
import de.timesnake.extension.special.chat.Plugin;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.basic.util.GsonFile;
import de.timesnake.library.commands.simple.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.io.File;
import java.util.*;

public class MoversManager implements Listener {

  public static final String FILE_NAME = "movers";

  public static MoversManager getInstance() {
    return instance;
  }

  private static MoversManager instance;

  private final Logger logger = LogManager.getLogger("special.movers");

  private final HashMap<String, MoverManager<?>> moverManagersByName = new HashMap<>();

  private final MoverDeserializer deserializer;

  public MoversManager() {
    instance = this;

    this.deserializer = new MoverDeserializer("type");
    this.deserializer.setGson(this.newGson());

    PortalManager portalManager = new PortalManager();
    this.moverManagersByName.put(portalManager.getName(), portalManager);
    this.deserializer.registerBarnType(portalManager.getName(), Portal.class);

    JumpPadManager jumpPadManager = new JumpPadManager();
    this.moverManagersByName.put(jumpPadManager.getName(), jumpPadManager);
    this.deserializer.registerBarnType(jumpPadManager.getName(), JumpPad.class);

    ElevatorManager elevatorManager = new ElevatorManager();
    this.moverManagersByName.put(elevatorManager.getName(), elevatorManager);
    this.deserializer.registerBarnType(elevatorManager.getName(), Elevator.class);

    for (ExWorld world : Server.getWorlds()) {
      this.loadMoversOfWorld(world);
    }

    Server.registerListener(this, ExSpecial.getPlugin());

    Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "movers", List.of("mvs", "mover"),
        new MoveCmd(), Plugin.SPECIAL);
  }

  private void loadMoversOfWorld(ExWorld world) {
    File moversFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + FILE_NAME + ".json");
    if (moversFile.exists()) {
      GsonFile file = this.newGsonFile(world.getWorldFolder().getAbsolutePath());

      LinkedList<Integer> loadedMovers = new LinkedList<>();

      List<? extends Mover> movers = file.read(new TypeToken<List<Mover>>() {
      }.getType());

      for (Mover mover : movers) {
        MoverManager<?> moverManager = this.moverManagersByName.get(mover.getType());

        if (moverManager == null) {
          this.logger.warn("Invalid mover type '{}' of mover '{}' in world '{}'", mover.getType(),
              mover.getId(), world.getName());
          continue;
        }

        mover.world = world;
        moverManager.addMover(mover, world);
        loadedMovers.add(mover.getId());
      }

      this.logger.info("Loaded movers in world '{}': {}", world.getName(),
          String.join(",", loadedMovers.stream().map(String::valueOf).toList()));
    }
  }

  public void onDisable() {
    for (ExWorld world : Server.getWorlds()) {
      this.saveMoversOfWorld(world);
    }
  }

  private void saveMoversOfWorld(ExWorld world) {
    List<Mover> movers = new ArrayList<>();
    for (MoverManager<?> moverManager : this.moverManagersByName.values()) {
      Set<? extends Mover> worldMovers = moverManager.moversByWorld.remove(world);
      if (worldMovers != null) {
        movers.addAll(worldMovers);
      }
    }

    if (!movers.isEmpty()) {
      this.newGsonFile(world.getWorldFolder().getAbsolutePath()).write(movers);
    }
  }

  public boolean handleCommand(Sender sender, User user, String type, Arguments<Argument> args) {
    MoverManager<?> moverManager = this.moverManagersByName.get(type.toLowerCase());

    if (moverManager == null) {
      return false;
    }

    moverManager.handleCommand(sender, user, args);
    return true;
  }

  public List<String> handleTabComplete(String type, Arguments<Argument> args) {
    MoverManager<?> moverManager = this.moverManagersByName.get(type.toLowerCase());

    if (moverManager == null) {
      return List.of();
    }

    return moverManager.handleTabComplete(args);
  }

  private GsonFile newGsonFile(String folderPath) {
    return new GsonFile(new File(folderPath + File.separator + FILE_NAME + ".json"), this.newGson());
  }

  private Gson newGson() {
    return new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .registerTypeAdapter(Mover.class, this.deserializer)
        .create();
  }

  public GsonFile getMoveFile(ExWorld world) {
    return this.newGsonFile(world.getWorldFolder().getAbsolutePath());
  }

  @EventHandler
  public void onWorldLoad(ExWorldLoadEvent e) {
    this.loadMoversOfWorld(e.getWorld());
  }

  @EventHandler
  public void onWorldUnload(WorldUnloadEvent e) {
    ExWorld world = Server.getWorld(e.getWorld());

    if (world != null) {
      this.saveMoversOfWorld(world);
      this.logger.info("Saved movers of world '{}'", world.getName());
    }
  }

  @EventHandler
  public void onUserMove(UserMoveEvent e) {

    User user = e.getUser();

    for (MoverManager<?> moverManager : this.moverManagersByName.values()) {
      moverManager.trigger(user, MoverManager.Trigger.MOVE);
    }
  }

  @EventHandler
  public void onPlayerSneak(PlayerToggleSneakEvent e) {
    User user = Server.getUser(e.getPlayer());

    for (MoverManager<?> moverManager : this.moverManagersByName.values()) {
      moverManager.trigger(user, MoverManager.Trigger.SNEAK);
    }
  }

  @EventHandler
  public void onPlayerJump(PlayerJumpEvent e) {
    User user = Server.getUser(e.getPlayer());

    for (MoverManager<?> moverManager : this.moverManagersByName.values()) {
      moverManager.trigger(user, MoverManager.Trigger.JUMP);
    }
  }

}
