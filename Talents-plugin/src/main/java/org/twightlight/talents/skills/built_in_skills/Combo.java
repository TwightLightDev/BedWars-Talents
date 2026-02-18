package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class Combo extends Skill {

    private String currentHitsMetadataValue = "skill.combo.currentHits";
    private String taskMetadataValue = "skill.combo.task";

    public Combo(String id, List<Integer> costList) {
        super(id, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;
        if (MysticalStand.isExtraAttack(e.getDamagePacket())) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            handleCombo(p, user, level);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedAttack(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getAttacker() == null) return;

        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());

        if (level != 0) {
            handleCombo(p, user, level);
        }
    }

    private void handleCombo(Player p, User user, int level) {
        IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p);
        Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
        StatsMap statsMap = arena1.getStatsMapOfUUID(user.getUuid());
        if (statsMap == null) return;

        // Initialize metadata
        if (!user.hasMetadata(currentHitsMetadataValue) || !(user.getMetadataValue(currentHitsMetadataValue) instanceof Integer)) {
            user.setMetadata(currentHitsMetadataValue, 0);
        }

        int currentHits = (Integer) user.getMetadataValue(currentHitsMetadataValue);

        // Remove previous stats
        modifyAttribute(statsMap, level, currentHits, false);

        // Cancel previous task
        if (user.getMetadataValue(taskMetadataValue) instanceof BukkitTask) {
            ((BukkitTask) user.getMetadataValue(taskMetadataValue)).cancel();
        }

        // Schedule reset task
        BukkitTask task = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            int hits = (Integer) user.getMetadataValue(currentHitsMetadataValue);
            IArena currentArena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p);
            Arena currentArena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(currentArena);
            StatsMap currentStatsMap = currentArena1.getStatsMapOfUUID(user.getUuid());
            if (currentStatsMap != null) {
                modifyAttribute(currentStatsMap, level, hits, false);
            }
            user.setMetadata(currentHitsMetadataValue, 0);
        }, 100L);

        user.setMetadata(taskMetadataValue, task);

        // Update combo counter
        if (Utility.rollChance(25.0D) && currentHits > 0) {
            currentHits--;
        } else if (currentHits < 7) {
            // Build combo (max 7 stacks)
            p.getWorld().playSound(p.getLocation(), XSound.ENTITY_PLAYER_ATTACK_SWEEP.parseSound(), 5.0F, 2.0F);
            currentHits++;
        }

        user.setMetadata(currentHitsMetadataValue, currentHits);

        // Apply new stats
        modifyAttribute(statsMap, level, currentHits, true);
    }

    private void modifyAttribute(StatsMap statsMap, int level, int currentHits, boolean add) {
        if (currentHits == 0) return;

        double penetration = 0.06 * (double) level * (double) currentHits;
        double critDamage = 0.125D * (double) level * (double) currentHits;
        double critChance = 0.05D * (double) level * (double) currentHits;

        if (add) {
            statsMap.getStatContainer(BaseStats.PENETRATION_RATIO.name()).add(penetration);
            statsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).add(critDamage);
            statsMap.getStatContainer(BaseStats.CRITICAL_RATE.name()).add(critChance);
        } else {
            statsMap.getStatContainer(BaseStats.PENETRATION_RATIO.name()).subtract(penetration);
            statsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).subtract(critDamage);
            statsMap.getStatContainer(BaseStats.CRITICAL_RATE.name()).subtract(critChance);
        }
    }
}

