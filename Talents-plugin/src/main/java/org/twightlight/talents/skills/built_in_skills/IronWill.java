package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleFirework;
import hm.zelha.particlesfx.particles.ParticleCrit;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

public class IronWill extends Skill {

    private String fortifyStacksMetadataValue = "skill.ironWill.stacks";
    private String taskMetadataValue = "skill.ironWill.task";
    private String lastStatsMetadataValue = "skill.ironWill.lastStats";
    private String damageLayerName = "ironWillLayer";

    private ParticleDustColored shieldOrange;
    private ParticleDustColored shieldGold;
    private ParticleDustColored shieldWhite;
    private ParticleFirework firework;
    private ParticleCrit critParticle;

    public IronWill(String id, List<Integer> costList) {
        super(id, costList);

        shieldOrange = new ParticleDustColored();
        shieldOrange.setColor(new hm.zelha.particlesfx.util.Color(255, 150, 30));
        shieldOrange.setCount(1);
        shieldOrange.setOffset(0, 0, 0);

        shieldGold = new ParticleDustColored();
        shieldGold.setColor(new hm.zelha.particlesfx.util.Color(255, 215, 0));
        shieldGold.setCount(1);
        shieldGold.setOffset(0, 0, 0);

        shieldWhite = new ParticleDustColored();
        shieldWhite.setColor(new hm.zelha.particlesfx.util.Color(255, 250, 230));
        shieldWhite.setCount(1);
        shieldWhite.setOffset(0, 0, 0);

        firework = new ParticleFirework();
        firework.setCount(2);
        firework.setOffset(0.1, 0.1, 0.1);
        firework.setSpeed(0.05);

        critParticle = new ParticleCrit();
        critParticle.setCount(5);
        critParticle.setOffset(0.3, 0.4, 0.3);
        critParticle.setSpeed(0.3);
    }

    // ==================== WHEN THE USER GETS HIT (MELEE) ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeDamage(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        Player attacker = e.getDamagePacket().getAttacker();
        handleFortify(victim, user, level, e.getDamagePacket().getDamageProperty(), attacker);
    }

    // ==================== WHEN THE USER GETS HIT (RANGED) ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedDamage(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        handleFortify(victim, user, level, e.getDamagePacket().getDamageProperty(), null);
    }

    // ==================== CORE LOGIC ====================

    private void handleFortify(Player victim, User user, int level, DamageProperty property, Player meleeAttacker) {
        IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(victim);
        if (arena == null) return;
        Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
        StatsMap statsMap = arena1.getStatsMapOfUUID(user.getUuid());
        if (statsMap == null) return;

        // Initialize
        if (!user.hasMetadata(fortifyStacksMetadataValue) || !(user.getMetadataValue(fortifyStacksMetadataValue) instanceof Integer)) {
            user.setMetadata(fortifyStacksMetadataValue, 0);
        }

        // Cancel previous timeout
        if (user.getMetadataValue(taskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(taskMetadataValue)).cancel();
        }

        int stacks = (Integer) user.getMetadataValue(fortifyStacksMetadataValue);

        // Remove old stat buffs
        removeStats(statsMap, level, stacks);

        // Build stacks (max 6)
        if (stacks < 6) {
            stacks++;
        }
        user.setMetadata(fortifyStacksMetadataValue, stacks);

        // Apply new stat buffs
        applyStats(statsMap, level, stacks);

        // Per-hit damage reduction layer
        double layerReduction = 0.0015D * level * stacks; // at lv20, 6 stacks = 0.72 (72% from layer)
        if (!property.hasLayer(damageLayerName)) {
            property.addLayer(damageLayerName, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
            property.addValueToLayer(damageLayerName, 1);
        }
        property.addValueToLayer(damageLayerName, -layerReduction);

        // Visual: shield ring pulse
        playShieldPulse(victim, stacks);

        // Sound: metallic clang, pitch rises with stacks
        victim.getWorld().playSound(victim.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(),
                0.6F, 0.8F + stacks * 0.15F);

        // ==================== THORNS REFLECTION on melee attacker ====================
        if (meleeAttacker != null && stacks >= 3 && Utility.rollChance(45)) {
            double thornsDamage = 0.03D * level * stacks; // at lv20, 6 stacks = 3.6
            double thornsCap = 4.0D;
            double finalThorns = Math.min(thornsDamage, thornsCap);

            CombatUtils.dealUndefinedDamage(meleeAttacker, finalThorns,
                    EntityDamageEvent.DamageCause.THORNS,
                    Map.of("ironWillThorns", true));

            // Thorns visual: sparks flying back toward attacker
            playThornsReflect(victim, meleeAttacker, stacks);

            meleeAttacker.getWorld().playSound(meleeAttacker.getLocation(),
                    XSound.BLOCK_ANVIL_LAND.parseSound(), 0.5F, 2.0F);

            if (stacks >= 6) {
                victim.sendMessage("§6§l[Iron Will] §fMax Fortify! §7Thorns: §e" + String.format("%.1f", finalThorns));
            }
        }

        // Timeout: stacks decay to 0 after 5 seconds of no hits
        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            int s = (Integer) user.getMetadataValue(fortifyStacksMetadataValue);
            removeStats(statsMap, level, s);
            user.setMetadata(fortifyStacksMetadataValue, 0);
        }, 100L);
        user.setMetadata(taskMetadataValue, timeoutTask);
    }

    // ==================== STATS ====================

    private void applyStats(StatsMap statsMap, int level, int stacks) {
        if (stacks == 0) return;
        // DR: 0.15 * level * stacks → at lv20, 6 stacks = 18.0
        double dr = 0.15D * level * stacks;
        statsMap.getStatContainer("DAMAGE_REDUCTION").add(dr);
    }

    private void removeStats(StatsMap statsMap, int level, int stacks) {
        if (stacks == 0) return;
        double dr = 0.15D * level * stacks;
        statsMap.getStatContainer("DAMAGE_REDUCTION").subtract(dr);
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Shield pulse: a flat rotating shield ring at chest height that gets more segments with stacks.
     */
    private void playShieldPulse(Entity entity, int stacks) {
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 5 || (entity instanceof Player && !((Player) entity).isOnline())) {
                    cancel();
                    return;
                }

                double radius = 0.7D + tick * 0.06D;
                double y = 0.9D;
                int points = 4 + stacks * 2; // 16 points at max stacks

                for (int i = 0; i < points; i++) {
                    double angle = Math.toRadians((360.0 / points) * i + tick * 25);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location loc = entity.getLocation().clone().add(x, y, z);

                    // Color depends on stack tier
                    if (stacks <= 2) {
                        shieldOrange.display(loc);
                    } else if (stacks <= 4) {
                        shieldGold.display(loc);
                    } else {
                        shieldWhite.display(loc);
                    }
                }

                // At high stacks, add a second ring at a different height
                if (stacks >= 4) {
                    double y2 = 1.5D;
                    double radius2 = radius * 0.7D;
                    for (int i = 0; i < points / 2; i++) {
                        double angle = Math.toRadians((360.0 / (points / 2)) * i - tick * 35);
                        double x = Math.cos(angle) * radius2;
                        double z = Math.sin(angle) * radius2;
                        Location loc = entity.getLocation().clone().add(x, y2, z);
                        shieldGold.display(loc);
                    }
                }

                // At max stacks, firework sparkle
                if (stacks >= 6 && tick == 0) {
                    firework.display(entity.getLocation().clone().add(0, 1.2, 0));
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 2L);
    }

    /**
     * Thorns reflect: crits and gold sparks travel from victim back toward attacker in an arc.
     */
    private void playThornsReflect(Player victim, Player attacker, int stacks) {
        Location from = victim.getLocation().add(0, 1.0, 0);
        Location to = attacker.getLocation().add(0, 1.0, 0);

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 5) {
                    cancel();
                    return;
                }

                double progress = (double) (tick + 1) / 5;
                Location point = from.clone().add(
                        to.clone().subtract(from).toVector().multiply(progress));
                // Arc upward
                point.add(0, Math.sin(progress * Math.PI) * 0.8D, 0);

                shieldGold.display(point);
                if (stacks >= 5) {
                    shieldWhite.display(point.clone().add(0, 0.15, 0));
                }

                // Crit sparkle at the point
                critParticle.display(point);

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}
