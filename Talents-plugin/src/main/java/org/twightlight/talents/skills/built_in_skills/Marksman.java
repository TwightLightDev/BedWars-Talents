package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleCrit;
import hm.zelha.particlesfx.particles.ParticleFirework;
import hm.zelha.particlesfx.particles.ParticleFlame;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class Marksman extends Skill {

    private String consecutiveHitsMetadataValue = "skill.marksman.hits";
    private String taskMetadataValue = "skill.marksman.task";
    private String damageLayerName = "marksmanLayer";

    private ParticleDustColored marksRed;
    private ParticleDustColored marksOrange;
    private ParticleDustColored marksYellow;
    private ParticleCrit critParticle;
    private ParticleFirework firework;
    private ParticleFlame flame;

    public Marksman(String id, List<Integer> costList) {
        super(id, costList);

        marksRed = new ParticleDustColored();
        marksRed.setColor(new hm.zelha.particlesfx.util.Color(230, 50, 30));
        marksRed.setCount(1);
        marksRed.setOffset(0, 0, 0);

        marksOrange = new ParticleDustColored();
        marksOrange.setColor(new hm.zelha.particlesfx.util.Color(255, 160, 40));
        marksOrange.setCount(1);
        marksOrange.setOffset(0, 0, 0);

        marksYellow = new ParticleDustColored();
        marksYellow.setColor(new hm.zelha.particlesfx.util.Color(255, 230, 60));
        marksYellow.setCount(1);
        marksYellow.setOffset(0, 0, 0);

        critParticle = new ParticleCrit();
        critParticle.setCount(6);
        critParticle.setOffset(0.3, 0.4, 0.3);
        critParticle.setSpeed(0.4);

        firework = new ParticleFirework();
        firework.setCount(4);
        firework.setOffset(0.2, 0.2, 0.2);
        firework.setSpeed(0.1);

        flame = new ParticleFlame();
        flame.setCount(1);
        flame.setOffset(0, 0, 0);
        flame.setSpeed(0);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;

        Player attacker = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(attacker.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Initialize
        if (!user.hasMetadata(consecutiveHitsMetadataValue) || !(user.getMetadataValue(consecutiveHitsMetadataValue) instanceof Integer)) {
            user.setMetadata(consecutiveHitsMetadataValue, 0);
        }

        // Cancel previous timeout
        if (user.getMetadataValue(taskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(taskMetadataValue)).cancel();
        }

        int hits = (Integer) user.getMetadataValue(consecutiveHitsMetadataValue);
        hits = Math.min(hits + 1, 5);
        user.setMetadata(consecutiveHitsMetadataValue, hits);

        // === Stacking ranged damage multiplier ===
        double multiplier = 0.006D * level * hits;
        DamageProperty property = e.getDamagePacket().getDamageProperty();
        if (!property.hasLayer(damageLayerName)) {
            property.addLayer(damageLayerName, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
            property.addValueToLayer(damageLayerName, 1);
        }
        property.addValueToLayer(damageLayerName, multiplier);

        // === Penetration buff at 3+ hits ===
        if (hits >= 3) {
            IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(attacker);
            if (arena != null) {
                Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
                StatsMap statsMap = arena1.getStatsMapOfUUID(user.getUuid());
                if (statsMap != null) {
                    double penBuff = 0.15D * level; // 3.0% at level 20
                    statsMap.getStatContainer("PENETRATION_RATIO").add(penBuff);
                    Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                        statsMap.getStatContainer("PENETRATION_RATIO").subtract(penBuff);
                    }, 60L);
                }
            }
        }

        // Visual on victim
        playMarksmanHit(e.getDamagePacket().getVictim(), hits);

        // Sound: click escalation
        attacker.playSound(attacker.getLocation(), XSound.ENTITY_ARROW_HIT_PLAYER.parseSound(),
                0.6F, 0.9F + hits * 0.2F);

        // === 5th hit: Precision Shot burst ===
        if (hits >= 5) {
            user.setMetadata(consecutiveHitsMetadataValue, 0);

            playPrecisionShotBurst(e.getDamagePacket().getVictim());
            attacker.getWorld().playSound(attacker.getLocation(), XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound(), 1.5F, 1.5F);
            e.getDamagePacket().getVictim().getWorld().playSound(e.getDamagePacket().getVictim().getLocation(),
                    XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound(), 1.2F, 1.8F);

            attacker.sendMessage("§c§l[Marksman] §6⚡ Precision Shot! §7(+" +
                    String.format("%.0f", multiplier * 100) + "% ranged damage)");
        }

        // Timeout: stacks reset after 5 seconds of no ranged hits
        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            user.setMetadata(consecutiveHitsMetadataValue, 0);
        }, 100L);
        user.setMetadata(taskMetadataValue, timeoutTask);
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Marksman hit: a target reticle that appears on the victim.
     * Starts as a simple dot, grows into a full crosshair at higher stacks.
     */
    private void playMarksmanHit(Entity victim, int hits) {
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 4 || (victim instanceof Player && !((Player) victim).isOnline())) {
                    cancel();
                    return;
                }

                Location center = victim.getLocation().add(0, 1.0, 0);

                // Crosshair arms: 4 directions, length grows with hit count
                double armLength = 0.15D + hits * 0.1D;
                double gap = 0.15D;

                // North-South-East-West arms
                for (int arm = 0; arm < 4; arm++) {
                    double angle = Math.toRadians(90.0 * arm);
                    int segments = 1 + hits;
                    for (int s = 0; s < segments; s++) {
                        double dist = gap + (armLength / segments) * (s + 1);
                        double x = Math.cos(angle) * dist;
                        double z = Math.sin(angle) * dist;

                        Location pLoc = center.clone().add(x, 0, z);

                        if (hits <= 2) {
                            marksRed.display(pLoc);
                        } else if (hits <= 4) {
                            marksOrange.display(pLoc);
                        } else {
                            marksYellow.display(pLoc);
                        }
                    }
                }

                // At 3+ hits, add a ring connecting the arms
                if (hits >= 3) {
                    double ringRadius = gap + armLength * 0.6D;
                    int ringPoints = 8;
                    for (int i = 0; i < ringPoints; i++) {
                        double angle = Math.toRadians((360.0 / ringPoints) * i + tick * 15);
                        double x = Math.cos(angle) * ringRadius;
                        double z = Math.sin(angle) * ringRadius;
                        marksRed.display(center.clone().add(x, 0, z));
                    }
                }

                // At 5 hits, center flame dot
                if (hits >= 5) {
                    flame.display(center);
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 2L);
    }

    /**
     * Precision shot burst: a converging ring of fireworks + expanding crit explosion.
     */
    private void playPrecisionShotBurst(Entity victim) {
        Location center = victim.getLocation().add(0, 1.0, 0);

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 8) {
                    cancel();
                    return;
                }

                if (tick < 4) {
                    // Converging ring
                    double radius = 1.5D - tick * 0.35D;
                    int points = 12;
                    for (int i = 0; i < points; i++) {
                        double angle = Math.toRadians((360.0 / points) * i + tick * 40);
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        marksYellow.display(center.clone().add(x, 0, z));
                        marksOrange.display(center.clone().add(x, 0.3, z));
                    }
                } else {
                    // Expanding crit + firework burst
                    double expandRadius = (tick - 4) * 0.4D;

                    critParticle.display(center);

                    for (int i = 0; i < 6; i++) {
                        double angle = Math.toRadians(60.0 * i + tick * 25);
                        double x = Math.cos(angle) * expandRadius;
                        double z = Math.sin(angle) * expandRadius;
                        marksRed.display(center.clone().add(x, 0, z));
                        marksYellow.display(center.clone().add(x, 0.2, z));
                    }

                    if (tick == 4) {
                        firework.display(center);
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}
