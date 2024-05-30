/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.main;

import de.timesnake.extension.special.display.DisplayManager;
import de.timesnake.extension.special.location_interactions.LocationInteractionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExSpecial extends JavaPlugin {

  private static ExSpecial plugin;

  private LocationInteractionManager locationInteractionManager;

  @Override
  public void onEnable() {
    plugin = this;

    this.locationInteractionManager = new LocationInteractionManager();
    new DisplayManager();
  }

  public LocationInteractionManager getLocationInteractionManager() {
    return locationInteractionManager;
  }

  public static ExSpecial getPlugin() {
    return plugin;
  }
}
