/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.world.ExWorld;

import java.util.Objects;

public abstract class Mover {

  protected final String type;
  protected final int id;

  protected transient ExWorld world;

  public Mover(MoverManager<?> manager, ExWorld world, String type) {
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
    Mover mover = (Mover) o;
    return id == mover.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
