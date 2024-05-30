/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.CancelableUserEvent;
import org.bukkit.event.HandlerList;

public class UserLocInteractionEvent<I extends LocationInteraction> extends CancelableUserEvent {

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  private static final HandlerList HANDLERS = new HandlerList();

  private final I locationInteraction;


  public UserLocInteractionEvent(User user, boolean isCanceled, I locationInteraction) {
    super(user, isCanceled);
    this.locationInteraction = locationInteraction;
  }

  public I getLocationInteraction() {
    return locationInteraction;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
