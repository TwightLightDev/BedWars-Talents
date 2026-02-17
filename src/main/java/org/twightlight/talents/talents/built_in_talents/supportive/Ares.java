package org.twightlight.talents.talents.built_in_talents.supportive;

import org.twightlight.talents.internal.Player;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class Ares implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Ares(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player player = (Player) params.get(0);
        if (((Integer) params.get(1)) <= 5) {
            Double current_AD = player.getAttribute(PlayerAttribute.ARROW_DAMAGE);
            Double current_MD = player.getAttribute(PlayerAttribute.MELEE_DAMAGE);

            Double add = level * 0.025D;
            player.modifyAttribute(PlayerAttribute.ARROW_DAMAGE, current_AD + add);
            player.modifyAttribute(PlayerAttribute.MELEE_DAMAGE, current_MD + add);

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