package org.twightlight.talents.runes.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.CustomListener;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.utils.Utility;

public class Curser implements CustomListener {
    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        double chance = v.getStatContainer("CURSE_CHANCE").get();

        Player attacker = e.getDamagePacket().getAttacker();
        if (Utility.rollChance(chance)) {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 3), false);
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20, 1), false);

        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        double chance = v.getStatContainer("CURSE_CHANCE").get();

        Player attacker = e.getDamagePacket().getAttacker();
        if (Utility.rollChance(chance)) {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 3), false);
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20, 1), false);

        }
    }

    public void onRegister() {
        return;
    }

    public void onUnregister() {
        return;
    }

    public boolean onDispatchError(Event event, String method, Exception exception) {
        return false;
    }

}
