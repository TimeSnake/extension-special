package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;

import java.util.Set;

public class JumpPad {

    public static final String LOCATION = "location";
    public static final String SPEED = "speed";
    public static final String DIRECTION = "direction";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";


    private final int id;

    private final ExLocation location;
    private final double speed;
    private final double x;
    private final double y;
    private final double z;

    public JumpPad(ExLocation location, double speed, double x, double y, double z, ExFile file) {
        this.location = location;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.z = z;

        Set<Integer> ids = file.getPathIntegerList(MoveManager.MOVERS);

        int id = 0;

        while (ids.contains(id)) {
            id++;
        }

        this.id = id;

        file.set(MoveManager.getMoverPath(id) + "." + MoveManager.TYPE, MoveManager.MoveType.JUMP_PAD.name().toLowerCase());
        file.setLocation(MoveManager.getMoverPath(id) + "." + LOCATION, this.getLocation(), false);
        file.set(MoveManager.getMoverPath(id) + "." + DIRECTION + "." + X, this.getX());
        file.set(MoveManager.getMoverPath(id) + "." + DIRECTION + "." + Y, this.getY());
        file.set(MoveManager.getMoverPath(id) + "." + DIRECTION + "." + Z, this.getZ());
        file.set(MoveManager.getMoverPath(id) + "." + SPEED, this.getSpeed());
    }

    public JumpPad(ExFile file, int id) throws WorldNotExistException {
        this.id = id;
        this.location = ExLocation.fromLocation(file.getLocation(MoveManager.getMoverPath(id) + "." + LOCATION)).middleBlock();
        this.speed = file.getDouble(MoveManager.getMoverPath(id) + "." + SPEED);
        this.x = file.getDouble(MoveManager.getMoverPath(id) + "." + DIRECTION + "." + X);
        this.y = file.getDouble(MoveManager.getMoverPath(id) + "." + DIRECTION + "." + Y);
        this.z = file.getDouble(MoveManager.getMoverPath(id) + "." + DIRECTION + "." + Z);
    }


    public int getId() {
        return id;
    }

    public ExLocation getLocation() {
        return location;
    }

    public double getSpeed() {
        return speed;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public boolean removeFromFile(ExFile file) {
        return file.remove(MoveManager.getMoverPath(this.id));
    }
}