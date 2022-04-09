package de.timesnake.extension.special.move;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.extension.special.chat.Plugin;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.extension.util.cmd.Arguments;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.io.File;
import java.util.*;

public class MoversManager implements Listener {

    public static final String FILE_NAME = "movers";

    public static final String MOVERS = "movers";
    public static final String TYPE = "type";

    private static MoversManager instance;
    private final Map<ExWorld, ExFile> moversFilesByWorld = new HashMap<>();
    private final HashMap<String, MoverManager<?>> moverManagersByName = new HashMap<>();

    public MoversManager() {
        instance = this;

        PortalManager portalManager = new PortalManager();
        this.moverManagersByName.put(portalManager.getName(), portalManager);

        JumpPadManager jumpPadManager = new JumpPadManager();
        this.moverManagersByName.put(jumpPadManager.getName(), jumpPadManager);

        ElevatorManager elevatorManager = new ElevatorManager();
        this.moverManagersByName.put(elevatorManager.getName(), elevatorManager);

        for (ExWorld world : Server.getWorlds()) {
            File gameFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + FILE_NAME + ".yml");
            if (gameFile.exists()) {
                this.moversFilesByWorld.put(world, new ExFile(world.getWorldFolder(), FILE_NAME + ".yml"));
            }
        }

        for (Map.Entry<ExWorld, ExFile> entry : this.getMoversFilesByWorld().entrySet()) {

            LinkedList<Integer> loadedMovers = new LinkedList<>();

            ExWorld world = entry.getKey();
            ExFile file = entry.getValue();

            for (Integer id : file.getPathIntegerList(MOVERS)) {
                String type = file.getString(getMoverPath(id) + "." + TYPE);

                if (type == null) {
                    continue;
                }

                MoverManager<?> moverManager = this.moverManagersByName.get(type);

                try {
                    moverManager.addMover(file, id, world);
                    loadedMovers.add(id);
                } catch (WorldNotExistException e) {
                    Server.printWarning(Plugin.SPECIAL,
                            "Can not load portal with id " + id + " in world " + world.getName());
                }
            }

            Server.printText(Plugin.SPECIAL,
                    "Loaded movers in world " + world.getName() + ": " + Arrays.toString(loadedMovers.toArray()));
        }

        Server.registerListener(this, ExSpecial.getPlugin());

        Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "movers", List.of("mvs", "mover"), new MoveCmd()
                , Plugin.SPECIAL);
    }

    public static MoversManager getInstance() {
        return instance;
    }

    public static String getMoverPath(int id) {
        return MOVERS + "." + id;
    }

    public boolean handleCommand(Sender sender, User user, String type, Arguments<Argument> args) {
        MoverManager<?> moverManager = this.moverManagersByName.get(type.toLowerCase());

        if (moverManager == null) {
            return false;
        }

        moverManager.handleCommand(sender, user, args);
        return true;
    }

    public List<String> handleTabComplete(String type, Arguments<Argument> args) {
        MoverManager<?> moverManager = this.moverManagersByName.get(type.toLowerCase());

        if (moverManager == null) {
            return List.of();
        }

        return moverManager.handleTabComplete(args);
    }

    public Map<ExWorld, ExFile> getMoversFilesByWorld() {
        return moversFilesByWorld;
    }

    public ExFile getMoveFile(ExWorld world) {
        return this.moversFilesByWorld.computeIfAbsent(world, (w) -> new ExFile(world.getWorldFolder(), FILE_NAME +
                ".yml"));
    }

    @EventHandler
    public void onUserMove(UserMoveEvent e) {

        User user = e.getUser();

        for (MoverManager<?> moverManager : this.moverManagersByName.values()) {
            moverManager.trigger(user, MoverManager.Trigger.MOVE);
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        User user = Server.getUser(e.getPlayer());

        for (MoverManager<?> moverManager : this.moverManagersByName.values()) {
            moverManager.trigger(user, MoverManager.Trigger.SNEAK);
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent e) {
        User user = Server.getUser(e.getPlayer());

        for (MoverManager<?> moverManager : this.moverManagersByName.values()) {
            moverManager.trigger(user, MoverManager.Trigger.JUMP);
        }
    }

}
