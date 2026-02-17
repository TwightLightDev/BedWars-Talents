package org.twightlight.talents.skills.built_in_skills;

import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleNote;

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
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;

public class DefensiveAura extends Skill {

    private String availableMetadataValue = "skill.defensiveAura.available";
    private Map<UUID, double[]> lastValues = new HashMap<>();
    private Map<UUID, BukkitTask> lastTask = new HashMap<>();

    private ParticleNote note;

    public DefensiveAura(String id, List<Integer> costList) {
        super(id, costList);
        note = new ParticleNote();
        note.setCount(8);
        note.setOffset(1, 1, 1);
        note.setSpeed(1);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player p = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            if (!user.hasMetadata(availableMetadataValue) || !(user.getMetadataValue(availableMetadataValue) instanceof Boolean)) {
                user.setMetadata(availableMetadataValue, true);
            }

            boolean available = (Boolean) user.getMetadataValue(availableMetadataValue);
            StatsMap v = e.getDamagePacket().getVictimStatsMap();
            if (v == null) return;

            if (p.getHealth() / p.getMaxHealth() < 0.5D && available) {
                user.setMetadata(availableMetadataValue, false);

                // Remove previous buffs if exists
                if (lastValues.containsKey(p.getUniqueId())) {
                    double[] stored = lastValues.get(p.getUniqueId());
                    v.getStatContainer("DAMAGE_REDUCTION").subtract(stored[0]);
                    v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").subtract(stored[1]);
                    v.getStatContainer("BLOCK_CHANCE").subtract(stored[2]);
                    v.getStatContainer("BLOCK_RATIO").subtract(stored[3]);

                    lastTask.get(p.getUniqueId()).cancel();
                    lastValues.remove(p.getUniqueId());
                    lastTask.remove(p.getUniqueId());
                }

                // Add new buffs
                double critDamageReduction = 2 * (double) level;
                double blockPower = 0.75D * (double) level;

                v.getStatContainer("DAMAGE_REDUCTION").add((double) level);
                v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").add(critDamageReduction);
                v.getStatContainer("BLOCK_CHANCE").add((double) level);
                v.getStatContainer("BLOCK_RATIO").add(blockPower);

                playDefensiveAura(p, 60);
                p.getWorld().playSound(p.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 5.0F, 2.0F);

                BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    if (lastValues.containsKey(p.getUniqueId())) {
                        double[] stored = lastValues.get(p.getUniqueId());
                        v.getStatContainer("DAMAGE_REDUCTION").subtract(stored[0]);
                        v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").subtract(stored[1]);
                        v.getStatContainer("BLOCK_CHANCE").subtract(stored[2]);
                        v.getStatContainer("BLOCK_RATIO").subtract(stored[3]);

                        lastValues.remove(p.getUniqueId());
                    }
                    lastTask.remove(p.getUniqueId());

                    Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                        user.setMetadata(availableMetadataValue, true);
                    }, 100L);
                }, 60L);

                lastValues.put(p.getUniqueId(), new double[]{(double) level, critDamageReduction, (double) level, blockPower});
                lastTask.put(p.getUniqueId(), task);
            }
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player p = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            if (!user.hasMetadata(availableMetadataValue) || !(user.getMetadataValue(availableMetadataValue) instanceof Boolean)) {
                user.setMetadata(availableMetadataValue, true);
            }

            boolean available = (Boolean) user.getMetadataValue(availableMetadataValue);
            StatsMap v = e.getDamagePacket().getVictimStatsMap();
            if (v == null) return;

            if (p.getHealth() / p.getMaxHealth() < 0.5D && available) {
                user.setMetadata(availableMetadataValue, false);

                if (lastValues.containsKey(p.getUniqueId())) {
                    double[] stored = lastValues.get(p.getUniqueId());
                    v.getStatContainer("DAMAGE_REDUCTION").subtract(stored[0]);
                    v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").subtract(stored[1]);
                    v.getStatContainer("BLOCK_CHANCE").subtract(stored[2]);
                    v.getStatContainer("BLOCK_RATIO").subtract(stored[3]);

                    lastTask.get(p.getUniqueId()).cancel();
                    lastValues.remove(p.getUniqueId());
                    lastTask.remove(p.getUniqueId());
                }

                double critDamageReduction = 2D * (double) level;
                double blockPower = 0.75D * (double) level;

                v.getStatContainer("DAMAGE_REDUCTION").add(level);
                v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").add(critDamageReduction);
                v.getStatContainer("BLOCK_CHANCE").add(level);
                v.getStatContainer("BLOCK_RATIO").add(blockPower);

                playDefensiveAura(p, 60);
                p.getWorld().playSound(p.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 5.0F, 2.0F);

                BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    if (lastValues.containsKey(p.getUniqueId())) {
                        double[] stored = lastValues.get(p.getUniqueId());
                        v.getStatContainer("DAMAGE_REDUCTION").subtract(stored[0]);
                        v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").subtract(stored[1]);
                        v.getStatContainer("BLOCK_CHANCE").subtract(stored[2]);
                        v.getStatContainer("BLOCK_RATIO").subtract(stored[3]);

                        lastValues.remove(p.getUniqueId());
                    }
                    lastTask.remove(p.getUniqueId());

                    Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                        user.setMetadata(availableMetadataValue, true);
                    }, 100L);
                }, 60L);

                lastValues.put(p.getUniqueId(), new double[]{level, critDamageReduction, level, blockPower});
                lastTask.put(p.getUniqueId(), task);
            }
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onUndefinedAttack(UndefinedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player p = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            if (!user.hasMetadata(availableMetadataValue) || !(user.getMetadataValue(availableMetadataValue) instanceof Boolean)) {
                user.setMetadata(availableMetadataValue, true);
            }

            boolean available = (Boolean) user.getMetadataValue(availableMetadataValue);
            StatsMap v = e.getDamagePacket().getVictimStatsMap();
            if (v == null) return;

            if (p.getHealth() / p.getMaxHealth() < 0.5D && available) {
                user.setMetadata(availableMetadataValue, false);

                if (lastValues.containsKey(p.getUniqueId())) {
                    double[] stored = lastValues.get(p.getUniqueId());
                    v.getStatContainer("DAMAGE_REDUCTION").subtract(stored[0]);
                    v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").subtract(stored[1]);
                    v.getStatContainer("BLOCK_CHANCE").subtract(stored[2]);
                    v.getStatContainer("BLOCK_RATIO").subtract(stored[3]);

                    lastTask.get(p.getUniqueId()).cancel();
                    lastValues.remove(p.getUniqueId());
                    lastTask.remove(p.getUniqueId());
                }

                double critDamageReduction = 2D * (double) level;
                double blockPower = 0.75D * (double) level;

                v.getStatContainer("DAMAGE_REDUCTION").add((double) level);
                v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").add(critDamageReduction);
                v.getStatContainer("BLOCK_CHANCE").add((double) level);
                v.getStatContainer("BLOCK_RATIO").add(blockPower);

                playDefensiveAura(p, 60);
                p.getWorld().playSound(p.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 5.0F, 2.0F);

                BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    if (lastValues.containsKey(p.getUniqueId())) {
                        double[] stored = lastValues.get(p.getUniqueId());
                        v.getStatContainer("DAMAGE_REDUCTION").subtract(stored[0]);
                        v.getStatContainer("CRITICAL_DAMAGE_REDUCTION").subtract(stored[1]);
                        v.getStatContainer("BLOCK_CHANCE").subtract(stored[2]);
                        v.getStatContainer("BLOCK_RATIO").subtract(stored[3]);

                        lastValues.remove(p.getUniqueId());
                    }
                    lastTask.remove(p.getUniqueId());

                    Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                        user.setMetadata(availableMetadataValue, true);
                    }, 100L);
                }, 60L);

                lastValues.put(p.getUniqueId(), new double[]{(double) level, critDamageReduction, (double) level, blockPower});
                lastTask.put(p.getUniqueId(), task);
            }
        }
    }


    private void playDefensiveAura(final Entity entity, final int duration) {
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick++ < duration / 4 && (!(entity instanceof Player) || ((Player) entity).isOnline())) {
                    note.display(entity.getLocation().clone().add(0.0D, 1.0D, 0.0D));
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 4L);
    }
}

