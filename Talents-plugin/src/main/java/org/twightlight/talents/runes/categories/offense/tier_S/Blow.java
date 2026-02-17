package org.twightlight.talents.runes.categories.offense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.metadata.FixedMetadataValue;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.offense.OffenseRune;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class Blow extends OffenseRune {

    public Blow() {
        super(64);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.PENETRATION_RATIO.name()).add(12 * amount);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "Crit_Rate");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        if (Utility.rollChance(10)) {
            e.getDamagePacket().getVictim().setMetadata("frozen", new FixedMetadataValue(Talents.getInstance(), true));
            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                e.getDamagePacket().getVictim().removeMetadata("frozen", Talents.getInstance());
            }, 20L);
            DamageProperty property = e.getDamagePacket().getDamageProperty();

            property.addLayer("blowLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
            property.addValueToLayer("blowLayer", 1.2);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "LifeSteal");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        if (Utility.rollChance(25)) {
            e.getDamagePacket().getVictim().setMetadata("frozen", new FixedMetadataValue(Talents.getInstance(), true));
            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                e.getDamagePacket().getVictim().removeMetadata("frozen", Talents.getInstance());
            }, 20L);
            DamageProperty property = e.getDamagePacket().getDamageProperty();

            property.addLayer("blowLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
            property.addValueToLayer("blowLayer", 1.2);
        }
    }
}
