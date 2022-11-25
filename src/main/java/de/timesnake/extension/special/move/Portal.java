/*
 * workspace.extension-special.main
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
