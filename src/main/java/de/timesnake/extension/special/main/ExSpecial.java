/*
 * extension-special.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.extension.special.main;

import de.timesnake.extension.special.display.DisplayManager;
import de.timesnake.extension.special.move.MoversManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExSpecial extends JavaPlugin {

    private static ExSpecial plugin;

    @Override
    public void onEnable() {
        plugin = this;

        new MoversManager();
        new DisplayManager();
    }

    public static ExSpecial getPlugin() {
        return plugin;
    }
}
