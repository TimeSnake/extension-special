package de.timesnake.extension.special.main;

import de.timesnake.extension.special.display.DisplayManager;
import de.timesnake.extension.special.move.MoveManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExSpecial extends JavaPlugin {

    private static ExSpecial plugin;

    @Override
    public void onEnable() {
        plugin = this;

        new MoveManager();
        new DisplayManager();
    }

    public static ExSpecial getPlugin() {
        return plugin;
    }
}
