package org.twightlight.talents.runes.categories.offense.tier_4;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.offense.OffenseRune;

public class Ice_IV extends OffenseRune {

    double primaryValue = 32;

    public Ice_IV(int tier) {
        super(tier);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.NORMAL_PREFIX, getRuneId());
            map.getStatContainer("MELEE_SLOWING_CHANCE").add(amount * primaryValue);
            map.getStatContainer("RANGED_FROZEN_CHANCE").add(amount * primaryValue/2);

        });
    }
}
