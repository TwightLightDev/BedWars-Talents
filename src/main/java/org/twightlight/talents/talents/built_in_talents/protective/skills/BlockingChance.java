package org.twightlight.talents.talents.built_in_talents.protective.skills;

import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;

import java.util.List;

public class BlockingChance implements Talent<Float> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public BlockingChance(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Float handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        double value = (Float) params.get(0);
        DebugService.debugMsg("Modified value from " + value + " to " + (float) (value + level));
        return (float) (value + level);
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
