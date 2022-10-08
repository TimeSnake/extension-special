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
