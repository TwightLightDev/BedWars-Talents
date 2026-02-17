package org.twightlight.talents.internal;

import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.talents.menus.AttributeMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Arena {
    public static Map<IArena, Arena> arenas = new HashMap<>();
    public final List<Player> players = new ArrayList<>();
    public final List<AttributeMenu> attributeMenus = new ArrayList<>();
    private final IArena arena;
    private List<BukkitTask> tasks;

    public Arena(IArena iArena, List<BukkitTask> tasks) {
        arenas.put(iArena, this);
        arena = iArena;
        this.tasks = tasks;
    }

    public void setTasks(List<BukkitTask> tasks) {
        this.tasks = tasks;
    }

    public void addTask(BukkitTask task) {
        if (task == null) {
            return;
        }
        tasks.add(task);
    }

    public List<BukkitTask> getTasks() {
        return tasks;
    }

    public void closeInstance() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        arenas.remove(arena);
        for (Player p : players) {
            p.remove();
        }

        for (AttributeMenu menu : attributeMenus) {
            menu.remove();
        }

        tasks.clear();
        players.clear();
        attributeMenus.clear();
        arenas.remove(arena);
    }

    public void addPlayer(Player p) {
        players.add(p);
    }

    public void addAttributeMenu(AttributeMenu menu) {
        attributeMenus.add(menu);
    }
    public IArena getArena() {
        return arena;
    }
}
