package org.twightlight.talents.talents.built_in_talents.supportive;

import org.twightlight.talents.internal.Player;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class Assassin implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Assassin(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player player = (Player) params.get(0);
        if (((Integer) params.get(1)) <= 5) {
            Double current_CA = player.getAttribute(PlayerAttribute.ARROW_CRITICAL_CHANCE);
            Double current_CH = player.getAttribute(PlayerAttribute.MELEE_CRITICAL_CHANCE);

            Double add = level * 0.1D;
            player.modifyAttribute(PlayerAttribute.ARROW_CRITICAL_CHANCE, current_CA + add);
            player.modifyAttribute(PlayerAttribute.MELEE_CRITICAL_CHANCE, current_CH + add);

            Double current_CDA = player.getAttribute(PlayerAttribute.ARROW_CRITICAL_DAMAGE);
            Double current_CDH = player.getAttribute(PlayerAttribute.MELEE_CRITICAL_DAMAGE);

            Double add1 = level * 0.0025D;
            player.modifyAttribute(PlayerAttribute.ARROW_CRITICAL_DAMAGE, current_CDA + add1);
            player.modifyAttribute(PlayerAttribute.MELEE_CRITICAL_DAMAGE, current_CDH + add1);
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