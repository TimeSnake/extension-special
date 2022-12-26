/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import org.bukkit.Color;

public class Portal extends Mover {

    public static final String FIRST = "first";
    public static final String SECOND = "second";
    public static final String FIRST_COLOR = "first_color";
    public static final String SECOND_COLOR = "second_color";

    private final ExLocation first;
    private final ExLocation second;
    private final Color firstColor;
    private final Color secondColor;

    public Portal(MoverManager<Portal> manager, ExLocation first, ExLocation second, Color firstColor,
                  Color secondColor) {
        super(manager, first.getExWorld());

        this.first = first.middleBlock().roundFacing();
        this.second = second.middleBlock().roundFacing();
        this.firstColor = firstColor;
        this.secondColor = secondColor;

        ExFile file = this.getFile();

        file.setLocation(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id), FIRST), this.getFirst(),
                true);
        file.setLocation(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id), SECOND), this.getSecond(),
                true);
        file.set(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id), FIRST_COLOR), this.firstColor);
        file.set(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id), SECOND_COLOR), this.secondColor);
        file.save();
    }

    public Portal(MoverManager<Portal> manager, int id, ExWorld world) throws WorldNotExistException {
        super(manager, id, world);

        ExFile file = this.getFile();

        this.first =
                file.getExLocation(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id), FIRST)).middleBlock().roundFacing();
        this.second =
                file.getExLocation(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id), SECOND)).middleBlock().roundFacing();

        Color firstColor = file.getColorFromHex(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id),
                FIRST_COLOR));
        Color secondColor = file.getColorFromHex(ExFile.toPath(MoversManager.getMoverPath(PortalManager.NAME, id),
                SECOND_COLOR));
        this.firstColor = firstColor != null ? firstColor : Color.fromRGB(255, 255, 255);
        this.secondColor = secondColor != null ? secondColor : Color.fromRGB(255, 255, 255);
    }

    public ExLocation getFirst() {
        return first;
    }

    public ExLocation getSecond() {
        return second;
    }

    public int getId() {
        return id;
    }

    public Color getFirstColor() {
        return firstColor;
    }

    public Color getSecondColor() {
        return secondColor;
    }

    @Override
    public boolean removeFromFile(ExFile file) {
        return file.remove(MoversManager.getMoverPath(PortalManager.NAME, this.id));
    }
}
