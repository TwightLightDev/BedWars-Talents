package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.Arrays;
import java.util.List;

public class FireballDamage  implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public FireballDamage(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        double base_value = (double) params.get(0);
        return base_value * (1 + level * 0.025);
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}