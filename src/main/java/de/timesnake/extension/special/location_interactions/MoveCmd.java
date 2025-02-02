/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.location_interactions;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.chat.Code;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import net.kyori.adventure.text.Component;

import java.util.LinkedList;

public class MoveCmd implements CommandListener {

  private final Code perm = Plugin.SERVER.createPermssionCode("exspecial.movers");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
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
    Arguments<Argument> shortArgs = new Arguments<>(sender, argsCopy) {
      @Override
      public Argument createArgument(de.timesnake.library.commands.Sender sender, String arg) {
        return new Argument(((Sender) sender), arg);
      }
    };

    boolean successfully = ExSpecial.getPlugin().getLocationInteractionManager().handleCommand(sender, user, type,
        shortArgs);

    if (!successfully) {
      sender.sendPluginMessage(Component.text("Unknown mover type", ExTextColor.WARNING));
    }

  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm)
        .addArgument(new Completion("portal", "jump_pad", "elevator")
            // TODO completion: MoversManager.getInstance().handleTabComplete(type, shortArgs);

        );
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
