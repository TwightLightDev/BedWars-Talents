package org.twightlight.talents.runes.categories.defense.tier_4;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.defense.DefenseRune;
import org.twightlight.talents.runes.categories.offense.OffenseRune;

public class Thorns_IV extends DefenseRune {

    double primaryValue = 1.8;
    double primaryValue1 = 4.5;


    public Thorns_IV(int tier) {
        super(tier);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.NORMAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).add(amount * primaryValue);
            map.getStatContainer(BaseStats.LIFESTEAL_REDUCTION.name()).add(amount * primaryValue1);

        });
    }
}
