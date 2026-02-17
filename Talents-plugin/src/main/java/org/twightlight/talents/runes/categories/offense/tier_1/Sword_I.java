package org.twightlight.talents.runes.categories.offense.tier_1;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.offense.OffenseRune;
import org.twightlight.talents.users.User;

public class Sword_I extends OffenseRune {

    double primaryValue = 0.2;

    public Sword_I(int tier) {
        super(tier);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.NORMAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.MELEE_DAMAGE.name()).add(amount * primaryValue);
        });
    }
}
