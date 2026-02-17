package org.twightlight.talents.talents.built_in_talents.ranged.skills;


import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Duplicate implements Talent<Boolean> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Duplicate(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Boolean handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        return Utility.rollChance(level * 4.25);
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
