package org.twightlight.talents.talents.built_in_talents.supportive;

import org.twightlight.talents.internal.Player;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class Shield implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Shield(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player player = (Player) params.get(0);
        if (((Integer) params.get(1)) <= 5) {
            Double current_SC = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_BLOCK_CHANCE);
            Double current_SR = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_BLOCK_AMOUNT);

            Double add = level * 0.1D;
            Double add1 = level * 0.001D;

            player.modifyAttribute(PlayerAttribute.TOTAL_ADDITIONAL_BLOCK_CHANCE, current_SC + add);
            player.modifyAttribute(PlayerAttribute.TOTAL_ADDITIONAL_BLOCK_AMOUNT, current_SR + add1);

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