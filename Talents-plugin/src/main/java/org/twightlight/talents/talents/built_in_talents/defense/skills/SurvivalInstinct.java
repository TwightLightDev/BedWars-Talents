package org.twightlight.talents.talents.built_in_talents.defense.skills;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.api.packets.UndefinedDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SurvivalInstinct extends Talent {
    public Map<UUID, Double> lastvalues;
    public Map<UUID, BukkitTask> lastTask;

    public SurvivalInstinct(String talentId, List<Integer> costList) {
        super(talentId, costList);
        lastvalues = new HashMap<>();
        lastTask = new HashMap<>();
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        Player victim = e.getDamagePacket().getVictimAsPlayer();
        double lost = 1 - victim.getHealth()/victim.getMaxHealth();
        if (lost >= 0.5) {
            double multiplier = lost - 0.5;
            User user = User.getUserFromUUID(victim.getUniqueId());

            Integer level = user.getTalentLevel(getTalentId());
            if (level == 0) return;

            if (lastvalues.containsKey(victim.getUniqueId())) {
                v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                lastTask.get(victim.getUniqueId()).cancel();
                lastvalues.remove(victim.getUniqueId());
                lastTask.remove(victim.getUniqueId());
            }

            double addition = multiplier * level * 2;
            v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).add(addition);
            BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                if (lastvalues.containsKey(victim.getUniqueId())) {
                    v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                    lastvalues.remove(victim.getUniqueId());
                }
                lastTask.remove(victim.getUniqueId());
            }, 40L);
            lastvalues.put(victim.getUniqueId(), addition);
            lastTask.put(victim.getUniqueId(), task);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        Player victim = e.getDamagePacket().getVictimAsPlayer();
        double lost = 1 - victim.getHealth()/victim.getMaxHealth();
        if (lost >= 0.5) {
            double multiplier = lost - 0.5;
            User user = User.getUserFromUUID(victim.getUniqueId());

            Integer level = user.getTalentLevel(getTalentId());

            if (lastvalues.containsKey(victim.getUniqueId())) {
                v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                lastTask.get(victim.getUniqueId()).cancel();
                lastvalues.remove(victim.getUniqueId());
                lastTask.remove(victim.getUniqueId());
            }

            double addition = multiplier * level * 2;
            v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).add(addition);
            BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                if (lastvalues.containsKey(victim.getUniqueId())) {
                    v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                    lastvalues.remove(victim.getUniqueId());
                }
                lastTask.remove(victim.getUniqueId());
            }, 40L);
            lastvalues.put(victim.getUniqueId(), addition);
            lastTask.put(victim.getUniqueId(), task);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onUndefinedAttack(UndefinedDamageEvent e) {
        UndefinedDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        Player victim = e.getDamagePacket().getVictimAsPlayer();
        double lost = 1 - victim.getHealth()/victim.getMaxHealth();
        if (lost >= 0.5) {
            double multiplier = lost - 0.5;
            User user = User.getUserFromUUID(victim.getUniqueId());

            Integer level = user.getTalentLevel(getTalentId());
            if (level == 0) return;

            if (lastvalues.containsKey(victim.getUniqueId())) {
                v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                lastTask.get(victim.getUniqueId()).cancel();
                lastvalues.remove(victim.getUniqueId());
                lastTask.remove(victim.getUniqueId());
            }

            double addition = multiplier * level * 2;
            v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).add(addition);
            BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                if (lastvalues.containsKey(victim.getUniqueId())) {
                    v.getStatContainer(BaseStats.DAMAGE_REDUCTION.name()).subtract(lastvalues.get(victim.getUniqueId()));
                    lastvalues.remove(victim.getUniqueId());
                }
                lastTask.remove(victim.getUniqueId());
            }, 40L);
            lastvalues.put(victim.getUniqueId(), addition);
            lastTask.put(victim.getUniqueId(), task);
        }
    }
}


