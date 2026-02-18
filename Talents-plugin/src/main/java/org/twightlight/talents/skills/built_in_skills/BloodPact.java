package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleDustColored;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;

public class BloodPact extends Skill {

    private String cooldownMeta = "skill.bloodPact.cooldown";
    private String pactTaskMeta = "skill.bloodPact.task";
    private String pactVictimMeta = "skill.bloodPact.victim";
    private String pactOwnerMeta = "skill.bloodPact.owner";
    private String redirectLayerName = "bloodPactRedirectLayer";
    private String costLayerName = "bloodPactCostLayer";

    // Active pacts: ownerUUID -> victimUUID
    private Map<UUID, UUID> activePacts = new HashMap<>();

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    // Particles
    private ParticleDustColored bloodRed;
    private ParticleDustColored bloodDark;
    private ParticleDustColored bloodPulse;
    private ParticleDustColored chainLink;

    private static final long COOLDOWN_MS = 14000L;

    public BloodPact(String id, List<Integer> costList) {
        super(id, costList);

        bloodRed = new ParticleDustColored();
        bloodRed.setColor(new hm.zelha.particlesfx.util.Color(180, 0, 0));
        bloodRed.setCount(1);
        bloodRed.setOffset(0, 0, 0);

        bloodDark = new ParticleDustColored();
        bloodDark.setColor(new hm.zelha.particlesfx.util.Color(80, 0, 10));
        bloodDark.setCount(2);
        bloodDark.setOffset(0.2, 0.3, 0.2);
        bloodDark.setSpeed(0.02);

        bloodPulse = new ParticleDustColored();
        bloodPulse.setColor(new hm.zelha.particlesfx.util.Color(220, 20, 30));
        bloodPulse.setCount(1);
        bloodPulse.setOffset(0, 0, 0);

        chainLink = new ParticleDustColored();
        chainLink.setColor(new hm.zelha.particlesfx.util.Color(140, 10, 20));
        chainLink.setCount(1);
        chainLink.setOffset(0, 0, 0);
    }

    // ==================== PACT CREATION ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttackCreatePact(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player attacker = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(attacker.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;
        if (!attacker.isSneaking()) return;

        // Already has active pact?
        if (activePacts.containsKey(attacker.getUniqueId())) return;

        // Cooldown
        if (user.hasMetadata(cooldownMeta)) {
            long cd = (Long) user.getMetadataValue(cooldownMeta);
            if (System.currentTimeMillis() < cd) {
                long rem = (cd - System.currentTimeMillis()) / 1000;
                attacker.sendMessage("§4§l[Blood Pact] §7Cooldown: §c" + rem + "s");
                return;
            }
        }

        Player victim = (Player) e.getDamagePacket().getVictim();
        IArena arena = util.getArenaByPlayer(attacker);
        if (arena == null) return;

        activePacts.put(attacker.getUniqueId(), victim.getUniqueId());

        // Pact parameters
        int durationTicks = 80 + level * 4;
        double killHeal = 3.0D + level * 0.3D;

        // Sound
        attacker.getWorld().playSound(attacker.getLocation(),
                XSound.ENTITY_WITHER_SPAWN.parseSound(), 0.4F, 2.0F);
        attacker.getWorld().playSound(victim.getLocation(),
                XSound.BLOCK_ENCHANTMENT_TABLE_USE.parseSound(), 1.5F, 0.3F);

        attacker.sendMessage("§4§l[Blood Pact] §c⛓ Pact forged with §f" + victim.getName() + "§c!");
        victim.sendMessage("§4§l[Blood Pact] §c⛓ " + attacker.getName() + " §cbound you in a Blood Pact!");

        // Store pact info
        user.setMetadata(pactVictimMeta, victim.getUniqueId());

        User victimUser = User.getUserFromUUID(victim.getUniqueId());
        if (victimUser != null) {
            victimUser.setMetadata(pactOwnerMeta, attacker.getUniqueId());
        }

        // Visual task
        BukkitTask task = new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= durationTicks || !attacker.isOnline() || !victim.isOnline()
                        || victim.isDead() || attacker.isDead()
                        || !activePacts.containsKey(attacker.getUniqueId())) {

                    // Check if victim died during pact
                    boolean victimDied = victim.isDead() || !victim.isOnline();
                    if (victimDied && attacker.isOnline() && !attacker.isDead()) {
                        double newHp = Math.min(attacker.getMaxHealth(),
                                attacker.getHealth() + killHeal);
                        attacker.setHealth(newHp);
                        attacker.sendMessage("§4§l[Blood Pact] §a✦ Pact fulfilled! Healed §e"
                                + String.format("%.1f", killHeal) + " HP§a!");
                        attacker.playSound(attacker.getLocation(),
                                XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 1.0F, 1.5F);
                    } else {
                        if (attacker.isOnline())
                            attacker.sendMessage("§4§l[Blood Pact] §8Pact dissolved.");
                    }

                    // Cleanup
                    activePacts.remove(attacker.getUniqueId());
                    User u = User.getUserFromUUID(attacker.getUniqueId());
                    if (u != null) {
                        u.removeMetadata(pactVictimMeta);
                        u.setMetadata(cooldownMeta, System.currentTimeMillis() + COOLDOWN_MS);
                    }
                    User vu = User.getUserFromUUID(victim.getUniqueId());
                    if (vu != null) vu.removeMetadata(pactOwnerMeta);

                    cancel();
                    return;
                }

                // Chain visual between players
                if (tick % 2 == 0) {
                    playChainVisual(attacker, victim, tick);
                }

                // Heartbeat pulse every 20 ticks
                if (tick % 20 == 0) {
                    playHeartbeatPulse(attacker);
                    playHeartbeatPulse(victim);
                    attacker.playSound(attacker.getLocation(),
                            XSound.BLOCK_NOTE_BLOCK_BASEDRUM.parseSound(), 0.6F, 0.5F);
                    victim.playSound(victim.getLocation(),
                            XSound.BLOCK_NOTE_BLOCK_BASEDRUM.parseSound(), 0.4F, 0.5F);
                }

                // Blood drip on both
                if (tick % 5 == 0) {
                    bloodDark.display(attacker.getLocation().add(0, 1.8, 0));
                    bloodDark.display(victim.getLocation().add(0, 1.8, 0));
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);

        user.setMetadata(pactTaskMeta, task);
    }

    // ==================== DAMAGE REDIRECT: YOU take damage → portion goes to ENEMY ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.HIGH)
    public void onOwnerTakeMeleeDamage(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        Player victim = (Player) e.getDamagePacket().getVictim();
        redirectDamageToEnemy(victim);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.HIGH)
    public void onOwnerTakeRangedDamage(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        Player victim = (Player) e.getDamagePacket().getVictim();
        redirectDamageToEnemy(victim);
    }

    private void redirectDamageToEnemy(Player owner) {
        if (!activePacts.containsKey(owner.getUniqueId())) return;
        UUID victimUUID = activePacts.get(owner.getUniqueId());
        Player linkedEnemy = Bukkit.getPlayer(victimUUID);
        if (linkedEnemy == null || !linkedEnemy.isOnline()) return;

        User user = User.getUserFromUUID(owner.getUniqueId());
        if (user == null) return;
        int level = user.getSkillLevel(getSkillId());
        double redirectPercent = 0.08D + level * 0.006D;

        // Schedule the redirect damage for next tick to avoid recursion
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            if (linkedEnemy.isOnline() && !linkedEnemy.isDead()) {
                double redirectDmg = owner.getLastDamage() * redirectPercent;
                if (redirectDmg > 0.1D) {
                    CombatUtils.dealUndefinedDamage(linkedEnemy, redirectDmg,
                            org.bukkit.event.entity.EntityDamageEvent.DamageCause.MAGIC,
                            Map.of("blood-pact-redirect", true));

                    // Visual: red flash on enemy
                    bloodPulse.display(linkedEnemy.getLocation().add(0, 1.0, 0));
                    linkedEnemy.playSound(linkedEnemy.getLocation(),
                            XSound.ENTITY_PLAYER_HURT.parseSound(), 0.3F, 1.5F);
                }
            }
        }, 1L);
    }

    // ==================== BLOOD COST: ENEMY takes damage → portion goes to YOU ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.HIGH)
    public void onLinkedEnemyTakeMeleeDamage(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (e.getDamagePacket().getMetadataMap().containsKey("blood-pact-redirect")) return;
        if (e.getDamagePacket().getMetadataMap().containsKey("blood-pact-cost")) return;

        Player victim = (Player) e.getDamagePacket().getVictim();
        applyBloodCost(victim);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.HIGH)
    public void onLinkedEnemyTakeRangedDamage(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;
        if (e.getDamagePacket().getMetadataMap().containsKey("blood-pact-redirect")) return;
        if (e.getDamagePacket().getMetadataMap().containsKey("blood-pact-cost")) return;

        Player victim = (Player) e.getDamagePacket().getVictim();
        applyBloodCost(victim);
    }

    private void applyBloodCost(Player linkedEnemy) {
        User enemyUser = User.getUserFromUUID(linkedEnemy.getUniqueId());
        if (enemyUser == null || !enemyUser.hasMetadata(pactOwnerMeta)) return;

        UUID ownerUUID = (UUID) enemyUser.getMetadataValue(pactOwnerMeta);
        if (!activePacts.containsKey(ownerUUID)) return;

        Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner == null || !owner.isOnline() || owner.isDead()) return;

        User ownerUser = User.getUserFromUUID(ownerUUID);
        int level = ownerUser.getSkillLevel(getSkillId());
        double costPercent = 0.03D + level * 0.002D;

        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            if (owner.isOnline() && !owner.isDead()) {
                double costDmg = linkedEnemy.getLastDamage() * costPercent;
                if (costDmg > 0.1D) {
                    CombatUtils.dealUndefinedDamage(owner, costDmg,
                            org.bukkit.event.entity.EntityDamageEvent.DamageCause.MAGIC,
                            Map.of("blood-pact-cost", true));

                    bloodDark.display(owner.getLocation().add(0, 1.5, 0));
                }
            }
        }, 1L);
    }

    // ==================== VISUAL EFFECTS ====================

    private void playChainVisual(Player owner, Player enemy, int tick) {
        Location from = owner.getLocation().add(0, 1.0, 0);
        Location to = enemy.getLocation().add(0, 1.0, 0);

        if (!from.getWorld().equals(to.getWorld())) return;

        int segments = 8;
        double waveAmp = 0.15D + 0.1D * Math.sin(tick * 0.3D);

        // Perpendicular vector for wave effect
        org.bukkit.util.Vector dir = to.toVector().subtract(from.toVector());
        org.bukkit.util.Vector perp = new org.bukkit.util.Vector(-dir.getZ(), 0, dir.getX()).normalize();

        for (int i = 0; i <= segments; i++) {
            double t = (double) i / (double) segments;
            Location point = from.clone().add(dir.clone().multiply(t));

            // Sine wave along the chain
            double wave = Math.sin(t * Math.PI * 3 + tick * 0.4) * waveAmp;
            point.add(perp.clone().multiply(wave));

            // Droop in the middle
            point.add(0, -Math.sin(t * Math.PI) * 0.5D, 0);

            if (i % 2 == 0) {
                bloodRed.display(point);
            } else {
                chainLink.display(point);
            }
        }
    }

    private void playHeartbeatPulse(Player player) {
        Location center = player.getLocation().add(0, 1.0, 0);
        int points = 8;
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(45.0 * i);
            double x = Math.cos(angle) * 0.6D;
            double z = Math.sin(angle) * 0.6D;
            bloodPulse.display(center.clone().add(x, 0, z));
        }
    }
}
