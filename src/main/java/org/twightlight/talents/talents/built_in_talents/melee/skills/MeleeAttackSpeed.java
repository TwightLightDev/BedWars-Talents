package org.twightlight.talents.talents.built_in_talents.melee.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class MeleeAttackSpeed implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public MeleeAttackSpeed(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        LivingEntity entity = (LivingEntity) e.getEntity();
        DebugService.debugMsg("Rolling for fast cooldown, current chance: " + level * 4 + "%");
        if (Utility.rollChance(level * 4)) {
            DebugService.debugMsg("Bingo! 35% fast cooldown activated!");
            entity.setNoDamageTicks(13);
        } else {
            entity.setNoDamageTicks(20);
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
