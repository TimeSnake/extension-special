package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.library.basic.util.cmd.Arguments;
import de.timesnake.library.basic.util.cmd.ExCommand;

import java.util.List;

public class DisplayCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission("exspecial.display", 2416)) {
            return;
        }

        User user = sender.getUser();

        if (!args.isLengthHigherEquals(1, true)) {
            return;
        }

        String action = args.getString(0).toLowerCase();

        switch (action) {
            case "add":
                if (!args.isLengthHigherEquals(2, true)) {
                    return;
                }

                String text = args.toMessage(1);
                text = text.replaceAll("\\\\&", "§");
                List<String> lines = List.of(text.split("\\\\n"));

                int id = DisplayManager.getInstance().addDisplay(user.getExLocation(), lines);
                sender.sendPluginMessage(ChatColor.PERSONAL + "Created display with id " + ChatColor.VALUE + id);
                break;
            case "remove":
                if (args.isLengthEquals(1, false)) {
                    Integer removedId = DisplayManager.getInstance().removeDisplay(user.getExLocation(), 1);
                    if (removedId != null) {
                        sender.sendPluginMessage(ChatColor.PERSONAL + "Removed display with id " + ChatColor.VALUE + removedId);
                    } else {
                        sender.sendPluginMessage(ChatColor.WARNING + "No display found");
                    }
                } else if (args.get(1).isInt(true)) {
                    Integer removeId = args.get(1).toInt();
                    boolean removed = DisplayManager.getInstance().removeDisplay(user.getExWorld(), removeId);
                    if (removed) {
                        sender.sendPluginMessage(ChatColor.PERSONAL + "Removed display with id " + ChatColor.VALUE + removeId);
                    } else {
                        sender.sendPluginMessage(ChatColor.WARNING + "No display found");
                    }
                } else {
                    sender.sendMessageCommandHelp("Remove display", "holod remove [id]");
                }
                break;
            default:
                sender.sendMessageCommandHelp("Create display", "holod add <text>");
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 0) {
            return List.of("add", "remove");
        }
        if (args.length() == 1) {
            return List.of("<line1>{\\n<nextLine>}");
        }
        return List.of();
    }
}
