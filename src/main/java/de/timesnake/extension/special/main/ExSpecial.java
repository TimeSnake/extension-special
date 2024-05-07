/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.main;

import de.timesnake.extension.special.display.DisplayManager;
import de.timesnake.extension.special.move.MoversManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExSpecial extends JavaPlugin {

  private static ExSpecial plugin;

  private MoversManager moversManager;

  @Override
  public void onEnable() {
    plugin = this;

    this.moversManager = new MoversManager();
    new DisplayManager();
  }

  @Override
  public void onDisable() {
    this.moversManager.onDisable();
  }

  public static ExSpecial getPlugin() {
    return plugin;
  }
}
