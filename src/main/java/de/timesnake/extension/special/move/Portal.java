package de.timesnake.extension.special.move;

import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
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

        this.first = first.middleBlock();
        this.second = second.middleBlock();
        this.firstColor = firstColor;
        this.secondColor = secondColor;

        ExFile file = this.getFile();

        file.set(ExFile.toPath(MoversManager.getMoverPath(id), MoversManager.TYPE), PortalManager.NAME);
        file.setLocation(ExFile.toPath(MoversManager.getMoverPath(id), FIRST), this.getFirst(), false);
        file.setLocation(ExFile.toPath(MoversManager.getMoverPath(id), SECOND), this.getSecond(), false);
        file.set(ExFile.toPath(MoversManager.getMoverPath(id), FIRST_COLOR), this.firstColor);
        file.set(ExFile.toPath(MoversManager.getMoverPath(id), SECOND_COLOR), this.secondColor);
        file.save();
    }

    public Portal(MoverManager<Portal> manager, int id, ExWorld world) throws WorldNotExistException {
        super(manager, id, world);

        ExFile file = this.getFile();

        this.first = file.getExLocation(ExFile.toPath(MoversManager.getMoverPath(id), FIRST)).middleBlock();
        this.second = file.getExLocation(ExFile.toPath(MoversManager.getMoverPath(id), SECOND)).middleBlock();

        Color firstColor = file.getColorFromHex(ExFile.toPath(MoversManager.getMoverPath(id), FIRST_COLOR));
        Color secondColor = file.getColorFromHex(ExFile.toPath(MoversManager.getMoverPath(id), SECOND_COLOR));
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
        return file.remove(MoversManager.getMoverPath(this.id));
    }
}
