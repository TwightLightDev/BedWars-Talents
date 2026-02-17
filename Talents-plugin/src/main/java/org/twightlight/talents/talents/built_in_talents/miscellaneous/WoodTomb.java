package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;

import java.util.List;

public class WoodTomb extends Talent {

    public WoodTomb(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onPlayerReSpawn(PlayerReSpawnEvent e) {
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            Player p = e.getPlayer();

            User user = User.getUserFromBukkitPlayer(p);
            int level = user.getTalentLevel(getTalentId());

            ItemStack i = new ItemStack(Material.WOOD, level, Short.valueOf("0"));
            p.getInventory().addItem(new ItemStack[]{i});
        }, 20L);
    }
}
