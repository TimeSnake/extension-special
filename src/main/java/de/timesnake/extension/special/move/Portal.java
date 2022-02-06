package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import org.bukkit.Color;

import java.util.Set;

public class Portal {

    public static final String FIRST = "first";
    public static final String SECOND = "second";
    public static final String FIRST_COLOR = "first_color";
    public static final String SECOND_COLOR = "second_color";

    private final int id;
    private final ExLocation first;
    private final ExLocation second;
    private final Color firstColor;
    private final Color secondColor;

    public Portal(ExLocation first, ExLocation second, Color firstColor, Color secondColor, ExFile file) {
        this.first = first.middleBlock();
        this.second = second.middleBlock();
        this.firstColor = firstColor;
        this.secondColor = secondColor;

        Set<Integer> ids = file.getPathIntegerList(MoveManager.MOVERS);

        int id = 0;

        while (ids.contains(id)) {
            id++;
        }

        this.id = id;

        file.set(MoveManager.getMoverPath(id) + "." + MoveManager.TYPE, MoveManager.MoveType.PORTAL.name().toLowerCase());
        file.setLocation(MoveManager.getMoverPath(id) + "." + FIRST, this.getFirst(), false);
        file.setLocation(MoveManager.getMoverPath(id) + "." + SECOND, this.getSecond(), false);
        file.set(MoveManager.getMoverPath(id) + "." + FIRST_COLOR, this.firstColor);
        file.set(MoveManager.getMoverPath(id) + "." + SECOND_COLOR, this.secondColor);
    }

    public Portal(ExFile file, int id) throws WorldNotExistException {
        this.id = id;
        this.first = ExLocation.fromLocation(file.getLocation(MoveManager.getMoverPath(id) + "." + FIRST)).middleBlock();
        this.second = ExLocation.fromLocation(file.getLocation(MoveManager.getMoverPath(id) + "." + SECOND)).middleBlock();

        Color firstColor = file.getColorFromHex(MoveManager.getMoverPath(id) + "." + FIRST_COLOR);
        Color secondColor = file.getColorFromHex(MoveManager.getMoverPath(id) + "." + SECOND_COLOR);
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

    public boolean removeFromFile(ExFile file) {
        return file.remove(MoveManager.getMoverPath(this.id));
    }
}
