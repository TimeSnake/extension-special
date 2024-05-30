/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.SimpleFacingLocation;
import org.bukkit.Color;

public class Portal extends LocationInteraction {

  private final SimpleFacingLocation first;
  private final SimpleFacingLocation second;

  private final Color firstColor;
  private final Color secondColor;

  public Portal(PortalManager manager, ExWorld world, SimpleFacingLocation first, SimpleFacingLocation second,
                Color firstColor, Color secondColor) {
    super(manager, world, "portal");

    this.first = first.middleHorizontalBlock().roundFacing();
    this.second = second.middleHorizontalBlock().roundFacing();
    this.firstColor = firstColor;
    this.secondColor = secondColor;
  }

  public SimpleFacingLocation getFirst() {
    return first;
  }

  public SimpleFacingLocation getSecond() {
    return second;
  }

  public Color getFirstColor() {
    return firstColor;
  }

  public Color getSecondColor() {
    return secondColor;
  }
}
