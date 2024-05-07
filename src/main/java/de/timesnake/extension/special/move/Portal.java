/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.world.ExFacingPosition;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import org.bukkit.Color;

public class Portal extends Mover {

  private final ExFacingPosition first;
  private final ExFacingPosition second;

  private final Color firstColor;
  private final Color secondColor;

  public Portal(MoverManager<Portal> manager, ExWorld world, ExFacingPosition first, ExFacingPosition second,
                Color firstColor, Color secondColor) {
    super(manager, world, "portal");

    this.first = first.middleHorizontalBlock().roundFacing();
    this.second = second.middleHorizontalBlock().roundFacing();
    this.firstColor = firstColor;
    this.secondColor = secondColor;
  }

  public ExFacingPosition getFirst() {
    return first;
  }

  public ExFacingPosition getSecond() {
    return second;
  }

  public Color getFirstColor() {
    return firstColor;
  }

  public Color getSecondColor() {
    return secondColor;
  }
}
