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

import java.util.List;

public class DisplayCmd implements CommandListener {

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
                sender.sendPluginMessage(Component.text("Created display with id ", ExTextColor.PERSONAL)
                        .append(Component.text(id, ExTextColor.VALUE)));
                break;
            case "remove":
                if (args.isLengthEquals(1, false)) {
                    Integer removedId = DisplayManager.getInstance().removeDisplay(user.getExLocation(), 1);
                    if (removedId != null) {
                        sender.sendPluginMessage(Component.text("Removed display with id ", ExTextColor.PERSONAL)
                                .append(Component.text(removedId, ExTextColor.VALUE)));
                    } else {
                        sender.sendPluginMessage(Component.text("No display found", ExTextColor.WARNING));
                    }
                } else if (args.get(1).isInt(true)) {
                    Integer removeId = args.get(1).toInt();
                    boolean removed = DisplayManager.getInstance().removeDisplay(user.getExWorld(), removeId);
                    if (removed) {
                        sender.sendPluginMessage(Component.text("Removed display with id ", ExTextColor.PERSONAL)
                                .append(Component.text(removeId, ExTextColor.VALUE)));
                    } else {
                        sender.sendPluginMessage(Component.text("No display found", ExTextColor.WARNING));
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
        if (args.length() == 1) {
            return List.of("add", "remove");
        }

        if (args.length() == 2)
            if (args.getString(0).equalsIgnoreCase("add")) {
                return List.of("<text>", "<line1>{\\n<nextLine>}");
            } else if (args.getString(0).equalsIgnoreCase("remove")) {
                return List.of("[id]");
            }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.perm = plugin.createPermssionCode("dsp", "exspecial.display");
    }
}
