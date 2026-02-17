package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import hm.zelha.particlesfx.particles.ParticleDustColored;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.arenas.ArenaManager;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.listeners.general.GeneralCombatCheck;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;

public class Berserk extends Skill {

    private String lastValueMetadataValue = "skill.berserk.lastValue";
    private Map<UUID, BukkitTask> taskMap = new HashMap<>();

    private ParticleDustColored berserkParticle;

    public Berserk(String id, List<Integer> costList) {
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
        if (!(e.getDamagePacket().getAttacker() instanceof Player)) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            startBerserkTask(p, user, level);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getAttacker() instanceof Player)) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            startBerserkTask(p, user, level);
        }
    }

    private void startBerserkTask(Player p, User user, int level) {
        // Only start task if not already running
        if (taskMap.containsKey(p.getUniqueId())) {
            return;
        }

        // Initialize metadata
        if (!user.hasMetadata(lastValueMetadataValue) || !(user.getMetadataValue(lastValueMetadataValue) instanceof Double)) {
            user.setMetadata(lastValueMetadataValue, 0.0D);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Talents.getInstance(), () -> {
            User currentUser = User.getUserFromUUID(p.getUniqueId());
            if (currentUser != null && p.isOnline()) {
                IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p);
                Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
                StatsMap statsMap = arena1.getStatsMapOfUUID(currentUser.getUuid());
                if (statsMap == null) return;

                double lastValue = (Double) currentUser.getMetadataValue(lastValueMetadataValue);

                // Remove previous modification
                if (lastValue > 0.0D) {
                    modifyAttribute(statsMap, level, lastValue, false);
                }

                // Calculate new multiplier based on missing health
                int multiplier;
                try {
                    multiplier = (int) ((1.0D - p.getHealth() / p.getMaxHealth()) / 0.01D);
                } catch (Exception ex) {
                    multiplier = 0;
                }

                // Apply new modification
                if (multiplier > 0) {
                    modifyAttribute(statsMap, level, (double) multiplier, true);
                }

                currentUser.setMetadata(lastValueMetadataValue, (double) multiplier);

                // Play visual effect if in combat
                if (GeneralCombatCheck.isInCombat(p)) {
                    playBerserkAura(p, 16);
                }

            } else {
                // Player offline or user not found - cleanup
                if (currentUser != null) {
                    double lastValue = (Double) currentUser.getMetadataValue(lastValueMetadataValue);
                    if (lastValue > 0.0D) {
                        IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p);
                        Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
                        StatsMap statsMap = arena1.getStatsMapOfUUID(currentUser.getUuid());
                        if (statsMap != null) {
                            modifyAttribute(statsMap, level, lastValue, false);
                        }
                    }
                    currentUser.setMetadata(lastValueMetadataValue, 0.0D);
                }

                taskMap.get(p.getUniqueId()).cancel();
                taskMap.remove(p.getUniqueId());
            }
        }, 0L, 20L);

        taskMap.put(p.getUniqueId(), task);
    }

    private void modifyAttribute(StatsMap statsMap, int level, double value, boolean add) {
        double lifesteal = (double) level * value * 1.5E-2D;
        double damageReduction = (double) level * value * 5.0E-3D;

        if (add) {
            statsMap.getStatContainer("MELEE_LIFESTEAL").add(lifesteal);
            statsMap.getStatContainer("DAMAGE_REDUCTION").add(damageReduction);
        } else {
            statsMap.getStatContainer("MELEE_LIFESTEAL").subtract(lifesteal);
            statsMap.getStatContainer("DAMAGE_REDUCTION").subtract(damageReduction);
        }
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

