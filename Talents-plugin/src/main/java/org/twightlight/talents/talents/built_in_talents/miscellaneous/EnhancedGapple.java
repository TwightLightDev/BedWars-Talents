package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.InGameData;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class EnhancedGapple extends Talent {

    public EnhancedGapple(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGappleConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();

        User user = User.getUserFromBukkitPlayer(p);
        int level = user.getTalentLevel(getTalentId());

        if (e.getItem().getType() != Material.GOLDEN_APPLE) return;

        if (Utility.rollChance(level * 2.5D)) {
            p.removePotionEffect(PotionEffectType.ABSORPTION);
            p.removePotionEffect(PotionEffectType.REGENERATION);
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 3600, 2));
        }



    }

}
