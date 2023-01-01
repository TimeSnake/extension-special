/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExWorld;

import java.util.List;

public abstract class Mover {

    protected final MoverManager<?> manager;

    protected final int id;
    protected final ExWorld world;

    public Mover(MoverManager<?> manager, ExWorld world) {
        this.manager = manager;
        this.world = world;

        List<Integer> ids = this.manager.getIds(this.world);

        int id = 0;

        while (ids.contains(id)) {
            id++;
        }

        this.id = id;
    }

    public Mover(MoverManager<?> manager, int id, ExWorld world) {
        this.manager = manager;
        this.id = id;
        this.world = world;
    }

    public int getId() {
        return id;
    }

    public ExFile getFile() {
        return this.manager.getFile(this.world);
    }

    public ExWorld getWorld() {
        return this.world;
    }

    public abstract boolean removeFromFile(ExFile file);

}
