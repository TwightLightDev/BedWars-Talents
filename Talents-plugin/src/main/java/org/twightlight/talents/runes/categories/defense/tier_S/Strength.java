package org.twightlight.talents.runes.categories.defense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.defense.DefenseRune;

public class Strength extends DefenseRune {
    public Strength() {
        super(64);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_ARMOR.name()).add(20 * amount);

            if (amount < 1) return;
            map.getStatContainer("BLOCK_CHANCE").add(5 * amount);
            map.getStatContainer("BLOCK_RATIO").add(5 * amount);
            map.getStatContainer("REFLECTION_CHANCE").add(5 * amount);
            map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_REGENERATION_RATIO.name()).add(5 * amount);
        });
    }
}
