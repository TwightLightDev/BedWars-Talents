package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleEndRod;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;


public class MirrorMark extends Skill {

    // Maps: markerUUID -> victim UUID
    private Map<UUID, UUID> activeMarks = new HashMap<>();
    // Maps: markerUUID -> marker's team (to check teammate status)
    private Map<UUID, ITeam> markerTeams = new HashMap<>();
    // Maps: markerUUID -> level
    private Map<UUID, Integer> markerLevels = new HashMap<>();
    // Maps: markerUUID -> visual task
    private Map<UUID, BukkitTask> visualTasks = new HashMap<>();

    private String markTaskMetadataValue = "skill.mirrorMark.task";
    private String damageLayerName = "mirrorMarkLayer";

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Particles
    private ParticleDustColored diamondSilver;
    private ParticleDustColored diamondWhite;
    private ParticleDustColored flashRed;
    private ParticleEndRod endRod;

    public MirrorMark(String id, List<Integer> costList) {
        super(id, costList);

        diamondSilver = new ParticleDustColored();
        diamondSilver.setColor(new hm.zelha.particlesfx.util.Color(192, 192, 220));
        diamondSilver.setCount(1);
        diamondSilver.setOffset(0, 0, 0);

        diamondWhite = new ParticleDustColored();
        diamondWhite.setColor(new hm.zelha.particlesfx.util.Color(240, 240, 255));
        diamondWhite.setCount(1);
        diamondWhite.setOffset(0, 0, 0);

        flashRed = new ParticleDustColored();
        flashRed.setColor(new hm.zelha.particlesfx.util.Color(255, 50, 50));
        flashRed.setCount(15);
        flashRed.setOffset(0.5, 0.8, 0.5);
        flashRed.setSpeed(0.3);

        endRod = new ParticleEndRod();
        endRod.setCount(1);
        endRod.setOffset(0.15, 0.15, 0.15);
        endRod.setSpeed(0.01);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttackApplyMark(MeleeDamageEvent e) {
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

        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;
        ITeam attackerTeam = arena.getTeam(attacker);

        // Cancel previous mark visual
        if (visualTasks.containsKey(attacker.getUniqueId())) {
            visualTasks.get(attacker.getUniqueId()).cancel();
            visualTasks.remove(attacker.getUniqueId());
        }
        if (user.getMetadataValue(markTaskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(markTaskMetadataValue)).cancel();
        }

        // Apply new mark
        activeMarks.put(attacker.getUniqueId(), victim.getUniqueId());
        markerTeams.put(attacker.getUniqueId(), attackerTeam);
        markerLevels.put(attacker.getUniqueId(), level);

        // Duration: 4s + 0.15s per level (max ~7s at lv20)
        int durationTicks = 80 + level * 3;

        // Visual flash on mark application
        flashRed.display(victim.getLocation().add(0, 1.0, 0));
        attacker.playSound(attacker.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0F, 2.0F);
        victim.playSound(victim.getLocation(), XSound.ENTITY_ELDER_GUARDIAN_CURSE.parseSound(), 0.5F, 2.0F);

        attacker.sendMessage("§7§l[Mirror Mark] §fMarked §c" + victim.getName() + "§f!");

        // Start rotating diamond visual on victim
        BukkitTask visualTask = new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (!victim.isOnline() || tick >= durationTicks) {
                    cancel();
                    return;
                }
                playMarkVisual(victim, tick);
                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 2L);
        visualTasks.put(attacker.getUniqueId(), visualTask);

        // Schedule mark expiry
        BukkitTask expiryTask = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            activeMarks.remove(attacker.getUniqueId());
            markerTeams.remove(attacker.getUniqueId());
            markerLevels.remove(attacker.getUniqueId());
            if (visualTasks.containsKey(attacker.getUniqueId())) {
                visualTasks.get(attacker.getUniqueId()).cancel();
                visualTasks.remove(attacker.getUniqueId());
            }
            if (attacker.isOnline()) {
                attacker.sendMessage("§7§l[Mirror Mark] §8Mark expired.");
            }
        }, durationTicks);
        user.setMetadata(markTaskMetadataValue, expiryTask);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttackApplyMark(RangedDamageEvent e) {
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

        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;
        ITeam attackerTeam = arena.getTeam(attacker);

        // Cancel previous mark visual
        if (visualTasks.containsKey(attacker.getUniqueId())) {
            visualTasks.get(attacker.getUniqueId()).cancel();
            visualTasks.remove(attacker.getUniqueId());
        }
        if (user.getMetadataValue(markTaskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(markTaskMetadataValue)).cancel();
        }

        // Apply new mark
        activeMarks.put(attacker.getUniqueId(), victim.getUniqueId());
        markerTeams.put(attacker.getUniqueId(), attackerTeam);
        markerLevels.put(attacker.getUniqueId(), level);

        // Duration: 4s + 0.15s per level (max ~7s at lv20)
        int durationTicks = 80 + level * 3;

        // Visual flash on mark application
        flashRed.display(victim.getLocation().add(0, 1.0, 0));
        attacker.playSound(attacker.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0F, 2.0F);
        victim.playSound(victim.getLocation(), XSound.ENTITY_ELDER_GUARDIAN_CURSE.parseSound(), 0.5F, 2.0F);

        attacker.sendMessage("§7§l[Mirror Mark] §fMarked §c" + victim.getName() + "§f!");

        // Start rotating diamond visual on victim
        BukkitTask visualTask = new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (!victim.isOnline() || tick >= durationTicks) {
                    cancel();
                    return;
                }
                playMarkVisual(victim, tick);
                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 2L);
        visualTasks.put(attacker.getUniqueId(), visualTask);

        // Schedule mark expiry
        BukkitTask expiryTask = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            activeMarks.remove(attacker.getUniqueId());
            markerTeams.remove(attacker.getUniqueId());
            markerLevels.remove(attacker.getUniqueId());
            if (visualTasks.containsKey(attacker.getUniqueId())) {
                visualTasks.get(attacker.getUniqueId()).cancel();
                visualTasks.remove(attacker.getUniqueId());
            }
            if (attacker.isOnline()) {
                attacker.sendMessage("§7§l[Mirror Mark] §8Mark expired.");
            }
        }, durationTicks);
        user.setMetadata(markTaskMetadataValue, expiryTask);
    }

    // ==================== DAMAGE AMPLIFICATION (when ANYONE hits the marked target) ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOW)
    public void onMeleeDamageAmplify(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (e.getDamagePacket().getAttacker() == null) return;

        amplifyIfMarked(e.getDamagePacket().getAttacker(),
                (Player) e.getDamagePacket().getVictim(),
                e.getDamagePacket().getDamageProperty());
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOW)
    public void onRangedDamageAmplify(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (e.getDamagePacket().getAttacker() == null) return;

        amplifyIfMarked(e.getDamagePacket().getAttacker(),
                (Player) e.getDamagePacket().getVictim(),
                e.getDamagePacket().getDamageProperty());
    }

    private void amplifyIfMarked(Player hitter, Player victim, DamageProperty property) {
        // Check if ANY marker has marked this victim
        for (Map.Entry<UUID, UUID> entry : activeMarks.entrySet()) {
            if (!entry.getValue().equals(victim.getUniqueId())) continue;

            UUID markerUUID = entry.getKey();
            ITeam markerTeam = markerTeams.get(markerUUID);
            Integer level = markerLevels.get(markerUUID);
            if (markerTeam == null || level == null) continue;

            // Check if the hitter is on the same team as the marker
            IArena hitterArena = util.getArenaByPlayer(hitter);
            if (hitterArena == null) continue;
            ITeam hitterTeam = hitterArena.getTeam(hitter);
            if (hitterTeam != markerTeam) continue;

            // Apply damage amplification layer
            // Formula: +0.4% * level per hit from teammates
            // At level 20: +8% damage amplification on marked target
            double amplification = 0.004D * (double) level;

            if (!property.hasLayer(damageLayerName)) {
                property.addLayer(damageLayerName, LayeredCalculator.OP_ADD,
                        LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer(damageLayerName, 1);
            }
            property.addValueToLayer(damageLayerName, amplification);

            // Feedback to marker
            Player marker = Bukkit.getPlayer(markerUUID);
            if (marker != null && marker.isOnline() && !marker.getUniqueId().equals(hitter.getUniqueId())) {
                marker.playSound(marker.getLocation(),
                        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 0.3F, 1.8F);
            }

            // Sparkle on hit
            endRod.display(victim.getLocation().add(0, 1.5, 0));
            break; // One mark per victim
        }
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * A rotating diamond shape above the victim's head.
     * The diamond is drawn with 4 vertices forming a 3D rhombus.
     */
    private void playMarkVisual(Player victim, int tick) {
        Location head = victim.getLocation().add(0, 2.3, 0);

        // Rotate the diamond
        double angle = Math.toRadians(tick * 6); // Slow rotation

        // Diamond vertices (top, bottom, 4 equatorial)
        double diamondSize = 0.25D;
        double topY = diamondSize * 1.5D;
        double botY = -diamondSize * 0.8D;

        // 4 equatorial points
        for (int i = 0; i < 4; i++) {
            double a = angle + Math.toRadians(90.0 * i);
            double ex = Math.cos(a) * diamondSize;
            double ez = Math.sin(a) * diamondSize;

            // Edge from equator to top
            for (double t = 0; t <= 1.0; t += 0.5) {
                double px = ex * (1.0 - t);
                double py = t * topY;
                double pz = ez * (1.0 - t);
                diamondSilver.display(head.clone().add(px, py, pz));
            }

            // Edge from equator to bottom
            for (double t = 0; t <= 1.0; t += 0.5) {
                double px = ex * (1.0 - t);
                double py = t * botY;
                double pz = ez * (1.0 - t);
                diamondWhite.display(head.clone().add(px, py, pz));
            }
        }

        // Trailing end rod at feet
        if (tick % 3 == 0) {
            endRod.display(victim.getLocation().add(0, 0.1, 0));
        }
    }
}
