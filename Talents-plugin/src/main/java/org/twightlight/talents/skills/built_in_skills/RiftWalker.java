package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleEnchant;
import hm.zelha.particlesfx.particles.ParticlePortal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;

/**
 * Riftwalker — Positional awareness skill.
 *
 * Tracks the distance the player has moved between combat hits.
 * Moving further between engagements builds "Rift Energy" stacks (max 5).
 * Each stack grants penetration (offensive) and applies a damage reduction
 * layer when the player is hit (defensive).
 *
 * Encourages aggressive repositioning — bridging around opponents,
 * circling, jumping — rather than standing still and trading blows.
 *
 * Visual: A rotating double-helix of purple portal particles that
 * intensifies with stacks, plus an enchantment table letter burst
 * on stack gain.
 */
public class RiftWalker extends Skill {

    // Metadata keys
    private String stacksMetadataValue = "skill.riftwalker.stacks";
    private String lastLocationXMetadataValue = "skill.riftwalker.lastLocX";
    private String lastLocationYMetadataValue = "skill.riftwalker.lastLocY";
    private String lastLocationZMetadataValue = "skill.riftwalker.lastLocZ";
    private String lastWorldMetadataValue = "skill.riftwalker.lastWorld";
    private String decayTaskMetadataValue = "skill.riftwalker.decayTask";
    private String damageLayerName = "riftwalkerDefenseLayer";

    // Distance thresholds
    private static final double DISTANCE_PER_STACK = 4.0D; // 4 blocks of movement = +1 stack
    private static final int MAX_STACKS = 5;
    private static final long DECAY_TIMEOUT = 120L; // 6 seconds of no combat = stacks decay

    // Pre-created particles
    private ParticlePortal portalParticle;
    private ParticleDustColored helixParticle1;
    private ParticleDustColored helixParticle2;
    private ParticleEnchant burstParticle;

    public RiftWalker(String id, List<Integer> costList) {
        super(id, costList);

        // Portal particle for ambient helix
        portalParticle = new ParticlePortal();
        portalParticle.setCount(1);
        portalParticle.setOffset(0, 0, 0);
        portalParticle.setSpeed(0.05);

        // Purple helix strand
        helixParticle1 = new ParticleDustColored();
        helixParticle1.setColor(new hm.zelha.particlesfx.util.Color(160, 32, 240)); // purple
        helixParticle1.setCount(1);
        helixParticle1.setOffset(0, 0, 0);
        helixParticle1.setSpeed(0);

        // Cyan helix strand
        helixParticle2 = new ParticleDustColored();
        helixParticle2.setColor(new hm.zelha.particlesfx.util.Color(0, 255, 200)); // cyan-teal
        helixParticle2.setCount(1);
        helixParticle2.setOffset(0, 0, 0);
        helixParticle2.setSpeed(0);

        // Enchantment burst on stack gain
        burstParticle = new ParticleEnchant();
        burstParticle.setCount(15);
        burstParticle.setOffset(0.8, 1.2, 0.8);
        burstParticle.setSpeed(0.5);
    }

    // ==================== OFFENSIVE: Attacker gains stacks + penetration ====================

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

        handleOffensiveStack(p, user, level);

        // Apply per-hit penetration bonus based on current stacks
        int stacks = getStacks(user);
        if (stacks > 0) {
            double penBonus = calculatePenetrationBonus(level, stacks);
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            if (!property.hasLayer("riftwalkerOffenseLayer")) {
                property.addLayer("riftwalkerOffenseLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer("riftwalkerOffenseLayer", 1);
            }
            property.addValueToLayer("riftwalkerOffenseLayer", penBonus);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        handleOffensiveStack(p, user, level);

        int stacks = getStacks(user);
        if (stacks > 0) {
            double penBonus = calculatePenetrationBonus(level, stacks);
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            if (!property.hasLayer("riftwalkerOffenseLayer")) {
                property.addLayer("riftwalkerOffenseLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer("riftwalkerOffenseLayer", 1);
            }
            property.addValueToLayer("riftwalkerOffenseLayer", penBonus);
        }
    }

    // ==================== DEFENSIVE: Victim gets damage reduction from stacks ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeDefend(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Update location tracking on being hit too
        updateLocationTracking(victim, user);

        int stacks = getStacks(user);
        if (stacks > 0) {
            double reduction = calculateDamageReduction(level, stacks);
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            if (!property.hasLayer(damageLayerName)) {
                property.addLayer(damageLayerName, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer(damageLayerName, 1);
            }
            property.addValueToLayer(damageLayerName, -reduction); // negative = damage reduction
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedDefend(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        updateLocationTracking(victim, user);

        int stacks = getStacks(user);
        if (stacks > 0) {
            double reduction = calculateDamageReduction(level, stacks);
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            if (!property.hasLayer(damageLayerName)) {
                property.addLayer(damageLayerName, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer(damageLayerName, 1);
            }
            property.addValueToLayer(damageLayerName, -reduction);
        }
    }

    // ==================== CORE LOGIC ====================

    private void handleOffensiveStack(Player p, User user, int level) {
        // Initialize location tracking if needed
        if (!user.hasMetadata(lastLocationXMetadataValue) ||
                !(user.getMetadataValue(lastLocationXMetadataValue) instanceof Double)) {
            initializeLocationTracking(p, user);
        }

        if (!user.hasMetadata(stacksMetadataValue) || !(user.getMetadataValue(stacksMetadataValue) instanceof Integer)) {
            user.setMetadata(stacksMetadataValue, 0);
        }

        // Calculate distance moved since last combat hit
        String lastWorld = (String) user.getMetadataValue(lastWorldMetadataValue);
        if (!p.getWorld().getName().equals(lastWorld)) {
            // Different world — reset tracking
            initializeLocationTracking(p, user);
            return;
        }

        double lastX = (Double) user.getMetadataValue(lastLocationXMetadataValue);
        double lastY = (Double) user.getMetadataValue(lastLocationYMetadataValue);
        double lastZ = (Double) user.getMetadataValue(lastLocationZMetadataValue);

        double dx = p.getLocation().getX() - lastX;
        double dy = p.getLocation().getY() - lastY;
        double dz = p.getLocation().getZ() - lastZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        int currentStacks = (Integer) user.getMetadataValue(stacksMetadataValue);
        int previousStacks = currentStacks;

        // Gain stacks based on distance
        int stacksToGain = (int) (distance / DISTANCE_PER_STACK);
        if (stacksToGain > 0 && currentStacks < MAX_STACKS) {
            currentStacks = Math.min(currentStacks + stacksToGain, MAX_STACKS);
            user.setMetadata(stacksMetadataValue, currentStacks);

            // Sound feedback — ascending pitch per stack
            float pitch = 0.8F + currentStacks * 0.2F;
            p.playSound(p.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 0.7F, pitch);

            // Burst effect on stack gain
            burstParticle.display(p.getLocation().clone().add(0, 1.0, 0));

            if (currentStacks > previousStacks) {
                p.sendMessage("§5§l[Riftwalker] §dRift Energy §7x" + currentStacks +
                        " §8(+" + (currentStacks - previousStacks) + " | moved " +
                        String.format("%.1f", distance) + " blocks)");
            }
        } else if (distance < 1.5D && currentStacks > 0) {
            // Standing still — lose a stack
            currentStacks = Math.max(0, currentStacks - 1);
            user.setMetadata(stacksMetadataValue, currentStacks);

            if (currentStacks < previousStacks) {
                p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_BASS.parseSound(), 0.5F, 0.5F);
                p.sendMessage("§5§l[Riftwalker] §8Rift Energy fading... §7x" + currentStacks);
            }
        }

        // Update location to current position
        updateLocationTracking(p, user);

        // Reset decay timer
        resetDecayTimer(p, user, level);

        // Play persistent helix effect if stacks > 0
        if (currentStacks > 0 && currentStacks > previousStacks) {
            playRiftHelix(p, currentStacks);
        }
    }

    private void initializeLocationTracking(Player p, User user) {
        user.setMetadata(lastLocationXMetadataValue, p.getLocation().getX());
        user.setMetadata(lastLocationYMetadataValue, p.getLocation().getY());
        user.setMetadata(lastLocationZMetadataValue, p.getLocation().getZ());
        user.setMetadata(lastWorldMetadataValue, p.getWorld().getName());
        user.setMetadata(stacksMetadataValue, 0);
    }

    private void updateLocationTracking(Player p, User user) {
        user.setMetadata(lastLocationXMetadataValue, p.getLocation().getX());
        user.setMetadata(lastLocationYMetadataValue, p.getLocation().getY());
        user.setMetadata(lastLocationZMetadataValue, p.getLocation().getZ());
        user.setMetadata(lastWorldMetadataValue, p.getWorld().getName());
    }

    private int getStacks(User user) {
        if (!user.hasMetadata(stacksMetadataValue) || !(user.getMetadataValue(stacksMetadataValue) instanceof Integer)) {
            return 0;
        }
        return (Integer) user.getMetadataValue(stacksMetadataValue);
    }

    private void resetDecayTimer(Player p, User user, int level) {
        if (user.getMetadataValue(decayTaskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(decayTaskMetadataValue)).cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            if (!p.isOnline()) return;
            User currentUser = User.getUserFromUUID(p.getUniqueId());
            if (currentUser == null) return;

            int stacks = getStacks(currentUser);
            if (stacks > 0) {
                currentUser.setMetadata(stacksMetadataValue, 0);
                p.sendMessage("§5§l[Riftwalker] §8Rift Energy dissipated.");
                p.playSound(p.getLocation(), XSound.BLOCK_BEACON_DEACTIVATE.parseSound(), 0.6F, 1.5F);
            }
        }, DECAY_TIMEOUT);

        user.setMetadata(decayTaskMetadataValue, task);
    }

    // ==================== BALANCE CALCULATIONS ====================

    /**
     * Per-hit damage multiplier bonus based on stacks and level.
     * At level 20, 5 stacks: 0.03 * 20 * 5 = 3.0 → +300%? No, too high.
     * Let's use: 0.006 * level * stacks
     * Level 20, 5 stacks = 0.006 * 20 * 5 = 0.60 → +60% damage multiplier.
     * Level 10, 3 stacks = 0.006 * 10 * 3 = 0.18 → +18% damage multiplier.
     * Level 1, 1 stack = 0.006 * 1 * 1 = 0.006 → +0.6% damage multiplier.
     */
    private double calculatePenetrationBonus(int level, int stacks) {
        return 0.0035D * (double) level * (double) stacks;
    }

    /**
     * Per-hit damage reduction layer when being hit.
     * At level 20, 5 stacks: 0.004 * 20 * 5 = 0.40 → 40% damage reduction layer.
     * Level 10, 3 stacks: 0.004 * 10 * 3 = 0.12 → 12% damage reduction layer.
     * Level 1, 1 stack: 0.004 * 1 * 1 = 0.004 → 0.4% reduction.
     */
    private double calculateDamageReduction(int level, int stacks) {
        return 0.002D * (double) level * (double) stacks;
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Double-helix effect rotating around the player.
     * Two interleaved spirals — one purple, one cyan — wrapping upward.
     * The number of particles per display scales with stacks.
     * Portal particles orbit at the base for an interdimensional feel.
     */
    private void playRiftHelix(final Player player, final int stacks) {
        new BukkitRunnable() {
            int tick = 0;
            final int duration = 20 + stacks * 4; // longer with more stacks
            final double radius = 0.6D;
            final double heightPerTick = 0.12D;

            public void run() {
                if (tick >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }

                Location center = player.getLocation().clone();

                // Double helix — two strands offset by PI
                int pointsPerTick = 1 + stacks; // more particles at higher stacks
                for (int i = 0; i < pointsPerTick; i++) {
                    double progress = (double) (tick * pointsPerTick + i) / (double) (duration * pointsPerTick);
                    double angle = progress * Math.PI * 4.0D; // 2 full rotations
                    double y = progress * 2.2D; // total height

                    // Strand 1 — purple
                    double x1 = Math.cos(angle) * radius;
                    double z1 = Math.sin(angle) * radius;
                    helixParticle1.display(center.clone().add(x1, y, z1));

                    // Strand 2 — cyan, offset by PI
                    double x2 = Math.cos(angle + Math.PI) * radius;
                    double z2 = Math.sin(angle + Math.PI) * radius;
                    helixParticle2.display(center.clone().add(x2, y, z2));
                }

                // Portal particles orbiting at feet level
                if (tick % 2 == 0) {
                    double footAngle = Math.toRadians(tick * 25);
                    double footR = radius + 0.3D;
                    portalParticle.display(center.clone().add(
                            Math.cos(footAngle) * footR, 0.1D,
                            Math.sin(footAngle) * footR));
                    portalParticle.display(center.clone().add(
                            Math.cos(footAngle + Math.PI) * footR, 0.1D,
                            Math.sin(footAngle + Math.PI) * footR));
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 2L);
    }
}

