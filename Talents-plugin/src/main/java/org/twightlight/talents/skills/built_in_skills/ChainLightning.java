package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleFirework;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

/**
 * ChainLightning — Multi-target chain damage skill.
 *
 * When you hit an enemy, there is a chance (scaling with level) that a lightning
 * bolt chains from the victim to the nearest enemy within range, then to the next,
 * and so on. Each bounce deals reduced damage. Maximum bounces scale with level.
 *
 * This is purely OPPONENT-SIDE — damage only goes to enemies. No self stacks, no self buffs.
 * It rewards fighting near groups of enemies.
 *
 * Visual: A jagged lightning bolt particle line that arcs between targets,
 * with electric blue/white particles and a bright flash at each impact point.
 * Each bounce has a small delay for dramatic effect.
 */
public class ChainLightning extends Skill {

    private String cooldownMetadataValue = "skill.chainLightning.cooldown";

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Particles
    private ParticleDustColored boltBlue;
    private ParticleDustColored boltWhite;
    private ParticleDustColored boltCyan;
    private ParticleFirework impactSpark;

    private static final double CHAIN_RADIUS = 5.0D;
    private static final long CHAIN_COOLDOWN_MS = 3000L; // 3s internal cooldown

    public ChainLightning(String id, List<Integer> costList) {
        super(id, costList);

        boltBlue = new ParticleDustColored();
        boltBlue.setColor(new hm.zelha.particlesfx.util.Color(30, 120, 255));
        boltBlue.setCount(1);
        boltBlue.setOffset(0, 0, 0);

        boltWhite = new ParticleDustColored();
        boltWhite.setColor(new hm.zelha.particlesfx.util.Color(220, 235, 255));
        boltWhite.setCount(1);
        boltWhite.setOffset(0, 0, 0);

        boltCyan = new ParticleDustColored();
        boltCyan.setColor(new hm.zelha.particlesfx.util.Color(0, 200, 255));
        boltCyan.setCount(1);
        boltCyan.setOffset(0, 0, 0);

        impactSpark = new ParticleFirework();
        impactSpark.setCount(8);
        impactSpark.setOffset(0.3, 0.3, 0.3);
        impactSpark.setSpeed(0.15);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player p = e.getDamagePacket().getAttacker();
        tryChain(p, e.getDamagePacket().getVictim());
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;

        Player p = e.getDamagePacket().getAttacker();
        tryChain(p, e.getDamagePacket().getVictim());
    }

    private void tryChain(Player attacker, Entity firstVictim) {
        if (firstVictim == null) return;
        User user = User.getUserFromUUID(attacker.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Cooldown check
        if (user.hasMetadata(cooldownMetadataValue)) {
            long cd = (Long) user.getMetadataValue(cooldownMetadataValue);
            if (System.currentTimeMillis() < cd) return;
        }

        // Chance to proc: 15% + 1.5% per level (max 45% at lv20)
        double procChance = 15.0D + 1.5D * (double) level;
        if (!Utility.rollChance(procChance)) return;

        // Set cooldown
        user.setMetadata(cooldownMetadataValue,
                System.currentTimeMillis() + CHAIN_COOLDOWN_MS);

        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;
        ITeam attackerTeam = arena.getTeam(attacker);

        // Calculate chain parameters
        int maxBounces = 1 + level / 5; // 1 at lv1-4, 2 at lv5-9, 3 at lv10-14, 4 at lv15-19, 5 at lv20
        double baseDamage = 0.5D + level * 0.1D; // 0.6 at lv1, 2.5 at lv20
        double damageDecay = 0.7D; // Each bounce does 70% of previous

        // Sound
        attacker.getWorld().playSound(firstVictim.getLocation(),
                XSound.ENTITY_LIGHTNING_BOLT_THUNDER.parseSound(), 0.6F, 2.0F);

        attacker.sendMessage("§b§l[Chain Lightning] §f⚡ Chained!");

        // Execute chain with delays
        executeChain(attacker, firstVictim, attackerTeam, maxBounces, baseDamage, damageDecay);
    }

    private void executeChain(Player attacker, Entity startVictim, ITeam attackerTeam,
                              int maxBounces, double baseDamage, double damageDecay) {
        Set<UUID> alreadyHit = new HashSet<>();
        alreadyHit.add(attacker.getUniqueId());
        alreadyHit.add(startVictim.getUniqueId());

        new BukkitRunnable() {
            Entity currentTarget = startVictim;
            int bounceCount = 0;
            double currentDamage = baseDamage;

            public void run() {
                if (bounceCount >= maxBounces || currentTarget == null || !attacker.isOnline()) {
                    cancel();
                    return;
                }

                // Find nearest enemy
                Player nextTarget = findNearestEnemy(currentTarget.getLocation(),
                        CHAIN_RADIUS, attackerTeam, alreadyHit);

                if (nextTarget == null) {
                    cancel();
                    return;
                }

                alreadyHit.add(nextTarget.getUniqueId());

                // Draw lightning bolt between current and next
                playLightningBolt(currentTarget.getLocation().add(0, 1.0, 0),
                        nextTarget.getLocation().add(0, 1.0, 0), bounceCount);

                // Impact effect
                impactSpark.display(nextTarget.getLocation().add(0, 1.0, 0));
                nextTarget.getWorld().playSound(nextTarget.getLocation(),
                        XSound.ENTITY_LIGHTNING_BOLT_IMPACT.parseSound(), 0.8F, 1.5F + bounceCount * 0.2F);

                // Deal damage
                CombatUtils.dealUndefinedDamage(nextTarget, currentDamage,
                        EntityDamageEvent.DamageCause.LIGHTNING,
                        Map.of("chain-lightning", true),
                        Set.of("reductionLayer1"));

                currentTarget = nextTarget;
                currentDamage *= damageDecay;
                bounceCount++;
            }
        }.runTaskTimer(Talents.getInstance(), 3L, 4L); // 4 ticks between bounces
    }

    private Player findNearestEnemy(Location center, double radius, ITeam attackerTeam, Set<UUID> exclude) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof Player)) continue;
            if (exclude.contains(entity.getUniqueId())) continue;

            Player target = (Player) entity;
            User targetUser = User.getUserFromUUID(target.getUniqueId());
            if (targetUser == null) continue;

            IArena targetArena = util.getArenaByPlayer(target);
            if (targetArena == null) continue;
            if (targetArena.getTeam(target) == attackerTeam) continue;

            double dist = target.getLocation().distance(center);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = target;
            }
        }
        return nearest;
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Jagged lightning bolt between two points.
     * Creates 3-5 intermediate "kink" points with random offsets
     * to simulate a natural lightning shape.
     */
    private void playLightningBolt(Location from, Location to, int bounceIndex) {
        int segments = 4 + bounceIndex; // More segments for later bounces
        Random rand = new Random();

        Location[] points = new Location[segments + 1];
        points[0] = from.clone();
        points[segments] = to.clone();

        // Generate random intermediate points
        for (int i = 1; i < segments; i++) {
            double t = (double) i / (double) segments;
            Location mid = from.clone().add(
                    to.clone().subtract(from).toVector().multiply(t));
            // Add random jagged offset
            double jag = 0.4D;
            mid.add(
                    (rand.nextDouble() - 0.5D) * jag,
                    (rand.nextDouble() - 0.5D) * jag * 0.5D,
                    (rand.nextDouble() - 0.5D) * jag
            );
            points[i] = mid;
        }

        // Draw lines between points
        for (int i = 0; i < segments; i++) {
            Location a = points[i];
            Location b = points[i + 1];
            int particlesPerSegment = 3;
            for (int p = 0; p <= particlesPerSegment; p++) {
                double progress = (double) p / (double) particlesPerSegment;
                Location point = a.clone().add(
                        b.clone().subtract(a).toVector().multiply(progress));

                if (p % 2 == 0) {
                    boltBlue.display(point);
                } else {
                    boltWhite.display(point);
                }

                // Core glow
                if (p == particlesPerSegment / 2) {
                    boltCyan.display(point);
                }
            }
        }
    }
}

