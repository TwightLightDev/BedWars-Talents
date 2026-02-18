package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleSpell;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
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

public class CursedShot extends Skill {

    private String cooldownMeta = "skill.cursedTouch.cooldown";

    // Track all active plagues: victimUUID -> PlagueData
    private Map<UUID, PlagueData> activePlagues = new HashMap<>();

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Particles
    private ParticleDustColored plagueGreen;
    private ParticleDustColored plagueDark;
    private ParticleDustColored tendrilGreen;
    private ParticleSpell sporeSpread;

    private static final double SPREAD_RADIUS = 3.0D;
    private static final long SPREAD_PROXIMITY_MS = 1500L; // 1.5s near to spread
    private static final long APPLY_COOLDOWN_MS = 8000L; // 4s between applications

    private static class PlagueData {
        UUID ownerUUID;
        ITeam ownerTeam;
        double dot;
        int remainingTicks;
        int spreadDepth;
        int maxSpreadDepth;
        int taskId;
        Map<UUID, Long> proximityTimers = new HashMap<>(); // nearby enemy -> first detected time

        PlagueData(UUID ownerUUID, ITeam ownerTeam, double dot,
                   int remainingTicks, int spreadDepth, int maxSpreadDepth) {
            this.ownerUUID = ownerUUID;
            this.ownerTeam = ownerTeam;
            this.dot = dot;
            this.remainingTicks = remainingTicks;
            this.spreadDepth = spreadDepth;
            this.maxSpreadDepth = maxSpreadDepth;
        }
    }

    public CursedShot(String id, List<Integer> costList) {
        super(id, costList);

        plagueGreen = new ParticleDustColored();
        plagueGreen.setColor(new hm.zelha.particlesfx.util.Color(40, 180, 30));
        plagueGreen.setCount(2);
        plagueGreen.setOffset(0.25, 0.5, 0.25);
        plagueGreen.setSpeed(0.02);

        plagueDark = new ParticleDustColored();
        plagueDark.setColor(new hm.zelha.particlesfx.util.Color(20, 80, 10));
        plagueDark.setCount(1);
        plagueDark.setOffset(0, 0, 0);

        tendrilGreen = new ParticleDustColored();
        tendrilGreen.setColor(new hm.zelha.particlesfx.util.Color(80, 220, 50));
        tendrilGreen.setCount(1);
        tendrilGreen.setOffset(0, 0, 0);

        sporeSpread = new ParticleSpell();
        sporeSpread.setCount(15);
        sporeSpread.setOffset(0.8, 0.8, 0.8);
        sporeSpread.setSpeed(0.2);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player attacker = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(attacker.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        // Cooldown per-victim (don't re-plague already plagued targets)
        Player victim = (Player) e.getDamagePacket().getVictim();
        if (activePlagues.containsKey(victim.getUniqueId())) return;

        if (user.hasMetadata(cooldownMeta)) {
            long cd = (Long) user.getMetadataValue(cooldownMeta);
            if (System.currentTimeMillis() < cd) return;
        }

        user.setMetadata(cooldownMeta, System.currentTimeMillis() + APPLY_COOLDOWN_MS);

        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;
        ITeam ownerTeam = arena.getTeam(attacker);

        // Plague parameters
        double baseDot = 1.2D + level * 0.14D;
        int duration = 80 + level * 4; // 4s to 8s
        int maxSpread = 1 + level / 7; // 1 at lv1-6, 2 at lv7-13, 3 at lv14-20

        applyPlague(victim, attacker.getUniqueId(), ownerTeam, baseDot, duration, 0, maxSpread);

        attacker.playSound(attacker.getLocation(), XSound.ENTITY_ZOMBIE_INFECT.parseSound(), 1.0F, 1.5F);
        attacker.sendMessage("§2§l[Cursed Touch] §a☠ Plagued §f" + victim.getName() + "§a!");
        victim.sendMessage("§2§l[Cursed Touch] §c☠ You are infected!");
    }

    private void applyPlague(Player victim, UUID ownerUUID, ITeam ownerTeam,
                             double dot, int duration, int depth, int maxDepth) {
        if (activePlagues.containsKey(victim.getUniqueId())) return;

        PlagueData data = new PlagueData(ownerUUID, ownerTeam, dot, duration, depth, maxDepth);

        int taskId = new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= data.remainingTicks || !victim.isOnline() || victim.isDead()) {
                    activePlagues.remove(victim.getUniqueId());
                    if (victim.isOnline())
                        victim.sendMessage("§2§l[Cursed Touch] §8Plague faded.");
                    cancel();
                    return;
                }

                // Visual: green swirl around victim
                if (tick % 3 == 0) {
                    double angle = Math.toRadians(tick * 12);
                    double r = 0.5D;
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    double y = 0.5D + Math.sin(tick * 0.2) * 0.5D;
                    plagueGreen.display(victim.getLocation().add(x, y, z));
                }

                // DoT every 20 ticks (1 second)
                if (tick % 20 == 0 && tick > 0) {
                    CombatUtils.dealUndefinedDamage(victim, data.dot,
                            EntityDamageEvent.DamageCause.POISON,
                            Map.of("cursed-touch-dot", true));

                    victim.playSound(victim.getLocation(),
                            XSound.ENTITY_PLAYER_HURT.parseSound(), 0.3F, 0.5F);
                }

                // Spread check every 10 ticks
                if (tick % 10 == 0 && data.spreadDepth < data.maxSpreadDepth) {
                    checkAndSpread(victim, data);
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L).getTaskId();

        data.taskId = taskId;
        activePlagues.put(victim.getUniqueId(), data);
    }

    private void checkAndSpread(Player plagued, PlagueData data) {
        long now = System.currentTimeMillis();

        for (Entity entity : plagued.getLocation().getWorld().getNearbyEntities(
                plagued.getLocation(), SPREAD_RADIUS, SPREAD_RADIUS, SPREAD_RADIUS)) {
            if (!(entity instanceof Player)) continue;
            if (entity.getUniqueId().equals(plagued.getUniqueId())) continue;
            if (entity.getUniqueId().equals(data.ownerUUID)) continue;
            if (activePlagues.containsKey(entity.getUniqueId())) continue;

            Player nearby = (Player) entity;
            User nearbyUser = User.getUserFromUUID(nearby.getUniqueId());
            if (nearbyUser == null) continue;

            IArena nArena = util.getArenaByPlayer(nearby);
            if (nArena == null) continue;
            // Don't spread to owner's team
            if (data.ownerTeam != null && nArena.getTeam(nearby) == data.ownerTeam) continue;

            // Proximity timer
            if (!data.proximityTimers.containsKey(nearby.getUniqueId())) {
                data.proximityTimers.put(nearby.getUniqueId(), now);
            }

            long firstSeen = data.proximityTimers.get(nearby.getUniqueId());

            // Visual: tendril reaching toward nearby enemy
            playTendril(plagued.getLocation().add(0, 1, 0),
                    nearby.getLocation().add(0, 1, 0));

            if (now - firstSeen >= SPREAD_PROXIMITY_MS) {
                // SPREAD!
                double spreadDot = data.dot * 0.7D; // 30% decay per spread
                int spreadDuration = (int) (data.remainingTicks * 0.7D);

                applyPlague(nearby, data.ownerUUID, data.ownerTeam,
                        spreadDot, spreadDuration, data.spreadDepth + 1, data.maxSpreadDepth);

                // Spread visual
                sporeSpread.display(nearby.getLocation().add(0, 1, 0));
                nearby.getWorld().playSound(nearby.getLocation(),
                        XSound.ENTITY_ZOMBIE_INFECT.parseSound(), 0.8F, 2.0F);
                nearby.sendMessage("§2§l[Cursed Touch] §c☠ Plague spread to you!");

                Player owner = Bukkit.getPlayer(data.ownerUUID);
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage("§2§l[Cursed Touch] §a☠ Plague spread to §f"
                            + nearby.getName() + "§a! §7(depth " + (data.spreadDepth + 1) + ")");
                    owner.playSound(owner.getLocation(),
                            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 0.5F, 1.5F);
                }

                data.proximityTimers.remove(nearby.getUniqueId());
            }
        }

        // Clean timers for players no longer nearby
        data.proximityTimers.entrySet().removeIf(entry -> {
            Player p = Bukkit.getPlayer(entry.getKey());
            return p == null || !p.isOnline()
                    || p.getLocation().distance(plagued.getLocation()) > SPREAD_RADIUS + 1.0D;
        });
    }

    private void playTendril(Location from, Location to) {
        if (!from.getWorld().equals(to.getWorld())) return;
        int segs = 4;
        for (int i = 0; i <= segs; i++) {
            double t = (double) i / (double) segs;
            Location point = from.clone().add(to.clone().subtract(from).toVector().multiply(t));
            // Wavy tendril
            point.add(0, Math.sin(t * Math.PI * 2) * 0.2D, 0);
            tendrilGreen.display(point);
        }
    }
}

