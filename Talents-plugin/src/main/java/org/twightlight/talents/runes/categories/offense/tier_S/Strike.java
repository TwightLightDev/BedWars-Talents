package org.twightlight.talents.runes.categories.offense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.metadata.FixedMetadataValue;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.offense.OffenseRune;
import org.twightlight.talents.utils.Utility;

public class
Strike extends OffenseRune {

    public Strike() {
        super(64);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).add(15 * amount);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "Penetration");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        if (Utility.rollChance(25)) {
            StatsMap statsMap = e.getDamagePacket().getAttackerStatsMap();
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            if (!property.hasLayer("criticalLayer")) {
                property.addLayer("criticalLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer("criticalLayer", 1);
            }
            property.addValueToLayer("criticalLayer", statsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).get() / 100 + statsMap.getStatContainer(BaseStats.MELEE_CRITICAL_DAMAGE.name()).get() / 100);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "Penetration");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        if (Utility.rollChance(25)) {
            StatsMap statsMap = e.getDamagePacket().getAttackerStatsMap();
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            if (!property.hasLayer("criticalLayer")) {
                property.addLayer("criticalLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer("criticalLayer", 1);
            }
            property.addValueToLayer("criticalLayer", statsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).get() / 100 + statsMap.getStatContainer(BaseStats.RANGED_CRITICAL_DAMAGE.name()).get() / 100);
        }
    }
}
