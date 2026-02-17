package org.twightlight.talents.skills.built_in_skills;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;

public class Reinforcement extends Skill {

    private String currentPointsMetadataValue = "skill.reinforcement.currentPoints";
    private String taskMetadataValue = "skill.reinforcement.task";
    private String lastStatsMapMetadataValue = "skill.reinforcement.lastStatsMap";
    private String damageLayerName = "reinforcementLayer";

    public Reinforcement(String id, List<Integer> costList) {
        super(id, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player p = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            handleReinforcement(user, level, e.getDamagePacket().getVictimStatsMap(), e.getDamagePacket().getDamageProperty());
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player p = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            handleReinforcement(user, level, e.getDamagePacket().getVictimStatsMap(), e.getDamagePacket().getDamageProperty());
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onUndefinedAttack(UndefinedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player p = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            handleReinforcement(user, level, e.getDamagePacket().getVictimStatsMap(), e.getDamagePacket().getDamageProperty());
        }
    }

    private void handleReinforcement(User user, int level, StatsMap statsMap, DamageProperty property) {
        if (statsMap == null) return;

        // Initialize metadata
        if (!user.hasMetadata(currentPointsMetadataValue) || !(user.getMetadataValue(currentPointsMetadataValue) instanceof Integer)) {
            user.setMetadata(currentPointsMetadataValue, 0);
        }

        // Cancel previous task
        if (user.getMetadataValue(taskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(taskMetadataValue)).cancel();
        }

        int currentPoints = (Integer) user.getMetadataValue(currentPointsMetadataValue);

        // Remove previous stats modification
        modifyAttribute(user, level, currentPoints, statsMap, false);

        // Schedule reset task
        BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            int points = (Integer) user.getMetadataValue(currentPointsMetadataValue);
            modifyAttribute(user, level, points, statsMap, false);
            user.setMetadata(currentPointsMetadataValue, 0);
        }, 100L);

        user.setMetadata(taskMetadataValue, task);

        // Increment points (max 4)
        if (currentPoints < 4) {
            currentPoints++;
            user.setMetadata(currentPointsMetadataValue, currentPoints);
        }

        // Apply new stats modification
        modifyAttribute(user, level, currentPoints, statsMap, true);

        // Apply direct damage reduction via layer
        double damageReduction = 0.007D * (double) level * (double) currentPoints;
        if (!property.hasLayer(damageLayerName)) {
            property.addLayer(damageLayerName, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
            property.addValueToLayer(damageLayerName, 1);
        }
        // Reduce damage by subtracting from multiplier
        property.addValueToLayer(damageLayerName, -damageReduction);
    }

    private void modifyAttribute(User user, int level, int currentPoints, StatsMap statsMap, boolean add) {
        if (statsMap == null || currentPoints == 0) return;

        double damageReduction = 0.15D * (double) currentPoints * (double) level;

        if (add) {
            statsMap.getStatContainer("DAMAGE_REDUCTION").add(damageReduction);
        } else {
            statsMap.getStatContainer("DAMAGE_REDUCTION").subtract(damageReduction);
        }
    }
}

