/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.SimpleLocation;
import de.timesnake.basic.bukkit.util.world.entity.HoloDisplay;
import de.timesnake.basic.bukkit.util.world.entity.PacketEntity;

import java.util.List;
import java.util.Objects;

public class Display {

  private final int id;
  private final SimpleLocation position;
  private final List<String> text;

  private transient PacketEntity packetEntity;
  private transient ExWorld world;

  public Display(DisplayManager manager, ExWorld world, SimpleLocation position, List<String> text) {
    this.id = manager.newId(world);
    this.position = position;
    this.text = text;
    this.world = world;
    this.loadEntity();
  }

  public int getId() {
    return id;
  }

  public SimpleLocation getPosition() {
    return position;
  }

  public List<String> getText() {
    return text;
  }

  public PacketEntity getPacketEntity() {
    return packetEntity;
  }

  public void setPacketEntity(PacketEntity packetEntity) {
    this.packetEntity = packetEntity;
  }

  public ExWorld getWorld() {
    return world;
  }

  void setWorld(ExWorld world) {
    this.world = world;
  }

  void loadEntity() {
    this.packetEntity = new HoloDisplay(position.toLocation(world), this.text);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Display display = (Display) o;
    return id == display.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
