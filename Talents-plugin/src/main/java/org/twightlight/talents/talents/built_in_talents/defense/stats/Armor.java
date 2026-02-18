package org.twightlight.talents.talents.built_in_talents.defense.stats;


import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.List;

public class Armor extends Talent {
    public Armor(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            int level = user.getTalentLevel(getTalentId());
            map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_ARMOR.name()).add((double) level);
        });
    }
}
