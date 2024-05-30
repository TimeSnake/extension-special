/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.world.ExWorld;

import java.util.Objects;

public abstract class LocationInteraction {

  protected final String type;
  protected final int id;

  protected transient ExWorld world;

  public LocationInteraction(LocationInteractionManagerBasis<?> manager, ExWorld world, String type) {
    this.type = type;
    this.id = manager.newId(world);
    this.world = world;
  }

  public int getId() {
    return id;
  }

  public ExWorld getWorld() {
    return this.world;
  }

  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocationInteraction locationInteraction = (LocationInteraction) o;
    return id == locationInteraction.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
