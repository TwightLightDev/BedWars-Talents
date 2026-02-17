package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.Utility;

import java.util.*;

public class Metallurgy extends Talent {
    private final Random random = new Random();
    private final Map<UUID, BukkitTask> tasks;
    public Metallurgy(String talentId, List<Integer> costList) {
        super(talentId, costList);
        tasks = new HashMap<>();
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;

        e.getArena().getPlayers().forEach((p) -> {

            User user = User.getUserFromBukkitPlayer(p);
            int level = user.getTalentLevel(getTalentId());
            if (level == 0) {
                return;
            }
            ItemStack i = new ItemStack(Material.GOLD_INGOT, 1, Short.valueOf("0"));
            int interval = (31 - level + (int)Math.floor((double)(level / 20))) * 20;
            BukkitTask t = Bukkit.getScheduler().runTaskTimer(Talents.getInstance(), () -> {
                if (Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p) != null && User.isPlaying(p) && p.getGameMode() == GameMode.SURVIVAL) {
                    p.getInventory().addItem(new ItemStack[]{i});
                }

            }, interval, interval);
            tasks.put(p.getUniqueId(), t);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameEnd(GameEndEvent e) {
        for (UUID winner : e.getAliveWinners()) {
            tasks.get(winner).cancel();
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onFinalKill(PlayerKillEvent e) {
        if (e.getCause().isFinalKill()) {
            tasks.get(e.getVictim().getUniqueId()).cancel();
        }
    }
}
