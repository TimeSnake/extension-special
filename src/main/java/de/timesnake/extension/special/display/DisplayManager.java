package de.timesnake.extension.special.display;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.HoloDisplay;
import de.timesnake.basic.bukkit.util.world.HoloDisplayManager;
import de.timesnake.extension.special.chat.Plugin;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.extension.util.chat.Chat;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DisplayManager {

    public static final String FILE_NAME = "holo_displays";

    public static final String DISPLAYS = "displays";

    private static DisplayManager instance;

    public static DisplayManager getInstance() {
        return instance;
    }

    private final Map<ExWorld, ExFile> displayFilesByWorld = new HashMap<>();

    public DisplayManager() {
        instance = this;

        for (ExWorld world : Server.getWorlds()) {
            File gameFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + FILE_NAME + ".yml");
            if (gameFile.exists()) {
                this.displayFilesByWorld.put(world, new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));
            }
        }

        for (Map.Entry<ExWorld, ExFile> entry : this.getDisplayFilesPerWorld().entrySet()) {

            ExWorld world = entry.getKey();
            ExFile file = entry.getValue();

            HoloDisplayManager displayManager = Server.getHoloDisplayManager();

            LinkedList<Integer> loadedDisplays = new LinkedList<>();

            for (Integer id : file.getPathIntegerList(DISPLAYS)) {
                try {
                    displayManager.addHoloDisplay(new Display(file, id), true);
                    loadedDisplays.add(id);
                } catch (WorldNotExistException e) {
                    Server.printWarning(Plugin.SPECIAL,
                            "Can not load display with id " + id + " in world " + world.getName());
                }
            }

            if (loadedDisplays.size() > 0) {
                Server.printText(Plugin.SPECIAL, "Loaded displays: " + Chat.listToString(loadedDisplays) + " in world" +
                        " " + world.getName());
            }
        }

        Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "holodisplay", List.of("holod"), new DisplayCmd(), Plugin.SPECIAL);
    }

    public Map<ExWorld, ExFile> getDisplayFilesPerWorld() {
        return this.displayFilesByWorld;
    }

    public static String getDisplayPath(int id) {
        return DISPLAYS + "." + id;
    }

    public int addDisplay(ExLocation loc, List<String> lines) {
        ExFile file = this.displayFilesByWorld.computeIfAbsent(loc.getExWorld(), (w) -> new ExFile(loc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

        Display display = new Display(loc, lines, file);

        Server.getHoloDisplayManager().addHoloDisplay(display, true);

        return display.getId();
    }

    public Integer removeDisplay(ExLocation loc, double range) {
        Display display = null;

        for (HoloDisplay holoDisplay : Server.getHoloDisplayManager().getHoloDisplays(loc.getExWorld())) {
            if (holoDisplay.getLocation().distance(loc) <= range && holoDisplay instanceof Display) {
                display = (Display) holoDisplay;
                break;
            }
        }

        if (display == null) {
            return null;
        }

        ExFile file = this.displayFilesByWorld.computeIfAbsent(loc.getExWorld(),
                (w) -> new ExFile(loc.getExWorld().getWorldFolder(), FILE_NAME + ".yml"));

        display.removeFromFile(file);

        Server.getHoloDisplayManager().removeHoloDisplay(display);

        return display.getId();
    }

    public boolean removeDisplay(ExWorld world, Integer id) {
        Display display = null;

        for (HoloDisplay holoDisplay : Server.getHoloDisplayManager().getHoloDisplays(world)) {
            if (holoDisplay instanceof Display && ((Display) holoDisplay).getId() == id) {
                display = (Display) holoDisplay;
                break;
            }
        }

        if (display == null) {
            return false;
        }

        ExFile file = this.displayFilesByWorld.computeIfAbsent(world,
                (w) -> new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));

        display.removeFromFile(file);

        Server.getHoloDisplayManager().removeHoloDisplay(display);

        return true;
    }
}
