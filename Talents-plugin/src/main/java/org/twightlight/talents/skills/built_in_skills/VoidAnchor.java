package org.twightlight.talents.skills.built_in_skills;

import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleEnchant;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

/**
 * VoidAnchor — Active movement restriction / opponent tether skill.
 *
 * ACTIVATION: Sneak + melee hit to anchor the enemy to their current position.
 * While anchored, if the target moves more than X blocks from the anchor point,
 * they are violently yanked back to the anchor and take undefined damage.
 *
 * The anchor lasts for a short duration. The target can see the anchor point
 * as a pulsing dark circle at their feet with a tether line to their body.
 *
 * Only ONE anchor per attacker. Cooldown after expiry.
 *
 * This is a pure OPPONENT MOVEMENT RESTRICTION — no self buffs.
 * It punishes enemies who try to flee and rewards aggressive anchoring plays.
 */
public class VoidAnchor extends Skill {

    private String cooldownMetadataValue = "skill.voidAnchor.cooldown";
    private String anchorTaskMetadataValue = "skill.voidAnchor.task";

    // Particles
    private ParticleDustColored anchorDark;
    private ParticleDustColored anchorRed;
    private ParticleDustColored tetherLine;
    private ParticleEnchant snapBurst;

    private static final long COOLDOWN_MS = 20000L; // 20 seconds
    private static final double BASE_LEASH_RADIUS = 4.5D;

    public VoidAnchor(String id, List<Integer> costList) {
        super(id, costList);

        anchorDark = new ParticleDustColored();
        anchorDark.setColor(new hm.zelha.particlesfx.util.Color(20, 0, 30));
        anchorDark.setCount(1);
        anchorDark.setOffset(0, 0, 0);

        anchorRed = new ParticleDustColored();
        anchorRed.setColor(new hm.zelha.particlesfx.util.Color(180, 0, 40));
        anchorRed.setCount(1);
        anchorRed.setOffset(0, 0, 0);

        tetherLine = new ParticleDustColored();
        tetherLine.setColor(new hm.zelha.particlesfx.util.Color(100, 0, 60));
        tetherLine.setCount(1);
        tetherLine.setOffset(0, 0, 0);

        snapBurst = new ParticleEnchant();
        snapBurst.setCount(20);
        snapBurst.setOffset(0.8, 1.2, 0.8);
        snapBurst.setSpeed(0.6);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player attacker = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(attacker.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Must be sneaking to activate
        if (!attacker.isSneaking()) return;

        // Cooldown
        if (user.hasMetadata(cooldownMetadataValue)) {
            long cd = (Long) user.getMetadataValue(cooldownMetadataValue);
            if (System.currentTimeMillis() < cd) {
                long remaining = (cd - System.currentTimeMillis()) / 1000;
                attacker.sendMessage("§4§l[Void Anchor] §7Cooldown: §c" + remaining + "s");
                return;
            }
        }

        // Cancel previous anchor
        if (user.getMetadataValue(anchorTaskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(anchorTaskMetadataValue)).cancel();
        }

        Player victim = (Player) e.getDamagePacket().getVictim();
        Location anchorPoint = victim.getLocation().clone();

        // Duration: 2.5s + 0.1s per level (max 4.5s at lv20)
        int durationTicks = 50 + level * 2;
        double leashRadius = BASE_LEASH_RADIUS - level * 0.05D; // Tighter at higher levels: 4.5 -> 3.5
        double snapDamage = 1.0D + level * 0.1D; // 1.1 at lv1, 3.0 at lv20
        int maxSnaps = 2 + level / 10; // 2 at lv1-9, 3 at lv10-19, 4 at lv20

        // Sound + message
        attacker.getWorld().playSound(anchorPoint, XSound.BLOCK_ANVIL_PLACE.parseSound(), 1.5F, 0.5F);
        attacker.getWorld().playSound(anchorPoint, XSound.ENTITY_EVOKER_PREPARE_SUMMON.parseSound(), 1.0F, 1.5F);
        attacker.sendMessage("§4§l[Void Anchor] §cAnchored §f" + victim.getName() + "§c!");
        victim.sendMessage("§4§l[Void Anchor] §cYou are anchored! Don't stray too far...");

        // Set cooldown NOW
        user.setMetadata(cooldownMetadataValue, System.currentTimeMillis() + COOLDOWN_MS);

        BukkitTask task = new BukkitRunnable() {
            int tick = 0;
            int snapsUsed = 0;

            public void run() {
                if (tick >= durationTicks || !victim.isOnline() || !attacker.isOnline() ||
                        snapsUsed >= maxSnaps) {
                    if (victim.isOnline()) {
                        victim.sendMessage("§4§l[Void Anchor] §8Anchor released.");
                    }
                    cancel();
                    return;
                }

                // Visual: anchor point circle + tether
                playAnchorVisual(anchorPoint, victim, tick, leashRadius);

                // Check distance
                if (tick % 2 == 0 && victim.getLocation().getWorld().equals(anchorPoint.getWorld())) {
                    double dist = victim.getLocation().distance(anchorPoint);

                    if (dist > leashRadius) {
                        // YANK BACK
                        snapsUsed++;

                        // Teleport with velocity toward anchor
                        Vector pullBack = anchorPoint.toVector()
                                .subtract(victim.getLocation().toVector()).normalize().multiply(1.2D);
                        pullBack.setY(0.4D);
                        victim.setVelocity(pullBack);

                        // Deal damage
                        CombatUtils.dealUndefinedDamage(victim, snapDamage,
                                EntityDamageEvent.DamageCause.MAGIC,
                                Map.of("void-anchor", true),
                                Set.of("reductionLayer1"));

                        // Effects
                        playSnapEffect(victim, anchorPoint);
                        victim.getWorld().playSound(victim.getLocation(),
                                XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 2.0F, 0.3F);
                        victim.getWorld().playSound(anchorPoint,
                                XSound.BLOCK_CHAIN_BREAK.parseSound(), 2.0F, 0.5F);

                        victim.sendMessage("§4§l[Void Anchor] §c§lYANKED! §7("
                                + snapsUsed + "/" + maxSnaps + ")");
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);

        user.setMetadata(anchorTaskMetadataValue, task);
    }

    // ==================== VISUAL EFFECTS ====================

    private void playAnchorVisual(Location anchor, Player victim, int tick, double radius) {
        // Pulsing circle at anchor point
        double pulseRadius = radius * 0.3D * (0.8D + 0.2D * Math.sin(tick * 0.2D));
        int points = 12;
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians((360.0 / points) * i + tick * 2);
            double x = Math.cos(angle) * pulseRadius;
            double z = Math.sin(angle) * pulseRadius;
            anchorDark.display(anchor.clone().add(x, 0.05D, z));
        }

        // Cross at center
        if (tick % 4 == 0) {
            for (double d = -0.3D; d <= 0.3D; d += 0.15D) {
                anchorRed.display(anchor.clone().add(d, 0.1D, 0));
                anchorRed.display(anchor.clone().add(0, 0.1D, d));
            }
        }

        // Tether line from anchor to victim
        if (tick % 2 == 0 && victim.isOnline()) {
            Location from = anchor.clone().add(0, 0.5D, 0);
            Location to = victim.getLocation().add(0, 0.8D, 0);
            int segments = 6;
            for (int s = 0; s <= segments; s++) {
                double t = (double) s / (double) segments;
                Location point = from.clone().add(
                        to.clone().subtract(from).toVector().multiply(t));
                // Slight droop in the middle
                point.add(0, -Math.sin(t * Math.PI) * 0.4D, 0);
                tetherLine.display(point);
            }
        }
    }

    private void playSnapEffect(Player victim, Location anchor) {
        // Burst at victim
        snapBurst.display(victim.getLocation().add(0, 1.0, 0));

        // Quick line from victim to anchor (reverse pull visual)
        Location from = victim.getLocation().add(0, 1.0, 0);
        Location to = anchor.clone().add(0, 0.5, 0);

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 4) {
                    cancel();
                    return;
                }
                double progress = (double) (tick + 1) / 4.0D;
                Location point = from.clone().add(
                        to.clone().subtract(from).toVector().multiply(progress));
                anchorRed.display(point);
                anchorDark.display(point.clone().add(0, 0.2, 0));
                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}

