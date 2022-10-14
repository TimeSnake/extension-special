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

import de.timesnake.basic.bukkit.core.chat.CommandManager;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.LinkedList;
import java.util.List;

public class MoveCmd implements CommandListener {

    private Code.Permission perm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission(this.perm)) {
            return;
        }

        User user = sender.getUser();

        if (!args.isLengthHigherEquals(2, true)) {
            return;
        }

        String type = args.getString(0).toLowerCase();

        LinkedList<Argument> argsCopy = args.getAll();
        argsCopy.removeFirst();
        CommandManager.Arguments shortArgs = new CommandManager.Arguments(sender, argsCopy);

        boolean successfully = MoversManager.getInstance().handleCommand(sender, user, type, shortArgs);

        if (!successfully) {
            sender.sendPluginMessage(Component.text("Unknown mover type", ExTextColor.WARNING));
        }

    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("portal", "jump_pad", "elevator");
        }

        String type = args.getString(0);

        LinkedList<Argument> argsCopy = args.getAll();
        argsCopy.removeFirst();
        CommandManager.Arguments shortArgs = new CommandManager.Arguments(argsCopy);

        return MoversManager.getInstance().handleTabComplete(type, shortArgs);
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.perm = plugin.createPermssionCode("mov", "exspecial.movers");
    }
}
