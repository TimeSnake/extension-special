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

package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.HoloDisplay;

import java.util.List;
import java.util.Set;

public class Display extends HoloDisplay {

    public static final String LOCATION = "location";
    public static final String TEXT = "text";

    private final int id;

    public Display(ExLocation location, List<String> text, ExFile file) {
        super(location, text);

        Set<Integer> ids = file.getPathIntegerList(DisplayManager.DISPLAYS);

        int id = 0;

        while (ids.contains(id)) {
            id++;
        }

        this.id = id;

        file.setLocation(DisplayManager.getDisplayPath(id) + "." + LOCATION, this.getLocation(), false).save();
        file.set(DisplayManager.getDisplayPath(id) + "." + TEXT, this.getText()).save();
    }

    public Display(ExFile file, int id) throws WorldNotExistException {
        super(ExLocation.fromLocation(file.getLocation(DisplayManager.getDisplayPath(id) + "." + LOCATION)),
                file.getStringList(DisplayManager.getDisplayPath(id) + "." + TEXT));
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean removeFromFile(ExFile file) {
        return file.remove(DisplayManager.getDisplayPath(this.id));
    }
}
