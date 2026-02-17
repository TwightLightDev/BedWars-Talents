package org.twightlight.talents.talents.built_in_talents.supportive;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.entity.Player;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.InGameData;
import org.twightlight.talents.users.User;

import java.util.List;

public class Shield extends Talent {

    public Shield(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onPlayerKill(PlayerKillEvent e) {
        Player p = e.getKiller();
        if (p == null) return;
        User user = User.getUserFromBukkitPlayer(p);
        int level = user.getTalentLevel(getTalentId());
        IArena iArena = e.getArena();
        Arena arena = Talents.getInstance().getArenaManager().getArenaFromIArena(iArena);

        int kills = arena.getInGameDataMap().get(p.getUniqueId()).get(InGameData.CURRENT_KILLS);
        StatsMap statsMap = arena.getStatsMapOfUUID(p.getUniqueId());

        if (kills <= 5) {
            double add = level * 0.1D;
            double add1 = level * 0.1D;
            statsMap.getStatContainer("BLOCK_CHANCE").add(add);
            statsMap.getStatContainer("BLOCK_RATIO").add(add1);

        }

    }

}
