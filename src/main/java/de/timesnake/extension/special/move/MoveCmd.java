/*
 * Copyright (C) 2023 timesnake
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
