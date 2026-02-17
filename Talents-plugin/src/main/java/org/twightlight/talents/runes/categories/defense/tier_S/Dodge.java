package org.twightlight.talents.runes.categories.defense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.util.Color;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.defense.DefenseRune;
import org.twightlight.talents.utils.Utility;

public class Dodge extends DefenseRune {

    public ParticleDustColored particle = new ParticleDustColored(20);

    public Dodge() {
        super(64);

        particle.setOffset(1, 2, 1);
        particle.setColor(Color.YELLOW);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.LIFESTEAL_REDUCTION.name()).add(7.5 * amount);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();
        if (!(packet.getVictim() instanceof Player)) return;

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.NORMAL_PREFIX, getCategory(), "Curse");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;

        if (Utility.rollChance(10)) {
            packet.getVictim().getWorld().playSound(packet.getVictim().getLocation(), XSound.ITEM_SHIELD_BLOCK.parseSound(), 10.0F, 2.0F);
            particle.display(packet.getVictim().getLocation().clone().add(0, 1, 0));
            e.setCancelled(true);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();
        if (!(packet.getVictim() instanceof Player)) return;

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.NORMAL_PREFIX, getCategory(), "Curse");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;

        if (Utility.rollChance(10)) {
            packet.getVictim().getWorld().playSound(packet.getVictim().getLocation(), XSound.ITEM_SHIELD_BLOCK.parseSound(), 10.0F, 2.0F);
            particle.display(packet.getVictim().getLocation().clone().add(0, 1, 0));
            e.setCancelled(true);
        }
    }
}
