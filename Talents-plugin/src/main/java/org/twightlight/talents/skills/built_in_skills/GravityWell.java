package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleEnchant;
import hm.zelha.particlesfx.particles.ParticlePortal;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
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

/**
 * GravityWell — Active zone-control skill.
 *
 * ACTIVATION: Sneak + melee hit to deploy a Gravity Well at the victim's location.
 * The well persists for a duration and creates a visible vortex zone.
 * Enemies inside the zone are pulled toward the center each tick and receive
 * a stacking movement speed debuff + penetration debuff (their armor is weakened).
 *
 * Only ONE well can be active at a time per player. Deploying a new one destroys the old.
 * Cooldown between deployments.
 *
 * This is a PURE ZONE-BASED ENEMY DEBUFF skill — the user gains nothing.
 * It rewards tactical placement and area denial.
 *
 * Visual: A dark purple/black vortex with inward-spiraling particles,
 * a pulsing ring on the ground, and enchantment letters being sucked inward.
 */
public class GravityWell extends Skill {

    private String cooldownMetadataValue = "skill.gravityWell.cooldown";
    private String activeWellTaskMetadataValue = "skill.gravityWell.activeTask";

    // Active well tracking: playerUUID -> well center location
    private Map<UUID, Location> activeWells = new HashMap<>();

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Particles
    private ParticleDustColored vortexDark;
    private ParticleDustColored vortexPurple;
    private ParticlePortal portalInward;
    private ParticleEnchant enchantSuck;

    // Constants
    private static final long COOLDOWN_TICKS = 300L; // 15 seconds
    private static final double BASE_RADIUS = 3.0D;
    private static final double PULL_STRENGTH = 0.15D;

    public GravityWell(String id, List<Integer> costList) {
        super(id, costList);

        vortexDark = new ParticleDustColored();
        vortexDark.setColor(new hm.zelha.particlesfx.util.Color(30, 0, 50));
        vortexDark.setCount(1);
        vortexDark.setOffset(0, 0, 0);

        vortexPurple = new ParticleDustColored();
        vortexPurple.setColor(new hm.zelha.particlesfx.util.Color(120, 0, 200));
        vortexPurple.setCount(1);
        vortexPurple.setOffset(0, 0, 0);

        portalInward = new ParticlePortal();
        portalInward.setCount(2);
        portalInward.setOffset(0.1, 0.1, 0.1);
        portalInward.setSpeed(0.05);

        enchantSuck = new ParticleEnchant();
        enchantSuck.setCount(3);
        enchantSuck.setOffset(0.5, 0.8, 0.5);
        enchantSuck.setSpeed(0.8);
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

        // ACTIVATION: Must be sneaking
        if (!p.isSneaking()) return;

        // Cooldown check
        if (user.hasMetadata(cooldownMetadataValue)) {
            long cooldownEnd = (Long) user.getMetadataValue(cooldownMetadataValue);
            if (System.currentTimeMillis() < cooldownEnd) {
                long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
                p.sendMessage("§5§l[Gravity Well] §7Cooldown: §c" + remaining + "s");
                return;
            }
        }

        // Cancel previous well if exists
        if (user.hasMetadata(activeWellTaskMetadataValue) &&
                user.getMetadataValue(activeWellTaskMetadataValue) instanceof Integer) {
            int oldTaskId = (Integer) user.getMetadataValue(activeWellTaskMetadataValue);
            Bukkit.getScheduler().cancelTask(oldTaskId);
            activeWells.remove(p.getUniqueId());
        }

        // Deploy well at victim location
        Entity victim = e.getDamagePacket().getVictim();
        if (victim == null) return;

        Location wellCenter = victim.getLocation().clone();
        activeWells.put(p.getUniqueId(), wellCenter);

        // Set cooldown
        long cooldownMs = COOLDOWN_TICKS * 50L;
        // Cooldown starts AFTER well expires
        int durationTicks = 60 + level * 4; // 3s at lv1, 7s at lv20

        // Sound
        p.getWorld().playSound(wellCenter, XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 2.0F, 0.3F);
        p.getWorld().playSound(wellCenter, XSound.BLOCK_PORTAL_TRIGGER.parseSound(), 1.0F, 0.5F);
        p.sendMessage("§5§l[Gravity Well] §dDeployed!");

        // Run the well
        double radius = BASE_RADIUS + level * 0.05D; // 3.0-4.0 at lv20
        double pullStr = PULL_STRENGTH + level * 0.005D;
        double armor = 0.3D * level; // debuff to ARMOR stat
        double penDebuff = 0.15D * level; // debuff to enemy PENETRATION_RESISTANCE

        IArena arena = util.getArenaByPlayer(p);
        ITeam ownerTeam = arena != null ? arena.getTeam(p) : null;

        // Track debuffed players for cleanup
        Map<UUID, double[]> debuffedPlayers = new HashMap<>();

        int taskId = new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= durationTicks || !p.isOnline()) {
                    // Cleanup debuffs
                    cleanupDebuffs(debuffedPlayers);
                    activeWells.remove(p.getUniqueId());

                    // Set cooldown NOW (after well expires)
                    User u = User.getUserFromUUID(p.getUniqueId());
                    if (u != null) {
                        u.setMetadata(cooldownMetadataValue,
                                System.currentTimeMillis() + cooldownMs);
                    }

                    if (p.isOnline()) {
                        p.sendMessage("§5§l[Gravity Well] §8Collapsed.");
                        p.playSound(p.getLocation(), XSound.BLOCK_BEACON_DEACTIVATE.parseSound(), 1.0F, 0.5F);
                    }

                    cancel();
                    return;
                }

                // === VISUAL: Inward-spiraling vortex ===
                playVortexEffect(wellCenter, radius, tick);

                // === PULL + DEBUFF enemies ===
                if (tick % 4 == 0) { // Every 4 ticks
                    for (Entity entity : wellCenter.getWorld().getNearbyEntities(wellCenter, radius, radius, radius)) {
                        if (!(entity instanceof Player)) continue;
                        if (entity.getUniqueId().equals(p.getUniqueId())) continue;

                        Player target = (Player) entity;
                        User targetUser = User.getUserFromUUID(target.getUniqueId());
                        if (targetUser == null) continue;

                        IArena targetArena = util.getArenaByPlayer(target);
                        if (targetArena == null) continue;
                        if (ownerTeam != null && targetArena.getTeam(target) == ownerTeam) continue;

                        double dist = target.getLocation().distance(wellCenter);
                        if (dist > radius) continue;

                        // Pull toward center
                        Vector pull = wellCenter.toVector().subtract(target.getLocation().toVector()).normalize();
                        double pullFactor = pullStr * (1.0D - dist / radius); // stronger at center
                        pull.multiply(pullFactor);
                        pull.setY(Math.max(-0.05D, pull.getY())); // Don't pull underground
                        target.setVelocity(target.getVelocity().add(pull));

                        // Apply debuff via StatsMap
                        Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(targetArena);
                        StatsMap targetStats = arena1.getStatsMapOfUUID(target.getUniqueId());
                        if (targetStats != null) {
                            if (!debuffedPlayers.containsKey(target.getUniqueId())) {
                                targetStats.getStatContainer(BaseStats.GENERIC_ADDITIONAL_ARMOR.name()).subtract(armor);
                                targetStats.getStatContainer("PENETRATION_RATIO").subtract(penDebuff);
                                debuffedPlayers.put(target.getUniqueId(),
                                        new double[]{armor, penDebuff});
                            }
                        }

                        // Pull sound
                        target.playSound(target.getLocation(),
                                XSound.BLOCK_PORTAL_AMBIENT.parseSound(), 0.3F, 2.0F);
                    }
                }

                // Clean debuffs from players who left the zone
                if (tick % 10 == 0) {
                    Iterator<Map.Entry<UUID, double[]>> it = debuffedPlayers.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<UUID, double[]> entry = it.next();
                        Player target = Bukkit.getPlayer(entry.getKey());
                        if (target == null || !target.isOnline() ||
                                target.getLocation().distance(wellCenter) > radius + 1.0D) {

                            if (target != null && target.isOnline()) {
                                IArena tArena = util.getArenaByPlayer(target);
                                if (tArena != null) {
                                    Arena a1 = Talents.getInstance().getArenaManager().getArenaFromIArena(tArena);
                                    StatsMap sm = a1.getStatsMapOfUUID(target.getUniqueId());
                                    if (sm != null) {
                                        sm.getStatContainer(BaseStats.GENERIC_ADDITIONAL_ARMOR.name()).add(entry.getValue()[0]);
                                        sm.getStatContainer("PENETRATION_RATIO").add(entry.getValue()[1]);
                                    }
                                }
                            }
                            it.remove();
                        }
                    }
                }

                // Sound pulse
                if (tick % 20 == 0) {
                    wellCenter.getWorld().playSound(wellCenter,
                            XSound.BLOCK_BEACON_AMBIENT.parseSound(), 1.5F, 0.3F);
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L).getTaskId();

        user.setMetadata(activeWellTaskMetadataValue, taskId);
    }

    private void cleanupDebuffs(Map<UUID, double[]> debuffedPlayers) {
        for (Map.Entry<UUID, double[]> entry : debuffedPlayers.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target != null && target.isOnline()) {
                IArena tArena = util.getArenaByPlayer(target);
                if (tArena != null) {
                    Arena a1 = Talents.getInstance().getArenaManager().getArenaFromIArena(tArena);
                    StatsMap sm = a1.getStatsMapOfUUID(target.getUniqueId());
                    if (sm != null) {
                        sm.getStatContainer("MOVEMENT_SPEED").add(entry.getValue()[0]);
                        sm.getStatContainer("PENETRATION_RATIO").add(entry.getValue()[1]);
                    }
                }
            }
        }
        debuffedPlayers.clear();
    }

    // ==================== VISUAL EFFECTS ====================

    private void playVortexEffect(Location center, double radius, int tick) {
        // Ground ring — pulsing
        double ringRadius = radius * (0.8D + 0.2D * Math.sin(tick * 0.15D));
        int ringPoints = 16;
        for (int i = 0; i < ringPoints; i++) {
            double angle = Math.toRadians((360.0 / ringPoints) * i + tick * 3);
            double x = Math.cos(angle) * ringRadius;
            double z = Math.sin(angle) * ringRadius;
            vortexDark.display(center.clone().add(x, 0.1D, z));
        }

        // Inward spiraling particles (3 spiral arms)
        for (int arm = 0; arm < 3; arm++) {
            double baseAngle = Math.toRadians(120.0 * arm - tick * 8);
            double spiralProgress = (tick % 20) / 20.0D;
            double spiralR = radius * (1.0D - spiralProgress);
            double spiralY = spiralProgress * 2.5D;

            double sx = Math.cos(baseAngle + spiralProgress * Math.PI * 2) * spiralR;
            double sz = Math.sin(baseAngle + spiralProgress * Math.PI * 2) * spiralR;
            vortexPurple.display(center.clone().add(sx, spiralY, sz));

            // Inner trail
            double innerProgress = ((tick + 7) % 20) / 20.0D;
            double innerR = radius * (1.0D - innerProgress) * 0.6D;
            double innerY = innerProgress * 2.0D;
            double ix = Math.cos(baseAngle + innerProgress * Math.PI * 3) * innerR;
            double iz = Math.sin(baseAngle + innerProgress * Math.PI * 3) * innerR;
            portalInward.display(center.clone().add(ix, innerY, iz));
        }

        // Central column of enchantment particles
        if (tick % 3 == 0) {
            enchantSuck.display(center.clone().add(0, 1.0D, 0));
        }
    }
}

