package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleNote;
import hm.zelha.particlesfx.particles.ParticleCritMagic;
import hm.zelha.particlesfx.particles.ParticleSweepAttack;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

/**
 * Resonance — Rhythm-based melee combat skill.
 *
 * Tracks the timing between consecutive melee hits. If the player
 * maintains a consistent rhythm (hitting within an ideal timing window),
 * they build "Resonance" stacks (max 6). Hitting too fast (spam clicking)
 * or too slow (pausing too long) breaks the rhythm and loses stacks.
 *
 * Each stack adds a scaling damage multiplier layer to the hit.
 * At 4+ stacks, each hit also releases a "Harmonic Shockwave" — an
 * AoE undefined damage burst to nearby enemies.
 * At max stacks (6), the shockwave gains bonus damage and applies slowness.
 *
 * Encourages deliberate, paced combat over spam-clicking.
 *
 * Visual: Musical note particles on each rhythmic hit, expanding
 * concentric hexagonal rings on shockwave trigger, with golden
 * enchanted crit sparkles during high resonance.
 */
public class Resonance extends Skill {

    // Metadata keys
    private String stacksMetadataValue = "skill.resonance.stacks";
    private String lastHitTimeMetadataValue = "skill.resonance.lastHitTime";
    private String decayTaskMetadataValue = "skill.resonance.decayTask";
    private String damageLayerName = "resonanceLayer";
    private String shockwaveLayerName = "resonanceShockwaveLayer";

    // Timing window (in milliseconds)
    // Ideal rhythm: 400-700ms between hits (~1.4 to 2.5 hits/sec)
    // Minecraft default attack speed is ~10 CPS at spam, ~2-3 CPS for deliberate hits
    private static final long RHYTHM_MIN_MS = 400L;  // faster than this = too fast
    private static final long RHYTHM_MAX_MS = 700L;  // slower than this = too slow
    private static final long RHYTHM_BREAK_MS = 1200L; // longer than this = lose all stacks
    private static final int MAX_STACKS = 6;
    private static final long DECAY_TIMEOUT = 100L; // 5 seconds no hits = stacks decay

    // Shockwave constants
    private static final double SHOCKWAVE_RADIUS = 2.5D;
    private static final int SHOCKWAVE_STACK_THRESHOLD = 4;

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Pre-created particles
    private ParticleNote noteParticle;
    private ParticleDustColored goldDust;
    private ParticleDustColored whiteDust;
    private ParticleDustColored orangeDust;
    private ParticleCritMagic critSparkle;
    private ParticleSweepAttack sweepParticle;

    public Resonance(String id, List<Integer> costList) {
        super(id, costList);

        // Musical note — shown on each rhythmic hit
        noteParticle = new ParticleNote();
        noteParticle.setCount(3);
        noteParticle.setOffset(0.4, 0.3, 0.4);
        noteParticle.setSpeed(1);

        // Gold dust — hexagon ring color
        goldDust = new ParticleDustColored();
        goldDust.setColor(new hm.zelha.particlesfx.util.Color(255, 215, 0)); // gold
        goldDust.setCount(1);
        goldDust.setOffset(0, 0, 0);
        goldDust.setSpeed(0);

        // White dust — inner hexagon ring
        whiteDust = new ParticleDustColored();
        whiteDust.setColor(new hm.zelha.particlesfx.util.Color(255, 255, 255));
        whiteDust.setCount(1);
        whiteDust.setOffset(0, 0, 0);
        whiteDust.setSpeed(0);

        // Orange dust — outer hexagon ring at max stacks
        orangeDust = new ParticleDustColored();
        orangeDust.setColor(new hm.zelha.particlesfx.util.Color(255, 140, 0)); // dark orange
        orangeDust.setCount(1);
        orangeDust.setOffset(0, 0, 0);
        orangeDust.setSpeed(0);

        // Enchanted crit sparkle — ambient high-resonance effect
        critSparkle = new ParticleCritMagic();
        critSparkle.setCount(5);
        critSparkle.setOffset(0.5, 0.8, 0.5);
        critSparkle.setSpeed(0.3);

        // Sweep attack — shockwave center
        sweepParticle = new ParticleSweepAttack();
        sweepParticle.setCount(3);
        sweepParticle.setOffset(0.3, 0.1, 0.3);
        sweepParticle.setSpeed(0);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // ---- Rhythm Detection ----

        long now = System.currentTimeMillis();

        // Initialize metadata
        if (!user.hasMetadata(stacksMetadataValue) || !(user.getMetadataValue(stacksMetadataValue) instanceof Integer)) {
            user.setMetadata(stacksMetadataValue, 0);
        }
        if (!user.hasMetadata(lastHitTimeMetadataValue) || !(user.getMetadataValue(lastHitTimeMetadataValue) instanceof Long)) {
            user.setMetadata(lastHitTimeMetadataValue, now);
            return; // First hit — just record time, no rhythm check
        }

        long lastHitTime = (Long) user.getMetadataValue(lastHitTimeMetadataValue);
        long interval = now - lastHitTime;
        user.setMetadata(lastHitTimeMetadataValue, now);

        int currentStacks = (Integer) user.getMetadataValue(stacksMetadataValue);
        int previousStacks = currentStacks;

        if (interval >= RHYTHM_BREAK_MS) {
            // Way too slow — reset entirely
            currentStacks = 0;
            p.sendMessage("§6§l[Resonance] §cRhythm broken! §8(too slow)");
            p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_BASS.parseSound(), 0.6F, 0.5F);
        } else if (interval >= RHYTHM_MIN_MS && interval <= RHYTHM_MAX_MS) {
            // Perfect rhythm — gain a stack
            if (currentStacks < MAX_STACKS) {
                currentStacks++;
            }
            // Level 20 bonus: 30% chance to gain an extra stack
            if (level == 20 && currentStacks < MAX_STACKS && Utility.rollChance(30.0D)) {
                currentStacks++;
            }

            float pitch = 0.6F + currentStacks * 0.25F;
            p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_HARP.parseSound(), 0.8F, pitch);

            // Note particle on victim
            if (e.getDamagePacket().getVictim() != null) {
                noteParticle.display(e.getDamagePacket().getVictim().getLocation().clone().add(0, 1.5, 0));
            }

            if (currentStacks > previousStacks) {
                p.sendMessage("§6§l[Resonance] §e♪ §7Harmony x" + currentStacks +
                        " §8(" + interval + "ms)");
            }
        } else if (interval < RHYTHM_MIN_MS) {
            // Too fast — lose a stack
            currentStacks = Math.max(0, currentStacks - 1);
            if (currentStacks < previousStacks) {
                p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_BASS.parseSound(), 0.5F, 1.0F);
                p.sendMessage("§6§l[Resonance] §cOff-beat! §7x" + currentStacks + " §8(too fast: " + interval + "ms)");
            }
        } else {
            // Between RHYTHM_MAX_MS and RHYTHM_BREAK_MS — slightly too slow, lose a stack
            currentStacks = Math.max(0, currentStacks - 1);
            if (currentStacks < previousStacks) {
                p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_BASS.parseSound(), 0.5F, 0.7F);
                p.sendMessage("§6§l[Resonance] §cOff-beat! §7x" + currentStacks + " §8(too slow: " + interval + "ms)");
            }
        }

        user.setMetadata(stacksMetadataValue, currentStacks);

        // ---- Cancel previous decay task, schedule new one ----
        if (user.getMetadataValue(decayTaskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(decayTaskMetadataValue)).cancel();
        }
        BukkitTask decayTask = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            User u = User.getUserFromUUID(p.getUniqueId());
            if (u != null) {
                u.setMetadata(stacksMetadataValue, 0);
                if (p.isOnline()) {
                    p.sendMessage("§6§l[Resonance] §8Harmony faded...");
                    p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_BASS.parseSound(), 0.4F, 0.3F);
                }
            }
        }, DECAY_TIMEOUT);
        user.setMetadata(decayTaskMetadataValue, decayTask);

        // ---- Apply per-hit damage layer ----
        if (currentStacks > 0) {
            double damageMultiplier = calculateDamageMultiplier(level, currentStacks);
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            if (!property.hasLayer(damageLayerName)) {
                property.addLayer(damageLayerName, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer(damageLayerName, 1);
            }
            property.addValueToLayer(damageLayerName, damageMultiplier);

            // Ambient enchanted crit sparkle at 3+ stacks
            if (currentStacks >= 3) {
                critSparkle.display(p.getLocation().clone().add(0, 1.2, 0));
            }
        }

        // ---- Shockwave at threshold ----
        if (currentStacks >= SHOCKWAVE_STACK_THRESHOLD) {
            Entity victim = e.getDamagePacket().getVictim();
            if (victim != null) {
                double shockwaveDamage = calculateShockwaveDamage(level, currentStacks);
                boolean isMaxStacks = currentStacks >= MAX_STACKS;

                triggerShockwave(p, victim.getLocation(), level, shockwaveDamage, isMaxStacks);
            }
        }
    }

    // ==================== BALANCE CALCULATIONS ====================

    /**
     * Per-hit damage multiplier layer.
     * Formula: 0.008 * level * stacks
     *
     * Level 1,  1 stack:  0.008 * 1 * 1   = 0.008  → +0.8%
     * Level 10, 3 stacks: 0.008 * 10 * 3  = 0.24   → +24%
     * Level 15, 5 stacks: 0.008 * 15 * 5  = 0.60   → +60%
     * Level 20, 6 stacks: 0.008 * 20 * 6  = 0.96   → +96% damage
     *
     * This is powerful but requires maintaining perfect rhythm — hard to
     * sustain 6 stacks in a real fight with knockback and re-engagement.
     */
    private double calculateDamageMultiplier(int level, int stacks) {
        return 0.008D * (double) level * (double) stacks;
    }

    /**
     * Shockwave damage — flat undefined damage dealt to nearby enemies.
     * Formula: 0.5 + 0.05 * level + 0.15 * stacks
     *
     * Level 1,  4 stacks: 0.5 + 0.05 + 0.6  = 1.15
     * Level 10, 4 stacks: 0.5 + 0.5 + 0.6   = 1.6
     * Level 20, 6 stacks: 0.5 + 1.0 + 0.9   = 2.4
     * Max stacks bonus:   2.4 * 1.5          = 3.6 (at max stacks)
     *
     * Modest — it's AoE and fires on every hit at 4+ stacks, so
     * individual damage must be low.
     */
    private double calculateShockwaveDamage(int level, int stacks) {
        double base = 0.5D + 0.05D * (double) level + 0.15D * (double) stacks;
        if (stacks >= MAX_STACKS) {
            base *= 1.5D; // max stack bonus
        }
        return Math.min(base, 5.0D); // hard cap at 5.0
    }

    // ==================== SHOCKWAVE LOGIC ====================

    private void triggerShockwave(Player attacker, Location center, int level, double damage, boolean isMax) {
        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;
        ITeam attackerTeam = arena.getTeam(attacker);

        Set<String> skipLayers = Set.of("reductionLayer1", "blockLayer", "KOBW_Defense",
                "weaknessLayer", "ironWillLayer",
                "riftwalkerDefenseLayer");
        Map<String, Object> metadata = Map.of("resonance-shockwave", true);

        for (Entity entity : center.getWorld().getNearbyEntities(center, SHOCKWAVE_RADIUS, SHOCKWAVE_RADIUS, SHOCKWAVE_RADIUS)) {
            if (entity == attacker) continue;
            if (!(entity instanceof LivingEntity)) continue;

            if (entity instanceof Player) {
                Player target = (Player) entity;
                User targetUser = User.getUserFromUUID(target.getUniqueId());
                if (targetUser == null) continue;

                IArena targetArena = util.getArenaByPlayer(target);
                if (targetArena == null) continue;
                if (targetArena.getTeam(target) == attackerTeam) continue;

                CombatUtils.dealUndefinedDamage(target, damage,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK, metadata, skipLayers);

                if (isMax) {
                    target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SLOW, 20, 0, false, false));
                }
            }
        }

        // Sound
        center.getWorld().playSound(center, XSound.BLOCK_NOTE_BLOCK_CHIME.parseSound(), 2.0F, 1.8F);
        if (isMax) {
            center.getWorld().playSound(center, XSound.BLOCK_NOTE_BLOCK_BELL.parseSound(), 2.0F, 2.0F);
            center.getWorld().playSound(center, XSound.ENTITY_PLAYER_ATTACK_CRIT.parseSound(), 1.5F, 1.5F);
        }

        // Visual — expanding hexagonal rings
        playShockwaveHexagons(center, isMax);
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Expanding concentric hexagonal rings.
     *
     * A hexagon is drawn at the center, then it expands outward over
     * several ticks. Two rings — an inner gold one and an outer white one —
     * expand at different rates, creating a layered shockwave look.
     * At max stacks, a third orange ring is added.
     *
     * Each hexagon is drawn by iterating over 6 vertices and interpolating
     * particles along each edge.
     */
    private void playShockwaveHexagons(final Location center, final boolean isMax) {
        new BukkitRunnable() {
            int tick = 0;
            final int duration = 8;

            public void run() {
                if (tick >= duration) {
                    cancel();
                    return;
                }

                double progress = (double) tick / (double) duration;

                // Inner ring — gold, expands from 0.3 to 1.5
                double innerRadius = 0.3D + progress * 1.2D;
                drawHexagon(center, innerRadius, tick * 5.0, 0.3 + progress * 0.3, goldDust, 3);

                // Outer ring — white, expands from 0.8 to 2.5
                double outerRadius = 0.8D + progress * 1.7D;
                drawHexagon(center, outerRadius, -(tick * 5.0), 0.15 + progress * 0.2, whiteDust, 3);

                // Max stack bonus ring — orange, expands fastest
                if (isMax) {
                    double maxRadius = 1.0D + progress * 2.2D;
                    drawHexagon(center, maxRadius, tick * 8.0, 0.1 + progress * 0.15, orangeDust, 2);
                }

                // Sweep particle at center for impact feel
                if (tick == 0) {
                    sweepParticle.display(center.clone().add(0, 0.3, 0));
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 2L);
    }

    /**
     * Draws a single hexagon (6 sides) at the given location.
     *
     * @param center     Center location of the hexagon
     * @param radius     Distance from center to each vertex
     * @param rotOffset  Rotation offset in degrees (for spinning effect)
     * @param yOffset    Height above ground
     * @param particle   Particle to display
     * @param segments   Number of interpolation points per edge
     */
    private void drawHexagon(Location center, double radius, double rotOffset, double yOffset,
                             ParticleDustColored particle, int segments) {
        int sides = 6;
        for (int i = 0; i < sides; i++) {
            double angle1 = Math.toRadians(60.0 * i + rotOffset);
            double angle2 = Math.toRadians(60.0 * (i + 1) + rotOffset);

            for (int s = 0; s <= segments; s++) {
                double t = (double) s / (double) segments;
                double x = (Math.cos(angle1) * (1.0 - t) + Math.cos(angle2) * t) * radius;
                double z = (Math.sin(angle1) * (1.0 - t) + Math.sin(angle2) * t) * radius;
                particle.display(center.clone().add(x, yOffset, z));
            }
        }
    }
}

