package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import hm.zelha.particlesfx.particles.ParticleDustColored;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class BrutalRevenge extends Skill {

    private String lastKillerMetadataValue = "skill.brutalRevenge.lastKiller";
    private String damageLayerName = "brutalRevengeLayer";

    // Combat tracking
    private static Map<UUID, Long> combatMap = new HashMap<>();

    private ParticleDustColored berserkParticle;

    public BrutalRevenge(String id, List<Integer> costList) {
        super(id, costList);
        berserkParticle = new ParticleDustColored();
        berserkParticle.setColor(hm.zelha.particlesfx.util.Color.RED);
        berserkParticle.setCount(8);
        berserkParticle.setOffset(1, 1, 1);
        berserkParticle.setSpeed(1);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            handleDamageBoost(e.getDamagePacket().getVictim(), p, user, level, e.getDamagePacket().getDamageProperty());
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            handleDamageBoost(e.getDamagePacket().getVictim(), p, user, level, e.getDamagePacket().getDamageProperty());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKill(PlayerKillEvent e) {
        Player victim = e.getVictim();
        Player killer = e.getKiller();

        if (killer == null || victim == null) return;

        // Check if it's not a final kill
        if (e.getCause().isFinalKill()) return;

        User victimUser = User.getUserFromUUID(victim.getUniqueId());
        if (victimUser == null) return;
        if (!victimUser.getActivatingSkills().contains(getSkillId())) {
            return;
        }

        // Store the killer's UUID in victim's metadata
        victimUser.setMetadata(lastKillerMetadataValue, killer.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            combatMap.put(e.getDamager().getUniqueId(), System.currentTimeMillis());
            combatMap.put(e.getEntity().getUniqueId(), System.currentTimeMillis());
        }
    }

    private void handleDamageBoost(Entity victim, Player attacker, User attackerUser, int level, DamageProperty property) {
        if (!attackerUser.hasMetadata(lastKillerMetadataValue)) {
            return;
        }

        Object lastKillerObj = attackerUser.getMetadataValue(lastKillerMetadataValue);
        if (!(lastKillerObj instanceof UUID)) {
            return;
        }

        UUID lastKillerUUID = (UUID) lastKillerObj;

        // Check if the victim is the player's last killer
        if (victim.getUniqueId().equals(lastKillerUUID)) {
            double multiplier = 1.0D + (double) level * 0.015D * (double) (Utility.rollChance((double) level) ? 2 : 1);

            if (!property.hasLayer(damageLayerName)) {
                property.addLayer(damageLayerName, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer(damageLayerName, 1);
            }
            property.addValueToLayer(damageLayerName, multiplier - 1); // Subtract 1 because we're adding to multiplier

            if (isInCombat(attacker)) {
                playBerserkAura(attacker, 16);
            }
        }
    }

    private static boolean isInCombat(Player player) {
        return System.currentTimeMillis() - combatMap.getOrDefault(player.getUniqueId(), 0L) <= 5000L;
    }

    private void playBerserkAura(final Entity entity, final int duration) {
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick++ < duration / 4 && (!(entity instanceof Player) || ((Player) entity).isOnline())) {
                    berserkParticle.display(entity.getLocation().clone().add(0.0D, 1.0D, 0.0D));
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 4L);
    }
}

