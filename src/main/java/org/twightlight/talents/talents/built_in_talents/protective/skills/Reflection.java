package org.twightlight.talents.talents.built_in_talents.protective.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Reflection implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Reflection(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        double past_damage = ((Double) params.get(0));
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        LivingEntity entity = (LivingEntity) e.getEntity();
        if (!(e.getDamager() instanceof LivingEntity)) {
            return past_damage;
        }
        LivingEntity damager = (LivingEntity) e.getDamager();
        DebugService.debugMsg("Rolling chance for reflection, current chance: " + level*1.25 + "%");
        if (Utility.rollChance(level*1.25)) {
            DebugService.debugMsg("Bingo! Reflect the damage!");
            damager.damage(3, entity);
            past_damage *= 0.75;
        }
        return past_damage;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
