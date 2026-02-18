package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleEndRod;
import hm.zelha.particlesfx.particles.ParticleDragonBreath;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

public class PhantomStrike extends Skill {

    private String hitsMetadataValue = "skill.phantomStrike.hits";
    private String taskMetadataValue = "skill.phantomStrike.task";

    private ParticleDustColored phantomCyan;
    private ParticleDustColored phantomWhite;
    private ParticleEndRod endRod;
    private ParticleDragonBreath dragonBreath;

    public PhantomStrike(String id, List<Integer> costList) {
        super(id, costList);

        phantomCyan = new ParticleDustColored();
        phantomCyan.setColor(new hm.zelha.particlesfx.util.Color(0, 220, 240));
        phantomCyan.setCount(1);
        phantomCyan.setOffset(0, 0, 0);

        phantomWhite = new ParticleDustColored();
        phantomWhite.setColor(new hm.zelha.particlesfx.util.Color(210, 245, 255));
        phantomWhite.setCount(1);
        phantomWhite.setOffset(0, 0, 0);

        endRod = new ParticleEndRod();
        endRod.setCount(1);
        endRod.setOffset(0, 0, 0);
        endRod.setSpeed(0.02);

        dragonBreath = new ParticleDragonBreath();
        dragonBreath.setCount(3);
        dragonBreath.setOffset(0.2, 0.2, 0.2);
        dragonBreath.setSpeed(0.02);
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

        Player victim = (Player) e.getDamagePacket().getVictim();

        // Initialize
        if (!user.hasMetadata(hitsMetadataValue) || !(user.getMetadataValue(hitsMetadataValue) instanceof Integer)) {
            user.setMetadata(hitsMetadataValue, 0);
        }
        if (user.getMetadataValue(taskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(taskMetadataValue)).cancel();
        }

        int hits = (Integer) user.getMetadataValue(hitsMetadataValue);
        hits++;
        user.setMetadata(hitsMetadataValue, hits);

        // Decay after 4 seconds
        user.setMetadata(taskMetadataValue, Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            user.setMetadata(hitsMetadataValue, 0);
        }, 80L));

        // Small ghost trail on each hit
        playGhostTrail(attacker, victim, hits);
        attacker.playSound(attacker.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 0.3F, 1.2F + hits * 0.15F);

        // ==================== TRIGGER AT 5 HITS ====================
        if (hits >= 5) {
            // Reset
            ((BukkitTask) user.getMetadataValue(taskMetadataValue)).cancel();
            user.setMetadata(hitsMetadataValue, 0);

            // Calculate phantom damage
            double baseDamage = 2.4D + level * 0.18D; // 3.0 at level 20
            boolean empowered = Utility.rollChance(level * 1.5D); // 30% at level 20
            double finalDamage = empowered ? baseDamage * 1.6D : baseDamage; // 4.8 if empowered

            // Apply attacker penetration buff for 2 seconds
            IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(attacker);
            if (arena != null) {
                Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
                StatsMap statsMap = arena1.getStatsMapOfUUID(user.getUuid());
                if (statsMap != null) {
                    double penBuff = 0.45D * level; // 9.0% at level 20
                    statsMap.getStatContainer(BaseStats.LIFESTEAL.name()).add(penBuff);
                    Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                        statsMap.getStatContainer(BaseStats.LIFESTEAL.name()).subtract(penBuff);
                    }, 40L);
                }
            }

            // ===== DELAYED PHANTOM HIT: damage arrives 0.5s later =====
            final double damage = finalDamage;
            final boolean emp = empowered;

            playPhantomChargeUp(victim, emp);

            attacker.getWorld().playSound(attacker.getLocation(), XSound.ENTITY_PLAYER_ATTACK_SWEEP.parseSound(), 1.5F, 0.6F);
            attacker.getWorld().playSound(victim.getLocation(), XSound.ENTITY_ENDER_DRAGON_FLAP.parseSound(), 1.5F, 2.0F);
            attacker.setHealth(Math.min(attacker.getHealth() + 2, attacker.getMaxHealth()));
            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                if (victim.isOnline() && User.getUserFromUUID(victim.getUniqueId()) != null) {
                    CombatUtils.dealUndefinedDamage(victim, damage,
                            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                            Map.of("phantomStrike", true),
                            Set.of("reductionLayer1", "blockLayer", "KOBW_Defense",
                                    "weaknessLayer", "ironWillLayer",
                                    "riftwalkerDefenseLayer"));

                    playPhantomImpact(victim, emp);
                    victim.getWorld().playSound(victim.getLocation(), XSound.ENTITY_PLAYER_ATTACK_CRIT.parseSound(), 2.0F, 0.5F);

                    if (emp) {
                        victim.getWorld().playSound(victim.getLocation(), XSound.ENTITY_GENERIC_EXPLODE.parseSound(), 0.8F, 2.0F);
                    }
                }
            }, 10L);

            // Message
            String msg = emp
                    ? "§b§l[Phantom Strike] §f⚡ Empowered Strike! §7(§b" + String.format("%.1f", damage) + " §7dmg)"
                    : "§b§l[Phantom Strike] §fPhantom Strike! §7(§b" + String.format("%.1f", damage) + " §7dmg)";
            attacker.sendMessage(msg);
        }
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Ghost trail: a short arc of cyan particles from attacker to victim.
     * Intensity grows with hit count.
     */
    private void playGhostTrail(Player attacker, Player victim, int hits) {
        Location from = attacker.getLocation().add(0, 1.2, 0);
        Location to = victim.getLocation().add(0, 1.0, 0);

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 3) {
                    cancel();
                    return;
                }

                double progress = (double) (tick + 1) / 3;
                Location point = from.clone().add(
                        to.clone().subtract(from).toVector().multiply(progress));
                // Add a small arc upward
                point.add(0, Math.sin(progress * Math.PI) * 0.5D, 0);

                for (int i = 0; i < hits; i++) {
                    double spread = (i - (hits - 1) / 2.0D) * 0.15D;
                    Location pLoc = point.clone().add(spread, 0, spread);
                    phantomCyan.display(pLoc);
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }

    /**
     * Phantom charge up: converging ring around victim that tightens over 10 ticks.
     * Empowered version adds an outer ring and end rod sparkle.
     */
    private void playPhantomChargeUp(Entity victim, boolean empowered) {
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 10 || (victim instanceof Player && !((Player) victim).isOnline())) {
                    cancel();
                    return;
                }

                double radius = 1.5D - tick * 0.13D;
                double y = 0.2D + tick * 0.15D;
                int points = 8;

                for (int i = 0; i < points; i++) {
                    double angle = Math.toRadians((360.0 / points) * i + tick * 40);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location loc = victim.getLocation().clone().add(x, y, z);
                    phantomCyan.display(loc);

                    if (tick % 2 == 0) {
                        phantomWhite.display(loc.clone().add(0, 0.2, 0));
                    }
                }

                if (empowered) {
                    double outerRadius = radius + 0.5D;
                    for (int i = 0; i < 6; i++) {
                        double angle = Math.toRadians((360.0 / 6) * i - tick * 30);
                        double x = Math.cos(angle) * outerRadius;
                        double z = Math.sin(angle) * outerRadius;
                        endRod.display(victim.getLocation().clone().add(x, y + 0.3D, z));
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }

    /**
     * Phantom impact: an expanding cross pattern + vertical pillar burst.
     * Empowered version adds a sphere burst of dragon breath.
     */
    private void playPhantomImpact(Entity victim, boolean empowered) {
        Location center = victim.getLocation().clone();

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 8) {
                    cancel();
                    return;
                }

                // Cross pattern expanding outward (4 arms)
                double dist = tick * 0.4D;
                for (int arm = 0; arm < 4; arm++) {
                    double angle = Math.toRadians(90.0 * arm + 45);
                    double x = Math.cos(angle) * dist;
                    double z = Math.sin(angle) * dist;
                    Location armLoc = center.clone().add(x, 0.1D, z);
                    phantomCyan.display(armLoc);
                    phantomWhite.display(armLoc.clone().add(0, 0.3, 0));
                }

                // Vertical pillar
                if (tick < 6) {
                    double pillarY = tick * 0.5D;
                    endRod.display(center.clone().add(0, pillarY, 0));
                    phantomCyan.display(center.clone().add(0, pillarY + 0.2, 0));
                }

                // Empowered sphere burst
                if (empowered && tick < 5) {
                    int spherePoints = 8;
                    double sphereRadius = tick * 0.3D;
                    for (int i = 0; i < spherePoints; i++) {
                        double phi = Math.acos(1 - 2.0D * i / spherePoints);
                        double theta = Math.PI * (1 + Math.sqrt(5)) * i + tick;
                        double sx = sphereRadius * Math.sin(phi) * Math.cos(theta);
                        double sy = sphereRadius * Math.cos(phi) + 1.0D;
                        double sz = sphereRadius * Math.sin(phi) * Math.sin(theta);
                        dragonBreath.display(center.clone().add(sx, sy, sz));
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}
