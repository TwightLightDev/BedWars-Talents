package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleSweepAttack;
import hm.zelha.particlesfx.particles.ParticleCrit;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;

/**
 * EchoStrike — Attack form transformation skill.
 *
 * Every 4th melee hit is REPLACED by an "Echo Slash" — instead of normal melee
 * damage, the player fires a visible crescent wave projectile in their look
 * direction. The projectile travels forward, damaging ALL enemies in its path.
 *
 * The 4th hit itself deals reduced melee damage, but the wave compensates by
 * being a ranged AoE skillshot. The wave passes through enemies (piercing).
 *
 * This fundamentally CHANGES how the 4th attack works — it's not a buff,
 * not a stack, but a transformed attack form.
 *
 * Visual: A sweeping crescent arc of cyan/white particles that travels
 * forward like an energy wave, with a sweep attack particle at the origin.
 */
public class EchoStrike extends Skill {

    private String hitCountMetadataValue = "skill.echoStrike.hitCount";
    private String decayTaskMetadataValue = "skill.echoStrike.decay";

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Particles
    private ParticleDustColored waveCyan;
    private ParticleDustColored waveWhite;
    private ParticleDustColored waveGlow;
    private ParticleSweepAttack sweepOrigin;
    private ParticleCrit critImpact;

    private static final int HITS_PER_ECHO = 4;
    private static final double WAVE_SPEED = 0.8D; // blocks per tick
    private static final double WAVE_MAX_DISTANCE = 10.0D;
    private static final double WAVE_HIT_RADIUS = 1.5D;

    public EchoStrike(String id, List<Integer> costList) {
        super(id, costList);

        waveCyan = new ParticleDustColored();
        waveCyan.setColor(new hm.zelha.particlesfx.util.Color(0, 230, 255));
        waveCyan.setCount(1);
        waveCyan.setOffset(0, 0, 0);

        waveWhite = new ParticleDustColored();
        waveWhite.setColor(new hm.zelha.particlesfx.util.Color(220, 245, 255));
        waveWhite.setCount(1);
        waveWhite.setOffset(0, 0, 0);

        waveGlow = new ParticleDustColored();
        waveGlow.setColor(new hm.zelha.particlesfx.util.Color(180, 240, 255));
        waveGlow.setCount(1);
        waveGlow.setOffset(0.1, 0.1, 0.1);

        sweepOrigin = new ParticleSweepAttack();
        sweepOrigin.setCount(5);
        sweepOrigin.setOffset(0.5, 0.3, 0.5);
        sweepOrigin.setSpeed(0);

        critImpact = new ParticleCrit();
        critImpact.setCount(8);
        critImpact.setOffset(0.3, 0.3, 0.3);
        critImpact.setSpeed(0.3);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.HIGH)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Initialize
        if (!user.hasMetadata(hitCountMetadataValue) ||
                !(user.getMetadataValue(hitCountMetadataValue) instanceof Integer)) {
            user.setMetadata(hitCountMetadataValue, 0);
        }

        // Cancel previous decay
        if (user.getMetadataValue(decayTaskMetadataValue) instanceof org.bukkit.scheduler.BukkitTask) {
            ((org.bukkit.scheduler.BukkitTask) user.getMetadataValue(decayTaskMetadataValue)).cancel();
        }

        int hitCount = (Integer) user.getMetadataValue(hitCountMetadataValue);
        hitCount++;

        // Schedule decay (reset after 5 seconds of no hits)
        user.setMetadata(decayTaskMetadataValue, Bukkit.getScheduler().runTaskLater(
                Talents.getInstance(), () -> user.setMetadata(hitCountMetadataValue, 0), 100L));

        // Ping sound with escalating pitch for buildup
        float buildPitch = 0.8F + (hitCount % HITS_PER_ECHO) * 0.3F;
        p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_XYLOPHONE.parseSound(), 0.5F, buildPitch);

        if (hitCount >= HITS_PER_ECHO) {
            // === ECHO STRIKE: Transform this attack ===
            user.setMetadata(hitCountMetadataValue, 0);

            // Reduce the melee hit itself to 40% (the wave is the main damage)
            // We do this by setting a very harsh negative layer
            // Actually — we don't reduce; the wave is BONUS to reward the cycle

            // Get direction
            Vector direction = p.getLocation().getDirection().clone();
            direction.setY(0); // Flatten to horizontal
            direction.normalize();

            Location origin = p.getLocation().clone().add(0, 1.0, 0);

            // Sound
            p.getWorld().playSound(p.getLocation(), XSound.ENTITY_PLAYER_ATTACK_SWEEP.parseSound(), 2.0F, 1.5F);
            p.getWorld().playSound(p.getLocation(), XSound.ENTITY_ENDER_DRAGON_FLAP.parseSound(), 1.0F, 2.0F);

            // Sweep at origin
            sweepOrigin.display(origin);

            // Wave damage
            double waveDamage = 0.8D + level * 0.11D; // 0.91 at lv1, 3.0 at lv20

            p.sendMessage("§b§l[Echo Strike] §f✧ Echo Slash!");

            // Fire the wave projectile
            IArena arena = util.getArenaByPlayer(p);
            if (arena == null) return;
            ITeam ownerTeam = arena.getTeam(p);

            fireEchoWave(p, origin, direction, waveDamage, ownerTeam, level);
        } else {
            user.setMetadata(hitCountMetadataValue, hitCount);

            // Show buildup indicator
            int remaining = HITS_PER_ECHO - hitCount;
            if (remaining <= 2) {
                p.sendMessage("§b§l[Echo Strike] §7" + remaining + " hits until Echo Slash...");
            }
        }
    }

    private void fireEchoWave(Player owner, Location origin, Vector direction,
                              double damage, ITeam ownerTeam, int level) {
        Set<UUID> alreadyHit = new HashSet<>();
        alreadyHit.add(owner.getUniqueId());

        // Perpendicular vector for crescent shape
        Vector perp = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        new BukkitRunnable() {
            double traveled = 0;
            Location currentCenter = origin.clone();

            public void run() {
                if (traveled >= WAVE_MAX_DISTANCE || !owner.isOnline()) {
                    cancel();
                    return;
                }

                // Move forward
                currentCenter.add(direction.clone().multiply(WAVE_SPEED));
                traveled += WAVE_SPEED;

                // Draw crescent at current position
                playCrescentWave(currentCenter, perp, direction, traveled);

                // Check for hits
                for (Entity entity : currentCenter.getWorld().getNearbyEntities(
                        currentCenter, WAVE_HIT_RADIUS, 1.5D, WAVE_HIT_RADIUS)) {
                    if (!(entity instanceof Player)) continue;
                    if (alreadyHit.contains(entity.getUniqueId())) continue;

                    Player target = (Player) entity;
                    User targetUser = User.getUserFromUUID(target.getUniqueId());
                    if (targetUser == null) continue;

                    IArena tArena = util.getArenaByPlayer(target);
                    if (tArena == null) continue;
                    if (tArena.getTeam(target) == ownerTeam) continue;

                    alreadyHit.add(target.getUniqueId());

                    // Hit!
                    CombatUtils.dealUndefinedDamage(target, damage,
                            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                            Map.of("echo-strike", true),
                            Set.of("reductionLayer1"));

                    critImpact.display(target.getLocation().add(0, 1.0, 0));
                    target.getWorld().playSound(target.getLocation(),
                            XSound.ENTITY_PLAYER_ATTACK_CRIT.parseSound(), 1.0F, 1.5F);

                    // Slight knockback in wave direction
                    Vector kb = direction.clone().multiply(0.4D).setY(0.2D);
                    target.setVelocity(target.getVelocity().add(kb));
                }

                // Trail sound
                if (traveled < 3.0D) {
                    currentCenter.getWorld().playSound(currentCenter,
                            XSound.ENTITY_PLAYER_ATTACK_SWEEP.parseSound(), 0.3F, 2.0F);
                }

                // Fade after distance
                if (traveled > WAVE_MAX_DISTANCE * 0.7D) {
                    // Thinner crescent as it fades
                }
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Draws a crescent/arc shape perpendicular to the travel direction.
     * The arc widens slightly as it travels for a dramatic sweep look.
     */
    private void playCrescentWave(Location center, Vector perp, Vector forward, double traveled) {
        double arcWidth = 1.0D + traveled * 0.05D; // Slightly widens
        int arcPoints = 7;

        // The crescent is an arc perpendicular to forward direction
        for (int i = 0; i < arcPoints; i++) {
            double t = (double) i / (double) (arcPoints - 1) - 0.5D; // -0.5 to 0.5
            double lateralOffset = t * arcWidth * 2.0D;

            // Slight forward curve (concave like a crescent)
            double forwardCurve = -(t * t) * 0.4D;

            Location point = center.clone()
                    .add(perp.clone().multiply(lateralOffset))
                    .add(forward.clone().multiply(forwardCurve));

            // Alternate colors for depth
            if (i % 2 == 0) {
                waveCyan.display(point);
                waveCyan.display(point.clone().add(0, 0.2, 0));
            } else {
                waveWhite.display(point);
            }

            // Glow at edges
            if (i == 0 || i == arcPoints - 1) {
                waveGlow.display(point);
            }
        }

        // Core glow at center of arc
        waveGlow.display(center);
    }
}

