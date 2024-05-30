/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.SimpleLocation;

public class JumpPad extends LocationInteraction {

  private final SimpleLocation position;
  private final double speed;
  private final double x;
  private final double y;
  private final double z;

  public JumpPad(JumpPadManager manager, ExWorld world, SimpleLocation position, double speed, double x, double y,
                 double z) {
    super(manager, world, "jump_pad");
    this.position = position;
    this.speed = speed;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public SimpleLocation getPosition() {
    return position;
  }

  public double getSpeed() {
    return speed;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

}