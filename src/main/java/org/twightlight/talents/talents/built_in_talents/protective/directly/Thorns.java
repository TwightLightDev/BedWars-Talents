package org.twightlight.talents.talents.built_in_talents.protective.directly;

import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;

import java.util.List;

public class Thorns implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Thorns(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        DebugService.debugMsg("Modified value to: " + ((Double) params.get(0)) * (1 - (level*2.5)/100));
        return ((Double) params.get(0)) * (1 - (level*2.5)/100);
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
