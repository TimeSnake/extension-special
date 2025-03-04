/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.library.chat.Code;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import net.kyori.adventure.text.Component;

import java.util.List;

public class DisplayCmd implements CommandListener {

  private final Code perm = Plugin.SERVER.createPermssionCode("exspecial.display");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
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
      case "add" -> {
        if (!args.isLengthHigherEquals(2, true)) {
          return;
        }
        String text = args.toMessage(1);
        text = text.replaceAll("\\\\&", "§");
        List<String> lines = List.of(text.split("\\\\n"));
        int id = DisplayManager.getInstance().addDisplay(user.getExLocation(), lines);
        sender.sendPluginMessage(Component.text("Created display with id ", ExTextColor.PERSONAL)
                .append(Component.text(id, ExTextColor.VALUE)));
      }
      case "remove" -> {
        if (args.isLengthEquals(1, false)) {
          Integer removedId = DisplayManager.getInstance()
              .removeDisplay(user.getExLocation(), 1);
          if (removedId != null) {
            sender.sendPluginMessage(Component.text("Removed display with id ", ExTextColor.PERSONAL)
                    .append(Component.text(removedId, ExTextColor.VALUE)));
          } else {
            sender.sendPluginMessage(Component.text("No display found", ExTextColor.WARNING));
          }
        } else if (args.get(1).isInt(true)) {
          Integer removeId = args.get(1).toInt();
          boolean removed = DisplayManager.getInstance()
              .removeDisplay(user.getExWorld(), removeId);
          if (removed) {
            sender.sendPluginMessage(Component.text("Removed display with id ", ExTextColor.PERSONAL)
                    .append(Component.text(removeId, ExTextColor.VALUE)));
          } else {
            sender.sendPluginMessage(Component.text("No display found", ExTextColor.WARNING));
          }
        } else {
          sender.sendTDMessageCommandHelp("Remove display", "holod remove [id]");
        }
      }
      default -> sender.sendTDMessageCommandHelp("Create display", "holod add <text>");
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm)
        .addArgument(new Completion("add")
            .addArgument(new Completion("<text>", "<line1>{\\n<nextLine>}").allowAny()))
        .addArgument(new Completion("remove")
            .addArgument(new Completion("[id]").allowAny()));
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
