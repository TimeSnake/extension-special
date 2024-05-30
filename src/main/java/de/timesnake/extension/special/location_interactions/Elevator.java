/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.world.ExWorld;

import java.util.TreeSet;

public class Elevator extends LocationInteraction {

  private final double x;
  private final double z;
  private transient final TreeSet<Integer> heights = new TreeSet<>();

  public Elevator(ElevatorManager manager, ExWorld world, double x, double z) {
    super(manager, world, "elevators");
    this.x = x;
    this.z = z;
  }

  public boolean addLevel(int height) {
    return this.heights.add(height);
  }

  public boolean removeLevel(int height) {
    return this.heights.remove(height);
  }

  public ExWorld getWorld() {
    return world;
  }

  public double getX() {
    return x;
  }

  public double getZ() {
    return z;
  }

  public Integer lower(int height) {
    return this.heights.lower(height);
  }

  public Integer higher(int height) {
    return this.heights.higher(height);
  }

}
