package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleSweepAttack;
import hm.zelha.particlesfx.particles.ParticleHeart;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

/**
 * SiphonField — AoE stat-stealing aura skill.
 *
 * ACTIVATION: Sneak + melee hit to deploy a Siphon Field centered on yourself.
 * The field persists for a duration, following you as you move.
 *
 * While active:
 * - Enemies inside the field lose CRITICAL_RATE and CRITICAL_DAMAGE (debuffed)
 * - Allies (teammates) inside the field gain those stolen stats as buffs
 *
 * The "siphon" transfers stats from enemies to allies — a zero-sum aura.
 * Only affects players in the field radius.
 *
 * This is an AOE ENEMY DEBUFF → ALLY BUFF TRANSFER. Unique because the user
 * themselves doesn't directly gain — it's the teammates who benefit from proximity.
 *
 * Visual: A rotating pentagram on the ground with green/red energy streams
 * flowing from enemies toward allies. Enemies have red descending particles,
 * allies have green ascending particles.
 */
public class SiphonField extends Skill {

    private String cooldownMetadataValue = "skill.siphonField.cooldown";
    private String activeFieldMetadataValue = "skill.siphonField.active";

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Particles
    private ParticleDustColored stealRed;
    private ParticleDustColored giveGreen;
    private ParticleDustColored fieldGold;
    private ParticleDustColored fieldDark;
    private ParticleSweepAttack sweep;

    private static final double FIELD_RADIUS = 4.0D;
    private static final long COOLDOWN_MS = 25000L;

    public SiphonField(String id, List<Integer> costList) {
        super(id, costList);

        stealRed = new ParticleDustColored();
        stealRed.setColor(new hm.zelha.particlesfx.util.Color(200, 20, 20));
        stealRed.setCount(3);
        stealRed.setOffset(0.2, 0.3, 0.2);
        stealRed.setSpeed(0);

        giveGreen = new ParticleDustColored();
        giveGreen.setColor(new hm.zelha.particlesfx.util.Color(20, 200, 60));
        giveGreen.setCount(3);
        giveGreen.setOffset(0.2, 0.3, 0.2);
        giveGreen.setSpeed(0);

        fieldGold = new ParticleDustColored();
        fieldGold.setColor(new hm.zelha.particlesfx.util.Color(200, 180, 40));
        fieldGold.setCount(1);
        fieldGold.setOffset(0, 0, 0);

        fieldDark = new ParticleDustColored();
        fieldDark.setColor(new hm.zelha.particlesfx.util.Color(60, 40, 20));
        fieldDark.setCount(1);
        fieldDark.setOffset(0, 0, 0);

        sweep = new ParticleSweepAttack();
        sweep.setCount(2);
        sweep.setOffset(0.1, 0.1, 0.1);
        sweep.setSpeed(0);
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
        if (!p.isSneaking()) return;

        // Check not already active
        if (user.hasMetadata(activeFieldMetadataValue) &&
                (Boolean) user.getMetadataValue(activeFieldMetadataValue)) return;

        // Cooldown
        if (user.hasMetadata(cooldownMetadataValue)) {
            long cd = (Long) user.getMetadataValue(cooldownMetadataValue);
            if (System.currentTimeMillis() < cd) {
                long rem = (cd - System.currentTimeMillis()) / 1000;
                p.sendMessage("§6§l[Siphon Field] §7Cooldown: §c" + rem + "s");
                return;
            }
        }

        user.setMetadata(activeFieldMetadataValue, true);

        int durationTicks = 60 + level * 3; // 3s to 6s
        double critSteal = 0.1D * level; // Steal from CRITICAL_RATE
        double critDmgSteal = 0.15D * level; // Steal from CRITICAL_DAMAGE

        IArena arena = util.getArenaByPlayer(p);
        if (arena == null) return;
        ITeam ownerTeam = arena.getTeam(p);

        // Track modified stats for cleanup
        Map<UUID, double[]> debuffedEnemies = new HashMap<>();
        Map<UUID, double[]> buffedAllies = new HashMap<>();

        p.getWorld().playSound(p.getLocation(), XSound.BLOCK_ENCHANTMENT_TABLE_USE.parseSound(), 2.0F, 0.5F);
        p.sendMessage("§6§l[Siphon Field] §eSiphon active! Draining nearby enemies...");

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= durationTicks || !p.isOnline()) {
                    // Cleanup ALL debuffs and buffs
                    cleanupStats(debuffedEnemies, true);
                    cleanupStats(buffedAllies, false);

                    user.setMetadata(activeFieldMetadataValue, false);
                    user.setMetadata(cooldownMetadataValue,
                            System.currentTimeMillis() + COOLDOWN_MS);

                    if (p.isOnline()) {
                        p.sendMessage("§6§l[Siphon Field] §8Field dissipated.");
                        p.playSound(p.getLocation(), XSound.BLOCK_BEACON_DEACTIVATE.parseSound(), 1.0F, 1.0F);
                    }
                    cancel();
                    return;
                }

                Location center = p.getLocation();

                // Visual: pentagram on ground
                playPentagram(center, FIELD_RADIUS, tick);

                // Process every second
                if (tick % 20 == 0) {
                    // First, clean everyone who left
                    cleanupLeftPlayers(center, debuffedEnemies, true);
                    cleanupLeftPlayers(center, buffedAllies, false);

                    // Find enemies and allies in range
                    for (Entity entity : center.getWorld().getNearbyEntities(center, FIELD_RADIUS, FIELD_RADIUS, FIELD_RADIUS)) {
                        if (!(entity instanceof Player)) continue;
                        if (entity.getUniqueId().equals(p.getUniqueId())) continue;

                        Player target = (Player) entity;
                        User targetUser = User.getUserFromUUID(target.getUniqueId());
                        if (targetUser == null) continue;

                        IArena targetArena = util.getArenaByPlayer(target);
                        if (targetArena == null) continue;
                        ITeam targetTeam = targetArena.getTeam(target);

                        Arena arenaObj = Talents.getInstance().getArenaManager().getArenaFromIArena(targetArena);
                        StatsMap targetStats = arenaObj.getStatsMapOfUUID(target.getUniqueId());
                        if (targetStats == null) continue;

                        if (targetTeam != ownerTeam) {
                            // ENEMY: apply debuff if not already debuffed
                            if (!debuffedEnemies.containsKey(target.getUniqueId())) {
                                targetStats.getStatContainer(BaseStats.CRITICAL_RATE.name()).subtract(critSteal);
                                targetStats.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).subtract(critDmgSteal);
                                debuffedEnemies.put(target.getUniqueId(), new double[]{critSteal, critDmgSteal});
                            }
                            // Descending red particles
                            stealRed.display(target.getLocation().add(0, 2.0, 0));
                        } else {
                            // ALLY: apply buff if not already buffed
                            if (!buffedAllies.containsKey(target.getUniqueId())) {
                                targetStats.getStatContainer(BaseStats.CRITICAL_RATE.name()).add(critSteal);
                                targetStats.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).add(critDmgSteal);
                                buffedAllies.put(target.getUniqueId(), new double[]{critSteal, critDmgSteal});
                            }
                            // Ascending green particles
                            giveGreen.display(target.getLocation().add(0, 0.5, 0));
                        }
                    }
                }

                // Ambient sound
                if (tick % 40 == 0) {
                    center.getWorld().playSound(center, XSound.BLOCK_BEACON_AMBIENT.parseSound(), 0.8F, 1.5F);
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }

    private void cleanupLeftPlayers(Location center, Map<UUID, double[]> tracked, boolean isDebuff) {
        Iterator<Map.Entry<UUID, double[]>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, double[]> entry = it.next();
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target == null || !target.isOnline() ||
                    target.getLocation().distance(center) > FIELD_RADIUS + 1.0D) {
                if (target != null && target.isOnline()) {
                    IArena a = util.getArenaByPlayer(target);
                    if (a != null) {
                        Arena a1 = Talents.getInstance().getArenaManager().getArenaFromIArena(a);
                        StatsMap sm = a1.getStatsMapOfUUID(target.getUniqueId());
                        if (sm != null) {
                            if (isDebuff) {
                                sm.getStatContainer(BaseStats.CRITICAL_RATE.name()).add(entry.getValue()[0]);
                                sm.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).add(entry.getValue()[1]);
                            } else {
                                sm.getStatContainer(BaseStats.CRITICAL_RATE.name()).subtract(entry.getValue()[0]);
                                sm.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).subtract(entry.getValue()[1]);
                            }
                        }
                    }
                }
                it.remove();
            }
        }
    }

    private void cleanupStats(Map<UUID, double[]> tracked, boolean isDebuff) {
        for (Map.Entry<UUID, double[]> entry : tracked.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target != null && target.isOnline()) {
                IArena a = util.getArenaByPlayer(target);
                if (a != null) {
                    Arena a1 = Talents.getInstance().getArenaManager().getArenaFromIArena(a);
                    StatsMap sm = a1.getStatsMapOfUUID(target.getUniqueId());
                    if (sm != null) {
                        if (isDebuff) {
                            sm.getStatContainer(BaseStats.CRITICAL_RATE.name()).add(entry.getValue()[0]);
                            sm.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).add(entry.getValue()[1]);
                        } else {
                            sm.getStatContainer(BaseStats.CRITICAL_RATE.name()).subtract(entry.getValue()[0]);
                            sm.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).subtract(entry.getValue()[1]);
                        }
                    }
                }
            }
        }
        tracked.clear();
    }

    // ==================== VISUAL EFFECTS ====================

    private void playPentagram(Location center, double radius, int tick) {
        double displayRadius = radius * 0.6D;
        int sides = 5;

        // Draw a pentagram (5-pointed star)
        double rotation = Math.toRadians(tick * 1.5);
        for (int i = 0; i < sides; i++) {
            // Connect every other vertex to form a star
            double angle1 = rotation + Math.toRadians((360.0 / sides) * i);
            double angle2 = rotation + Math.toRadians((360.0 / sides) * ((i + 2) % sides));

            Location v1 = center.clone().add(Math.cos(angle1) * displayRadius, 0.1D, Math.sin(angle1) * displayRadius);
            Location v2 = center.clone().add(Math.cos(angle2) * displayRadius, 0.1D, Math.sin(angle2) * displayRadius);

            // Draw line between vertices
            int segs = 4;
            for (int s = 0; s <= segs; s++) {
                double t = (double) s / (double) segs;
                Location point = v1.clone().add(v2.clone().subtract(v1).toVector().multiply(t));
                fieldGold.display(point);
            }
        }

        // Outer circle
        if (tick % 3 == 0) {
            int ringPoints = 12;
            for (int i = 0; i < ringPoints; i++) {
                double angle = Math.toRadians((360.0 / ringPoints) * i - tick * 2);
                fieldDark.display(center.clone().add(
                        Math.cos(angle) * displayRadius * 1.1D, 0.05D,
                        Math.sin(angle) * displayRadius * 1.1D));
            }
        }
    }
}

