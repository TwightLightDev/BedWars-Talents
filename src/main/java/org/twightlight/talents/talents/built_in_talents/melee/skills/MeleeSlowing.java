package org.twightlight.talents.talents.built_in_talents.melee.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class MeleeSlowing implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public MeleeSlowing(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        DebugService.debugMsg("Rolling chance for slowing victim, current chance: " + level*1.5 + "%");
        if (Utility.rollChance(level*1.5)) {
            ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Math.round((float) (level/5) + 1) * 20, 0));
            DebugService.debugMsg("Bingo, slow the target for: " + Math.round((float) (level/5) + 1) * 20 + " ticks!");
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
