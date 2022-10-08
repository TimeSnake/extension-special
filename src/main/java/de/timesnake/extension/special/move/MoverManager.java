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

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.extension.util.cmd.Arguments;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MoverManager<M extends Mover> implements Listener {

    protected final String name;
    protected final HashMap<ExWorld, Set<M>> moversByWorld = new HashMap<>();

    public MoverManager(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getIds(ExWorld world) {
        return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).stream().map(Mover::getId).collect(Collectors.toList());
    }

    public abstract void trigger(User user, Trigger type);

    public abstract void handleCommand(Sender sender, User user, Arguments<Argument> args);

    public abstract List<String> handleTabComplete(Arguments<Argument> args);

    public abstract void addMover(ExFile file, int id, ExWorld world) throws WorldNotExistException;

    public ExFile getFile(ExWorld world) {
        return MoversManager.getInstance().getMoveFile(world);
    }

    public enum Trigger {
        SNEAK,
        JUMP,
        MOVE
    }

}
