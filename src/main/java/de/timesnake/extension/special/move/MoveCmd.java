package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import org.bukkit.Color;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MoveCmd implements CommandListener {

    private final HashMap<UUID, Tuple<ExLocation, Color>> firstPortalByUuid = new HashMap<>();

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

        String type = args.getString(0);
        String action = args.getString(1);

        if (type.equalsIgnoreCase("portal")) {
            if (action.equalsIgnoreCase("add")) {
                if (!args.isLengthHigherEquals(3, true)) {
                    return;
                }

                String number = args.getString(2);

                boolean isFirst = !number.equalsIgnoreCase("second");

                if (!args.isLengthEquals(4, true) || !args.get(3).isHexColor(true)) {
                    if (isFirst) {
                        sender.sendMessageCommandHelp("Add first portal", "movers portal first <hexColor>");
                    } else {
                        sender.sendMessageCommandHelp("Add second portal", "movers portal second <hexColor>");
                    }
                    return;
                }

                if (isFirst) {
                    this.firstPortalByUuid.put(user.getUniqueId(), new Tuple<>(user.getExLocation(), args.get(3).toColorFromHex()));
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Saved first location");
                    sender.sendMessageCommandHelp("Add second portal", "movers portal second <hexColor>");
                } else if (this.firstPortalByUuid.containsKey(user.getUniqueId())) {

                    Tuple<ExLocation, Color> first = this.firstPortalByUuid.remove(user.getUniqueId());
                    ExLocation secondLoc = user.getExLocation();
                    Color color = args.get(3).toColorFromHex();

                    Integer id = MoveManager.getInstance().addPortal(first.getA(), secondLoc, first.getB(), color);

                    sender.sendPluginMessage(ChatColor.PERSONAL + "Created portal with id " + ChatColor.VALUE + id);
                } else {
                    sender.sendMessageCommandHelp("Create portal", "movers portal first <hexColor>");
                }

            } else if (action.equalsIgnoreCase("remove")) {
                if (args.isLengthEquals(2, false)) {
                    Integer removedId = MoveManager.getInstance().removePortal(user.getExLocation(), 2);
                    if (removedId != null) {
                        sender.sendPluginMessage(ChatColor.PERSONAL + "Removed portal with id " + ChatColor.VALUE + removedId);
                    } else {
                        sender.sendPluginMessage(ChatColor.WARNING + "No portal found");
                    }
                } else if (args.get(2).isInt(true)) {
                    Integer removeId = args.get(2).toInt();
                    boolean removed = MoveManager.getInstance().removePortal(user.getExWorld(), removeId);
                    if (removed) {
                        sender.sendPluginMessage(ChatColor.PERSONAL + "Removed portal with id " + ChatColor.VALUE + removeId);
                    } else {
                        sender.sendPluginMessage(ChatColor.WARNING + "No portal with found with id " + ChatColor.PERSONAL + removed);
                    }
                } else {
                    sender.sendMessageCommandHelp("Remove portal", "movers portal remove [id]");
                }
            } else {
                sender.sendMessageCommandHelp("Create portal", "movers portal add <first/second> <hexColor>");
                sender.sendMessageCommandHelp("Remove portal", "movers portal remove [id]");
            }
        } else if (type.equalsIgnoreCase("jumppad")) {
            if (action.equalsIgnoreCase("add")) {
                if (args.isLengthEquals(6, false)) {
                    if (args.get(2).isDouble(true) && args.get(3).isDouble(true) && args.get(4).isDouble(true) && args.get(5).isDouble(true)) {
                        double vecX = args.get(2).toDouble();
                        double vecY = args.get(3).toDouble();
                        double vecZ = args.get(4).toDouble();
                        double speed = args.get(5).toDouble();

                        Integer id = MoveManager.getInstance().addJumpPad(user.getExLocation(), vecX, vecY, vecZ, speed);

                        sender.sendPluginMessage(ChatColor.PERSONAL + "Added jump pad with id " + ChatColor.VALUE + id);
                    }
                } else if (args.isLengthEquals(4, true)) {
                    if (args.get(2).isDouble(true) && args.get(3).isDouble(true)) {
                        double height = args.get(2).toDouble();
                        double speed = args.get(3).toDouble();

                        ExLocation loc = user.getExLocation();

                        Vector vector = loc.getDirection().normalize();
                        vector.setY(height);

                        Integer id = MoveManager.getInstance().addJumpPad(user.getExLocation(), vector.getX(), vector.getY(), vector.getZ(), speed);

                        sender.sendPluginMessage(ChatColor.PERSONAL + "Added jump pad with id " + ChatColor.VALUE + id);
                    }
                } else {
                    sender.sendMessageCommandHelp("Create jump pad", "movers jumppad add <dX> <dY> <dZ> <speed>");
                    sender.sendMessageCommandHelp("Create jump pad with look direction", "movers jumppad add <height> <speed>");
                }

            } else if (action.equalsIgnoreCase("remove")) {
                if (args.isLengthEquals(2, false)) {
                    Integer removedId = MoveManager.getInstance().removeJumpPad(user.getExLocation(), 2);
                    if (removedId != null) {
                        sender.sendPluginMessage(ChatColor.PERSONAL + "Removed jump pad with id " + ChatColor.VALUE + removedId);
                    } else {
                        sender.sendPluginMessage(ChatColor.WARNING + "No jump pad found");
                    }
                } else if (args.get(2).isInt(true)) {
                    Integer removeId = args.get(2).toInt();
                    boolean removed = MoveManager.getInstance().removeJumpPad(user.getExWorld(), removeId);
                    if (removed) {
                        sender.sendPluginMessage(ChatColor.PERSONAL + "Removed jump pad with id " + ChatColor.VALUE + removeId);
                    } else {
                        sender.sendPluginMessage(ChatColor.WARNING + "No jump pad with found with id " + ChatColor.VALUE + removed);
                    }
                } else {
                    sender.sendMessageCommandHelp("Remove jump pad", "movers jumppad remove [id]");
                }
            } else {
                sender.sendMessageCommandHelp("Create jump pad", "movers jumppad add <dX> <dY> <dZ> <speed>");
                sender.sendMessageCommandHelp("Create jump pad with look direction", "movers jumppad add <height> <speed>");
                sender.sendMessageCommandHelp("Remove jump pad", "movers jumppad remove [id]");
            }
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("portal", "jumppad");
        }
        if (args.length() == 2) {
            return List.of("add", "remove");
        }

        if (args.get(0).equalsIgnoreCase("portal")) {
            if (args.length() == 3) {
                return List.of("first", "second");
            }
        }

        if (args.get(0).equalsIgnoreCase("jumppad")) {
            if (args.length() == 3) {
                return List.of("<directionX>", "<height>");
            }
            if (args.length() == 4) {
                return List.of("<directionY>", "<speed>");
            }
            if (args.length() == 5) {
                return List.of("<directionZ>");
            }
            if (args.length() == 6) {
                return List.of("<speed>");
            }
        }
        return null;
    }
}
