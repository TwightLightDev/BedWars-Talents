package org.twightlight.talents.runes.categories.defense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.defense.DefenseRune;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Endurance extends DefenseRune {
    public Map<UUID, Double> lastvalues;
    public Map<UUID, Double> lastvalues1;
    public Map<UUID, BukkitTask> lastTask;

    public Endurance() {
        super(64);
        lastvalues = new HashMap<>();
        lastTask = new HashMap<>();
        lastvalues1 = new HashMap<>();
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).add(1.6 * amount);
            map.getStatContainer(BaseStats.CRITICAL_DAMAGE_REDUCTION.name()).add(10 * amount);
            map.getStatContainer("REGENERATION_PER_FIVE_SECONDS").add(amount * 0.7);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();
        if (!(packet.getVictim() instanceof Player)) return;

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.NORMAL_PREFIX, getCategory(), "Thorns");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        Player victim = e.getDamagePacket().getVictimAsPlayer();
        double lost = 1 - victim.getHealth()/victim.getMaxHealth();

        if (victim.isDead()) return;

        int multiplier = (int) Math.floor(lost * 50);
        if (lastvalues.containsKey(victim.getUniqueId())) {
            v.getStatContainer(BaseStats.CRITICAL_DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
            v.getStatContainer("REGENERATION_PER_FIVE_SECONDS").subtract(lastvalues1.get(victim.getUniqueId()));

            lastTask.get(victim.getUniqueId()).cancel();
            lastvalues.remove(victim.getUniqueId());
            lastvalues1.remove(victim.getUniqueId());
            lastTask.remove(victim.getUniqueId());
        }

        double addition = multiplier * 0.8;
        double addition1 = multiplier * 0.08;

        v.getStatContainer(BaseStats.CRITICAL_DAMAGE_REDUCTION.name()).add(addition);
        v.getStatContainer("REGENERATION_PER_FIVE_SECONDS").add(addition1);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            if (lastvalues.containsKey(victim.getUniqueId())) {
                v.getStatContainer(BaseStats.CRITICAL_DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                v.getStatContainer("REGENERATION_PER_FIVE_SECONDS").subtract(lastvalues1.get(victim.getUniqueId()));

                lastvalues.remove(victim.getUniqueId());
                lastvalues1.remove(victim.getUniqueId());
            }
            lastTask.remove(victim.getUniqueId());
        }, 40L);

        lastvalues.put(victim.getUniqueId(), addition);
        lastvalues1.put(victim.getUniqueId(), addition1);
        lastTask.put(victim.getUniqueId(), task);
        if (Utility.rollChance(10)) {
            double heal = victim.getMaxHealth() * lost * 0.1;

            victim.setHealth(Math.min(victim.getMaxHealth(), victim.getHealth() + Math.min(1.2, heal)));
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();
        if (!(packet.getVictim() instanceof Player)) return;

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.NORMAL_PREFIX, getCategory(), "Thorns");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        Player victim = e.getDamagePacket().getVictimAsPlayer();
        double lost = 1 - victim.getHealth()/victim.getMaxHealth();

        if (victim.isDead()) return;

        int multiplier = (int) Math.floor(lost * 50);
        if (lastvalues.containsKey(victim.getUniqueId())) {
            v.getStatContainer(BaseStats.CRITICAL_DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
            v.getStatContainer("REGENERATION_PER_FIVE_SECONDS").subtract(lastvalues1.get(victim.getUniqueId()));

            lastTask.get(victim.getUniqueId()).cancel();
            lastvalues.remove(victim.getUniqueId());
            lastvalues1.remove(victim.getUniqueId());
            lastTask.remove(victim.getUniqueId());
        }

        double addition = multiplier * 0.8;
        double addition1 = multiplier * 0.08;

        v.getStatContainer(BaseStats.CRITICAL_DAMAGE_REDUCTION.name()).add(addition);
        v.getStatContainer("REGENERATION_PER_FIVE_SECONDS").add(addition1);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            if (lastvalues.containsKey(victim.getUniqueId())) {
                v.getStatContainer(BaseStats.CRITICAL_DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                v.getStatContainer("REGENERATION_PER_FIVE_SECONDS").subtract(lastvalues1.get(victim.getUniqueId()));

                lastvalues.remove(victim.getUniqueId());
                lastvalues1.remove(victim.getUniqueId());
            }
            lastTask.remove(victim.getUniqueId());
        }, 40L);

        lastvalues.put(victim.getUniqueId(), addition);
        lastvalues1.put(victim.getUniqueId(), addition1);
        lastTask.put(victim.getUniqueId(), task);
        if (Utility.rollChance(10)) {
            double heal = victim.getMaxHealth() * lost * 0.1;

            victim.setHealth(Math.min(victim.getMaxHealth(), victim.getHealth() + Math.min(1.2, heal)));
        }
    }
}
