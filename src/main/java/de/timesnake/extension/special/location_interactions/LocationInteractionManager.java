/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldLoadEvent;
import de.timesnake.basic.bukkit.util.world.ExWorldUnloadEvent;
import de.timesnake.basic.bukkit.util.world.WorldManager;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.commands.simple.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.List;

public class LocationInteractionManager implements Listener {

  private final Logger logger = LogManager.getLogger("special.loc_int.manager");

  private final HashMap<String, LocationInteractionManagerBasis<?>> locInteractionManagerByName = new HashMap<>();

  public LocationInteractionManager() {
    PortalManager portalManager = new PortalManager();
    this.locInteractionManagerByName.put(portalManager.getName(), portalManager);

    JumpPadManager jumpPadManager = new JumpPadManager();
    this.locInteractionManagerByName.put(jumpPadManager.getName(), jumpPadManager);

    ElevatorManager elevatorManager = new ElevatorManager();
    this.locInteractionManagerByName.put(elevatorManager.getName(), elevatorManager);

    for (ExWorld world : Server.getWorlds()) {
      this.loadLocInteractionsOfWorld(world);
    }

    Server.registerListener(this, ExSpecial.getPlugin());

    Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "movers", List.of("mvs", "mover"),
        new MoveCmd(), Plugin.SERVER);
  }

  private void loadLocInteractionsOfWorld(ExWorld world) {
    int number = 0;
    for (LocationInteractionManagerBasis<?> manager : this.locInteractionManagerByName.values()) {
      number += manager.loadLocInteractionsOfWorld(world);
    }
    this.logger.info("Loaded location interactions in world '{}': {}", world.getName(), number);
  }

  private void saveLocInteractionsOfWorld(ExWorld world) {
    for (LocationInteractionManagerBasis<?> manager : this.locInteractionManagerByName.values()) {
      manager.saveLocInteractionsOfWorld(world);
    }
    this.logger.info("Saved location interactions in world '{}'", world.getName());
  }

  public boolean handleCommand(Sender sender, User user, String type, Arguments<Argument> args) {
    LocationInteractionManagerBasis<?> moverManager = this.locInteractionManagerByName.get(type.toLowerCase());

    if (moverManager == null) {
      return false;
    }

    moverManager.handleCommand(sender, user, args);
    return true;
  }

  public List<String> handleTabComplete(String type, Arguments<Argument> args) {
    LocationInteractionManagerBasis<?> moverManager = this.locInteractionManagerByName.get(type.toLowerCase());

    if (moverManager == null) {
      return List.of();
    }

    return moverManager.handleTabComplete(args);
  }

  @EventHandler
  public void onWorldLoad(ExWorldLoadEvent e) {
    this.loadLocInteractionsOfWorld(e.getWorld());
  }

  @EventHandler
  public void onWorldUnload(ExWorldUnloadEvent e) {
    if (e.getActionType().equals(WorldManager.WorldUnloadActionType.UNLOAD)
        || e.getActionType().equals(WorldManager.WorldUnloadActionType.RELOAD))
      this.saveLocInteractionsOfWorld(e.getWorld());
  }

  @EventHandler
  public void onUserMove(UserMoveEvent e) {

    User user = e.getUser();

    for (LocationInteractionManagerBasis<?> manager : this.locInteractionManagerByName.values()) {
      LocInteractionResult<?> result = manager.trigger(user, LocationInteractionManagerBasis.Trigger.MOVE);
      if (result == null) {
        continue;
      }

      UserLocInteractionEvent<?> event = new UserLocInteractionEvent<>(user, false, result.getMover());
      if (!event.isCancelled()) {
        result.getAction().accept(user);
      }
    }
  }

  @EventHandler
  public void onPlayerSneak(PlayerToggleSneakEvent e) {
    User user = Server.getUser(e.getPlayer());

    for (LocationInteractionManagerBasis<?> manager : this.locInteractionManagerByName.values()) {
      LocInteractionResult<?> result = manager.trigger(user, LocationInteractionManagerBasis.Trigger.SNEAK);
      if (result == null) {
        continue;
      }

      UserLocInteractionEvent<?> event = new UserLocInteractionEvent<>(user, false, result.getMover());
      if (!event.isCancelled()) {
        result.getAction().accept(user);
      }
    }
  }

  @EventHandler
  public void onPlayerJump(PlayerJumpEvent e) {
    User user = Server.getUser(e.getPlayer());

    for (LocationInteractionManagerBasis<?> manager : this.locInteractionManagerByName.values()) {
      LocInteractionResult<?> result = manager.trigger(user, LocationInteractionManagerBasis.Trigger.JUMP);
      if (result == null) {
        continue;
      }

      UserLocInteractionEvent<?> event = new UserLocInteractionEvent<>(user, false, result.getMover());
      if (!event.isCancelled()) {
        result.getAction().accept(user);
      }
    }
  }

}
