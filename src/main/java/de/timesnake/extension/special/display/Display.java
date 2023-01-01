/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
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
