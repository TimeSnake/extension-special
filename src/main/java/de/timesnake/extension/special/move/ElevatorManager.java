package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElevatorManager extends MoverManager<Elevator> {

    public static final double RADIUS = 0.9;

    public static final String NAME = "elevator";

    public ElevatorManager() {
        super(NAME);
    }

    public Elevator addElevator(ExWorld world, double x, double z) {
        Elevator elevator = new Elevator(this, world, x, z);
        this.moversByWorld.computeIfAbsent(world, (w) -> new HashSet<>()).add(elevator);
        return elevator;
    }

    public boolean removeElevator(ExWorld world, Integer id) {
        Elevator elevator = this.getElevators(world).stream().filter(e -> e.getId() == id).findFirst().orElse(null);

        if (elevator == null) {
            return false;
        }

        ExFile file = MoversManager.getInstance().getMoveFile(world);

        elevator.removeFromFile(file);

        this.getElevators(world).remove(elevator);

        return true;
    }

    public Integer removeElevator(ExLocation loc, double range) {
        Elevator elevator = this.getElevators(loc.getExWorld()).stream().filter(e ->
                loc.getExWorld().equals(e.getWorld()) && Math.abs(e.getX() - loc.getX()) <= range &&
                        Math.abs(e.getZ() - loc.getZ()) <= range).findFirst().orElse(null);

        if (elevator == null) {
            return null;
        }

        ExFile file = MoversManager.getInstance().getMoveFile(loc.getExWorld());

        elevator.removeFromFile(file);

        this.getElevators(loc.getExWorld()).remove(elevator);

        return elevator.getId();
    }

    public Set<Elevator> getElevators(ExWorld world) {
        return this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>());
    }

    @Override
    public void trigger(User user, Trigger type) {
        Location location = user.getLocation();

        if (type.equals(Trigger.JUMP) || (type.equals(Trigger.SNEAK) && user.isSneaking())) {
            Set<Elevator> elevators = this.moversByWorld.get(user.getExWorld());

            if (elevators != null) {
                for (Elevator elevator : elevators) {
                    if (elevator.getWorld().equals(location.getWorld())
                            && Math.abs(elevator.getX() - location.getX()) < RADIUS * RADIUS
                            && Math.abs(elevator.getZ() - location.getZ()) < RADIUS * RADIUS) {

                        if (type.equals(Trigger.JUMP)) {
                            Integer higher = elevator.higher(location.getBlockY());

                            if (higher == null) {
                                return;
                            }

                            user.teleport(location.getX(), higher, location.getZ());
                        } else {
                            Integer lower = elevator.lower(location.getBlockY());

                            if (lower == null) {
                                return;
                            }

                            user.teleport(location.getX(), lower, location.getZ());
                        }

                        return;
                    }
                }
            }
        }
    }

    @Override
    public void handleCommand(Sender sender, User user, Arguments<Argument> args) {
        String action = args.getString(0);

        if (action.equalsIgnoreCase("add")) {

            ExLocation location = user.getExLocation();

            ExWorld world = location.getExWorld();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            Elevator elevator = this.getElevators(location.getExWorld()).stream().filter(e ->
                    location.getExWorld().equals(e.getWorld()) && Math.abs(e.getX() - x) <= RADIUS &&
                            Math.abs(e.getZ() - z) <= RADIUS).findFirst().orElse(null);

            if (elevator == null) {
                elevator = this.addElevator(world, x, z);
                sender.sendPluginMessage(Component.text("Created elevator ", ExTextColor.PERSONAL)
                        .append(Component.text(elevator.getId(), ExTextColor.VALUE)));
            }

            elevator.addLevel((int) y);
            sender.sendPluginMessage(Component.text("Added level ", ExTextColor.PERSONAL)
                    .append(Component.text((int) y, ExTextColor.VALUE))
                    .append(Component.text(" at elevator ", ExTextColor.PERSONAL))
                    .append(Component.text(elevator.getId(), ExTextColor.VALUE)));


        } else if (action.equalsIgnoreCase("remove")) {
            if (args.isLengthEquals(1, false)) {
                Integer removedId = this.removeElevator(user.getExLocation(), 2);
                if (removedId != null) {
                    sender.sendPluginMessage(Component.text("Removed elevator with id ", ExTextColor.PERSONAL)
                            .append(Component.text(removedId, ExTextColor.VALUE)));
                } else {
                    sender.sendPluginMessage(Component.text("No elevator found", ExTextColor.WARNING));
                }
            } else if (args.get(1).isInt(true)) {
                Integer removeId = args.get(1).toInt();
                boolean removed = this.removeElevator(user.getExWorld(), removeId);
                if (removed) {
                    sender.sendPluginMessage(Component.text("Removed elevator with id ", ExTextColor.PERSONAL)
                            .append(Component.text(removeId, ExTextColor.VALUE)));
                } else {
                    sender.sendPluginMessage(Component.text("No elevator with found with id ", ExTextColor.WARNING)
                            .append(Component.text(removed, ExTextColor.VALUE)));
                }
            } else {
                sender.sendMessageCommandHelp("Remove elevator", "movers elevator remove [id]");
            }
        } else {
            sender.sendMessageCommandHelp("Create elevator", "movers elevator add");
            sender.sendMessageCommandHelp("Remove elevator", "movers elevator remove [id]");
        }
    }

    @Override
    public List<String> handleTabComplete(Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("add", "remove");
        }

        return List.of();
    }

    @Override
    public void addMover(ExFile file, int id, ExWorld world) {
        Elevator elevator = new Elevator(this, id, world);
        this.moversByWorld.computeIfAbsent(world, w -> new HashSet<>()).add(elevator);
    }
}
