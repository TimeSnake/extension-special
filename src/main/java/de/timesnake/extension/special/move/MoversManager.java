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

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.file.ExFile;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.extension.special.chat.Plugin;
import de.timesnake.extension.special.main.ExSpecial;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.cmd.Arguments;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MoversManager implements Listener {

    public static final String FILE_NAME = "movers";

    public static final String MOVERS = "movers";

    public static MoversManager getInstance() {
        return instance;
    }

    public static String getMoverPath(String type, int id) {
        return ExFile.toPath(MOVERS, type, String.valueOf(id));
    }

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

            for (String type : file.getPathStringList(MOVERS)) {
                MoverManager<?> moverManager = this.moverManagersByName.get(type);

                if (type == null) {
                    continue;
                }

                for (Integer id : file.getPathIntegerList(ExFile.toPath(MOVERS, type))) {
                    try {
                        moverManager.addMover(file, id, world);
                        loadedMovers.add(id);
                    } catch (WorldNotExistException e) {
                        Server.printWarning(Plugin.SPECIAL,
                                "Can not load " + type + " with id " + id + " in world " + world.getName());
                    }
                }
            }

            Server.printText(Plugin.SPECIAL,
                    "Loaded movers in world " + world.getName() + ": " + Chat.listToString(loadedMovers));
        }

        Server.registerListener(this, ExSpecial.getPlugin());

        Server.getCommandManager().addCommand(ExSpecial.getPlugin(), "movers", List.of("mvs", "mover"), new MoveCmd()
                , Plugin.SPECIAL);
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
