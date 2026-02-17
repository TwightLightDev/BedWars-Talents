package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
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
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Reincarnation extends Talent {

    public Reincarnation(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onPlayerKill(PlayerKillEvent e) {
        Player p = e.getVictim();

        User user = User.getUserFromBukkitPlayer(p);
        int level = user.getTalentLevel(getTalentId());

        if (Utility.rollChance((level * 3))) {
            e.getArena().getRespawnSessions().put(p, 1);
        }
    }
}