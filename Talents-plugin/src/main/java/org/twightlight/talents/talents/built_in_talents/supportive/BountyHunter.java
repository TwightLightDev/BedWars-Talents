package org.twightlight.talents.talents.built_in_talents.supportive;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.InGameData;
import org.twightlight.talents.users.User;

import java.util.List;

public class BountyHunter extends Talent {

    public BountyHunter(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onPlayerKill(PlayerKillEvent e) {
        Player p = e.getKiller();
        if (p == null) return;
        User user = User.getUserFromBukkitPlayer(p);
        int level = user.getTalentLevel(getTalentId());

        int gold = 1 + Math.round((float)(level / 10));
        ItemStack i = new ItemStack(Material.GOLD_INGOT, gold);
        ItemStack i1 = new ItemStack(Material.IRON_INGOT, level);
        p.getInventory().addItem(new ItemStack[]{i});
        p.getInventory().addItem(new ItemStack[]{i1});
    }

}
