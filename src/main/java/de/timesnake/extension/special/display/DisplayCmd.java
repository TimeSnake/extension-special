/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import java.util.List;
import net.kyori.adventure.text.Component;

public class DisplayCmd implements CommandListener {

    private Code perm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd,
            Arguments<Argument> args) {
        if (!sender.isPlayer(true)) {
            return;
        }

        if (!sender.hasPermission(this.perm)) {
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
                sender.sendPluginMessage(
                        Component.text("Created display with id ", ExTextColor.PERSONAL)
                                .append(Component.text(id, ExTextColor.VALUE)));
                break;
            case "remove":
                if (args.isLengthEquals(1, false)) {
                    Integer removedId = DisplayManager.getInstance()
                            .removeDisplay(user.getExLocation(), 1);
                    if (removedId != null) {
                        sender.sendPluginMessage(
                                Component.text("Removed display with id ", ExTextColor.PERSONAL)
                                        .append(Component.text(removedId, ExTextColor.VALUE)));
                    } else {
                        sender.sendPluginMessage(
                                Component.text("No display found", ExTextColor.WARNING));
                    }
                } else if (args.get(1).isInt(true)) {
                    Integer removeId = args.get(1).toInt();
                    boolean removed = DisplayManager.getInstance()
                            .removeDisplay(user.getExWorld(), removeId);
                    if (removed) {
                        sender.sendPluginMessage(
                                Component.text("Removed display with id ", ExTextColor.PERSONAL)
                                        .append(Component.text(removeId, ExTextColor.VALUE)));
                    } else {
                        sender.sendPluginMessage(
                                Component.text("No display found", ExTextColor.WARNING));
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
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd,
            Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("add", "remove");
        }

        if (args.length() == 2) {
            if (args.getString(0).equalsIgnoreCase("add")) {
                return List.of("<text>", "<line1>{\\n<nextLine>}");
            } else if (args.getString(0).equalsIgnoreCase("remove")) {
                return List.of("[id]");
            }
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.perm = plugin.createPermssionCode("exspecial.display");
    }
}
