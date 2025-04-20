package org.twightlight.talents.talents.built_in_talents.melee.skills;

import fr.mrmicky.fastparticles.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Thunder implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;

    public Thunder(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        DebugService.debugMsg("Rolling chance for striking victim, current chance: " + level * 1.5 + "%");
        if (Utility.rollChance(level * 1.5)) {
            DebugService.debugMsg("Bingo, Striking victim!");
            EntityDamageEvent lightningEvent = new EntityDamageEvent(
                    e.getEntity(),
                    EntityDamageEvent.DamageCause.LIGHTNING,
                    0
            );
            Bukkit.getPluginManager().callEvent(lightningEvent);
            if (!lightningEvent.isCancelled()) {
                LivingEntity entity = ((LivingEntity) e.getEntity());
                LivingEntity attacker = ((LivingEntity) e.getDamager());
                entity.setHealth(Math.max(0, entity.getHealth() - 2));
                attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 1));
                for (int i = 0; i < 6; i++) {
                    ParticleType.of("FLAME").spawn((Player) e.getDamager(), e.getEntity().getLocation().add(0, i * 0.2 * (6 - i) + 1.5, 0), 1, 0, 0, 0, 0);
                    if (entity instanceof Player) {
                        ParticleType.of("FLAME").spawn((Player) e.getEntity(), e.getEntity().getLocation().add(0, i * 0.2 * (6 - i) + 1.5, 0), 1, 0, 0, 0, 0);
                    }
                }
            }
        }

        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}