package org.twightlight.talents.talents.built_in_talents.protective.directly;

import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class SelfRegen  implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public SelfRegen(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        return ((Double) params.get(0)) * (1 + level / 50);
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}