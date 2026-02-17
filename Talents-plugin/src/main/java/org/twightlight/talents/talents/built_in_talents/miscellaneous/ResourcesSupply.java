package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ResourcesSupply extends Talent {
    private final Random random = new Random();

    public ResourcesSupply(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            e.getArena().getPlayers().forEach((p) -> {

                User user = User.getUserFromBukkitPlayer(p);
                int level = user.getTalentLevel(getTalentId());

                int gold = 1 + Math.round((float)(level / 4)) + 2 * Math.round((float)(level / 20));
                int iron = level * 3;
                ItemStack i = new ItemStack(Material.GOLD_INGOT, level == 0 ? 0 : gold, Short.valueOf("0"));
                ItemStack i1 = new ItemStack(Material.IRON_INGOT, iron, Short.valueOf("0"));
                p.getInventory().addItem(new ItemStack[]{i});
                p.getInventory().addItem(new ItemStack[]{i1});

            });
        }, 20L);
    }
}
