package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleSnowDust;
import hm.zelha.particlesfx.particles.ParticleCritMagic;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;

public class FrostGlyph extends Skill {

    private String glyphCountMetadataValue = "skill.frostGlyph.count";
    private String cooldownMetadataValue = "skill.frostGlyph.cooldown";

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Active glyphs: ownerUUID -> list of glyph data
    private Map<UUID, List<GlyphData>> activeGlyphs = new HashMap<>();

    // Particles
    private ParticleDustColored iceBlue;
    private ParticleDustColored iceWhite;
    private ParticleDustColored iceCyan;
    private ParticleSnowDust snowball;
    private ParticleCritMagic critMagic;

    private static final double GLYPH_TRIGGER_RADIUS = 1.2D;
    private static final long PLACE_COOLDOWN_MS = 2500L; // 2.5s between glyphs

    private static class GlyphData {
        Location location;
        long armedTime;
        int taskId;

        GlyphData(Location location, long armedTime, int taskId) {
            this.location = location;
            this.armedTime = armedTime;
            this.taskId = taskId;
        }
    }

    public FrostGlyph(String id, List<Integer> costList) {
        super(id, costList);

        iceBlue = new ParticleDustColored();
        iceBlue.setColor(new hm.zelha.particlesfx.util.Color(100, 180, 255));
        iceBlue.setCount(1);
        iceBlue.setOffset(0, 0, 0);

        iceWhite = new ParticleDustColored();
        iceWhite.setColor(new hm.zelha.particlesfx.util.Color(230, 240, 255));
        iceWhite.setCount(1);
        iceWhite.setOffset(0, 0, 0);

        iceCyan = new ParticleDustColored();
        iceCyan.setColor(new hm.zelha.particlesfx.util.Color(0, 220, 240));
        iceCyan.setCount(1);
        iceCyan.setOffset(0, 0, 0);

        snowball = new ParticleSnowDust();
        snowball.setCount(15);
        snowball.setOffset(0.8, 1.0, 0.8);
        snowball.setSpeed(0.3);

        critMagic = new ParticleCritMagic();
        critMagic.setCount(10);
        critMagic.setOffset(0.5, 0.5, 0.5);
        critMagic.setSpeed(0.4);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (e.getDamagePacket().getVictim() == null) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player attacker = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(attacker.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Cooldown
        if (user.hasMetadata(cooldownMetadataValue)) {
            long cd = (Long) user.getMetadataValue(cooldownMetadataValue);
            if (System.currentTimeMillis() < cd) return;
        }

        // Max glyphs: 2 + level/5 (2 at lv1-4, 3 at lv5-9, 4 at lv10-14, 5 at lv15-19, 6 at lv20)
        int maxGlyphs = 2 + level / 5;

        List<GlyphData> glyphs = activeGlyphs.computeIfAbsent(
                attacker.getUniqueId(), k -> new ArrayList<>());

        // Remove oldest if at max
        while (glyphs.size() >= maxGlyphs) {
            GlyphData oldest = glyphs.remove(0);
            Bukkit.getScheduler().cancelTask(oldest.taskId);
        }

        // Place glyph at victim's location
        Location glyphLoc = e.getDamagePacket().getVictim().getLocation().clone();
        user.setMetadata(cooldownMetadataValue, System.currentTimeMillis() + PLACE_COOLDOWN_MS);

        // Subtle placement sound
        attacker.playSound(attacker.getLocation(), XSound.BLOCK_SNOW_PLACE.parseSound(), 0.5F, 2.0F);

        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;
        ITeam ownerTeam = arena.getTeam(attacker);

        // Glyph parameters
        int lifetimeTicks = 200 + level * 10; // 10-15 seconds
        long armDelay = 20L; // 1 second to arm
        double damage = 1D + level * 0.1D;
        int slowDuration = 20 + level; // 1-2 seconds of slowness
        int slowAmplifier = level >= 10 ? 1 : 0;

        long armedTime = System.currentTimeMillis() + armDelay * 50L;

        int taskId = new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= lifetimeTicks || !attacker.isOnline()) {
                    glyphs.removeIf(g -> g.location.equals(glyphLoc));
                    cancel();
                    return;
                }

                // Subtle visual while armed (after arm delay)
                if (tick >= armDelay && tick % 5 == 0) {
                    playSnowflake(glyphLoc, 0.5D, tick);
                }

                // Check for enemies stepping on it (after arm delay)
                if (tick >= armDelay && tick % 3 == 0) {
                    for (Entity entity : glyphLoc.getWorld().getNearbyEntities(
                            glyphLoc, GLYPH_TRIGGER_RADIUS, 1.5D, GLYPH_TRIGGER_RADIUS)) {
                        if (!(entity instanceof Player)) continue;
                        if (entity.getUniqueId().equals(attacker.getUniqueId())) continue;

                        Player target = (Player) entity;
                        User targetUser = User.getUserFromUUID(target.getUniqueId());
                        if (targetUser == null) continue;

                        IArena tArena = util.getArenaByPlayer(target);
                        if (tArena == null) continue;
                        if (tArena.getTeam(target) == ownerTeam) continue;

                        // DETONATE!
                        playDetonation(glyphLoc);

                        glyphLoc.getWorld().playSound(glyphLoc,
                                XSound.BLOCK_GLASS_BREAK.parseSound(), 2.0F, 0.5F);
                        glyphLoc.getWorld().playSound(glyphLoc,
                                XSound.ENTITY_PLAYER_HURT_FREEZE.parseSound(), 1.5F, 1.0F);

                        CombatUtils.dealTrueDamage(damage, attacker, target);

                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOW, slowDuration, slowAmplifier, false, true));

                        target.sendMessage("§b§l[Frost Glyph] §f❄ Frozen trap triggered!");
                        attacker.sendMessage("§b§l[Frost Glyph] §f❄ " + target.getName() + " triggered your glyph!");

                        glyphs.removeIf(g -> g.location.equals(glyphLoc));
                        cancel();
                        return;
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L).getTaskId();

        glyphs.add(new GlyphData(glyphLoc, armedTime, taskId));
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (e.getDamagePacket().getVictim() == null) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player attacker = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(attacker.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Cooldown
        if (user.hasMetadata(cooldownMetadataValue)) {
            long cd = (Long) user.getMetadataValue(cooldownMetadataValue);
            if (System.currentTimeMillis() < cd) return;
        }

        // Max glyphs: 2 + level/5 (2 at lv1-4, 3 at lv5-9, 4 at lv10-14, 5 at lv15-19, 6 at lv20)
        int maxGlyphs = 2 + level / 5;

        List<GlyphData> glyphs = activeGlyphs.computeIfAbsent(
                attacker.getUniqueId(), k -> new ArrayList<>());

        // Remove oldest if at max
        while (glyphs.size() >= maxGlyphs) {
            GlyphData oldest = glyphs.remove(0);
            Bukkit.getScheduler().cancelTask(oldest.taskId);
        }

        // Place glyph at victim's location
        Location glyphLoc = e.getDamagePacket().getVictim().getLocation().clone();
        user.setMetadata(cooldownMetadataValue, System.currentTimeMillis() + PLACE_COOLDOWN_MS);

        // Subtle placement sound
        attacker.playSound(attacker.getLocation(), XSound.BLOCK_SNOW_PLACE.parseSound(), 0.5F, 2.0F);

        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;
        ITeam ownerTeam = arena.getTeam(attacker);

        // Glyph parameters
        int lifetimeTicks = 200 + level * 10; // 10-15 seconds
        long armDelay = 20L; // 1 second to arm
        double damage = 1D + level * 0.1D;
        int slowDuration = 20 + level; // 1-2 seconds of slowness
        int slowAmplifier = level >= 10 ? 1 : 0;

        long armedTime = System.currentTimeMillis() + armDelay * 50L;

        int taskId = new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= lifetimeTicks || !attacker.isOnline()) {
                    glyphs.removeIf(g -> g.location.equals(glyphLoc));
                    cancel();
                    return;
                }

                // Subtle visual while armed (after arm delay)
                if (tick >= armDelay && tick % 5 == 0) {
                    playSnowflake(glyphLoc, 0.5D, tick);
                }

                // Check for enemies stepping on it (after arm delay)
                if (tick >= armDelay && tick % 3 == 0) {
                    for (Entity entity : glyphLoc.getWorld().getNearbyEntities(
                            glyphLoc, GLYPH_TRIGGER_RADIUS, 1.5D, GLYPH_TRIGGER_RADIUS)) {
                        if (!(entity instanceof Player)) continue;
                        if (entity.getUniqueId().equals(attacker.getUniqueId())) continue;

                        Player target = (Player) entity;
                        User targetUser = User.getUserFromUUID(target.getUniqueId());
                        if (targetUser == null) continue;

                        IArena tArena = util.getArenaByPlayer(target);
                        if (tArena == null) continue;
                        if (tArena.getTeam(target) == ownerTeam) continue;

                        // DETONATE!
                        playDetonation(glyphLoc);

                        glyphLoc.getWorld().playSound(glyphLoc,
                                XSound.BLOCK_GLASS_BREAK.parseSound(), 2.0F, 0.5F);
                        glyphLoc.getWorld().playSound(glyphLoc,
                                XSound.ENTITY_PLAYER_HURT_FREEZE.parseSound(), 1.5F, 1.0F);

                        CombatUtils.dealTrueDamage(damage, attacker, target);

                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOW, slowDuration, slowAmplifier, false, true));

                        target.sendMessage("§b§l[Frost Glyph] §f❄ Frozen trap triggered!");
                        attacker.sendMessage("§b§l[Frost Glyph] §f❄ " + target.getName() + " triggered your glyph!");

                        glyphs.removeIf(g -> g.location.equals(glyphLoc));
                        cancel();
                        return;
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L).getTaskId();

        glyphs.add(new GlyphData(glyphLoc, armedTime, taskId));
    }

    // ==================== VISUAL EFFECTS ====================

    /**
     * Subtle 6-pointed snowflake on the ground.
     */
    private void playSnowflake(Location center, double size, int tick) {
        double rotation = Math.toRadians(tick * 0.5);
        for (int arm = 0; arm < 6; arm++) {
            double angle = rotation + Math.toRadians(60.0 * arm);

            // Main arm
            for (double d = 0.1D; d <= size; d += 0.25D) {
                double x = Math.cos(angle) * d;
                double z = Math.sin(angle) * d;
                iceBlue.display(center.clone().add(x, 0.05D, z));
            }

            // Small branches at 60% of arm length
            double branchStart = size * 0.6D;
            double branchLen = size * 0.25D;
            for (int side = -1; side <= 1; side += 2) {
                double branchAngle = angle + side * Math.toRadians(45);
                for (double d = 0; d <= branchLen; d += 0.2D) {
                    double bx = Math.cos(angle) * branchStart + Math.cos(branchAngle) * d;
                    double bz = Math.sin(angle) * branchStart + Math.sin(branchAngle) * d;
                    iceWhite.display(center.clone().add(bx, 0.05D, bz));
                }
            }
        }
    }

    /**
     * Dramatic ice crystal explosion on detonation.
     */
    private void playDetonation(Location center) {
        snowball.display(center.clone().add(0, 0.5, 0));
        critMagic.display(center.clone().add(0, 0.8, 0));

        // Expanding ice ring + vertical shards
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 8) {
                    cancel();
                    return;
                }

                // Expanding hexagonal ring
                double radius = 0.3D + tick * 0.3D;
                for (int i = 0; i < 6; i++) {
                    double angle = Math.toRadians(60.0 * i + tick * 15);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    iceCyan.display(center.clone().add(x, 0.1D, z));
                    iceBlue.display(center.clone().add(x, 0.3D, z));
                }

                // Vertical ice shards shooting up
                if (tick < 5) {
                    double shardY = tick * 0.5D;
                    for (int i = 0; i < 3; i++) {
                        double angle = Math.toRadians(120.0 * i);
                        double sx = Math.cos(angle) * 0.2D;
                        double sz = Math.sin(angle) * 0.2D;
                        iceWhite.display(center.clone().add(sx, shardY, sz));
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}
