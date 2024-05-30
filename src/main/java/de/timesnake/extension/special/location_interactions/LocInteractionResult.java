/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.user.User;

import java.util.function.Consumer;

public class LocInteractionResult<M extends LocationInteraction> {

  private final M mover;
  private final Consumer<User> action;

  public LocInteractionResult(M mover, Consumer<User> action) {
    this.mover = mover;
    this.action = action;
  }

  public M getMover() {
    return mover;
  }

  public Consumer<User> getAction() {
    return action;
  }
}
