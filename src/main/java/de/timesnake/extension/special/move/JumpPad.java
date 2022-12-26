/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;

public class JumpPad extends Mover {

    public static final String LOCATION = "location";
    public static final String SPEED = "speed";
    public static final String DIRECTION = "direction";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";

    private final ExLocation location;
    private final double speed;
    private final double x;
    private final double y;
    private final double z;

    public JumpPad(MoverManager<JumpPad> manager, ExLocation location, double speed, double x, double y, double z) {
        super(manager, location.getExWorld());

        this.location = location;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.z = z;

        ExFile file = this.getFile();

        file.setLocation(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + LOCATION, this.getLocation(),
                false);
        file.set(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + DIRECTION + "." + X, this.getX());
        file.set(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + DIRECTION + "." + Y, this.getY());
        file.set(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + DIRECTION + "." + Z, this.getZ());
        file.set(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + SPEED, this.getSpeed());
        file.save();
    }

    public JumpPad(MoverManager<JumpPad> manager, int id, ExWorld world) throws WorldNotExistException {
        super(manager, id, world);

        ExFile file = this.getFile();

        this.location =
                ExLocation.fromLocation(file.getLocation(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + LOCATION)).middleBlock();
        this.speed = file.getDouble(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + SPEED);
        this.x = file.getDouble(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + DIRECTION + "." + X);
        this.y = file.getDouble(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + DIRECTION + "." + Y);
        this.z = file.getDouble(MoversManager.getMoverPath(JumpPadManager.NAME, id) + "." + DIRECTION + "." + Z);
        file.save();
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

    @Override
    public boolean removeFromFile(ExFile file) {
        return file.remove(MoversManager.getMoverPath(JumpPadManager.NAME, this.id));
    }
}