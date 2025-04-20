package org.twightlight.talents.talents.built_in_talents.melee.directly;

import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;

import java.util.List;

public class MeleeAttackDamage implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public MeleeAttackDamage(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        double value = (Double) params.get(0);
        DebugService.debugMsg("Modified value from " + value + " to " + (float) (value + level* 0.05));
        return value + (level * 0.05);
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
