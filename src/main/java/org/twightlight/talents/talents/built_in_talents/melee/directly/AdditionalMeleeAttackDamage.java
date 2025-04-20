package org.twightlight.talents.talents.built_in_talents.melee.directly;

import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;

import java.util.List;

public class AdditionalMeleeAttackDamage implements Talent<Float> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public AdditionalMeleeAttackDamage(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Float handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        double value = (Float) params.get(0);
        DebugService.debugMsg("Modified value from " + value + " to " + (float) (value + level * 0.005));
        return (float) (value + level * 0.005);
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
