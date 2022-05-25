package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.util.List;

public class MoveCmd implements CommandListener {


    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission("exspecial.movers", 2415)) {
            return;
        }

        User user = sender.getUser();

        if (!args.isLengthHigherEquals(2, true)) {
            return;
        }

        String type = args.getString(0).toLowerCase();

        boolean successfully = MoversManager.getInstance().handleCommand(sender, user, type, args.removeLowerEquals(0));

        if (!successfully) {
            sender.sendPluginMessage(ChatColor.WARNING + "Unknown mover type");
        }

    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("portal", "jump_pad", "elevator");
        }

        String type = args.getString(0);

        return MoversManager.getInstance().handleTabComplete(type, args.removeLowerEquals(0));
    }
}
