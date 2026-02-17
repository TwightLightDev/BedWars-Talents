package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class AngelGift extends Talent {

    public AngelGift(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onPlayerReSpawn(PlayerReSpawnEvent e) {
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            Player p = e.getPlayer();

            User user = User.getUserFromBukkitPlayer(p);
            int level = user.getTalentLevel(getTalentId());

            int gold = 1 + Math.round((float)(level / 10));
            ItemStack i = new ItemStack(Material.GOLD_INGOT, level == 0 ? 0 : gold, Short.valueOf("0"));
            ItemStack i1 = new ItemStack(Material.IRON_INGOT, level, Short.valueOf("0"));
            p.getInventory().addItem(new ItemStack[]{i});
            p.getInventory().addItem(new ItemStack[]{i1});
        }, 20L);
    }
}
