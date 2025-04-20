package org.twightlight.talents.talents.built_in_talents.special;

import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class KingOfBedWars implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;

    public KingOfBedWars(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {

        if (level <= 0) {
            return null;
        }
        if (!Utility.rollChance(level * 2.5)) {
            return null;
        }
        double base_damage = (double) params.get(0);
        int mode = (int) params.get(1);
        if (mode == 1) {
            return base_damage * (1 + (level * 0.01));
        } else if (mode == 2) {
            return base_damage * (1 - (level * 0.008));
        }
        return base_damage;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}