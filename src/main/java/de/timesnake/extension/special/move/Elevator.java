/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import java.util.ArrayList;
import java.util.TreeSet;

public class Elevator extends Mover {

  private static final String LOCATION = "base";
  private static final String X = "x";
  private static final String Z = "z";
  private static final String HEIGHTS = "heights";

  private final double x;
  private final double z;

  private final TreeSet<Integer> heights = new TreeSet<>();

  public Elevator(MoverManager<Elevator> manager, ExWorld world, double x, double z) {
    super(manager, world);

    this.x = x;
    this.z = z;

    ExFile file = this.getFile();

    file.set(ExFile.toPath(MoversManager.getMoverPath(ElevatorManager.NAME, id), LOCATION, X),
        this.x);
    file.set(ExFile.toPath(MoversManager.getMoverPath(ElevatorManager.NAME, id), LOCATION, Z),
        this.z);

    file.save();
  }

  public Elevator(MoverManager<Elevator> manager, int id, ExWorld world) {
    super(manager, id, world);

    ExFile file = this.getFile();

    this.x = file.getDouble(
        ExFile.toPath(MoversManager.getMoverPath(ElevatorManager.NAME, id), LOCATION, X));
    this.z = file.getDouble(
        ExFile.toPath(MoversManager.getMoverPath(ElevatorManager.NAME, id), LOCATION, Z));

    this.heights.addAll(
        file.getIntegerList(ExFile.toPath(MoversManager.getMoverPath(ElevatorManager.NAME, id),
            HEIGHTS)));
  }

  public boolean addLevel(int height) {
    boolean b = this.heights.add(height);

    if (b) {
      this.getFile()
          .set(ExFile.toPath(MoversManager.getMoverPath(ElevatorManager.NAME, id), HEIGHTS),
              new ArrayList<>(this.heights)).save();
    }

    return b;
  }

  public boolean removeLevel(int height) {
    boolean b = this.heights.remove(height);

    if (b) {
      this.getFile()
          .set(ExFile.toPath(MoversManager.getMoverPath(ElevatorManager.NAME, id), HEIGHTS),
              new ArrayList<>(this.heights)).save();
    }

    return b;
  }

  public ExWorld getWorld() {
    return world;
  }

  public double getX() {
    return x;
  }

  public double getZ() {
    return z;
  }

  public Integer lower(int height) {
    return this.heights.lower(height);
  }

  public Integer higher(int height) {
    return this.heights.higher(height);
  }

  @Override
  public boolean removeFromFile(ExFile file) {
    return file.remove(MoversManager.getMoverPath(ElevatorManager.NAME, this.id));
  }
}
