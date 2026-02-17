package org.twightlight.talents.talents.built_in_talents.supportive;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.entity.Despawnable;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.InGameData;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Shura extends Talent {

    public Shura(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onPlayerKill(PlayerKillEvent e) {
        Player p = e.getKiller();
        if (p == null) return;
        User user = User.getUserFromBukkitPlayer(p);
        int level = user.getTalentLevel(getTalentId());

        p.setHealth(Math.min((double)level * 1.5D / 100.0D * p.getMaxHealth() + p.getHealth(), p.getMaxHealth()));

    }

}
