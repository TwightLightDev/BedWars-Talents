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

import java.util.List;
import java.util.Random;

public class WoodSupply extends Talent {
    private final Random random = new Random();

    public WoodSupply(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            e.getArena().getPlayers().forEach((p) -> {

                User user = User.getUserFromBukkitPlayer(p);
                int level = user.getTalentLevel(getTalentId());

                ItemStack i = new ItemStack(Material.WOOD, level, Short.valueOf("0"));
                p.getInventory().addItem(new ItemStack[]{i});
            });
        }, 20L);
    }
}
