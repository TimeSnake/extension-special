/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.world.ExPosition;
import de.timesnake.basic.bukkit.util.world.ExWorld;

public class JumpPad extends Mover {

  private final ExPosition position;
  private final double speed;
  private final double x;
  private final double y;
  private final double z;

  public JumpPad(MoverManager<JumpPad> manager, ExWorld world, ExPosition position, double speed, double x, double y,
                 double z) {
    super(manager, world, "jump_pad");

    this.position = position;
    this.speed = speed;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public ExPosition getPosition() {
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