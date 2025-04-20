package org.twightlight.talents.talents.built_in_talents.supportive;

import org.twightlight.talents.internal.Player;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class Warrior implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Warrior(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player player = (Player) params.get(0);
        if (((Integer) params.get(1)) <= 5) {
            Double current = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH);

            Double add = level * 0.05D;
            player.modifyAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH, current + add);
            org.bukkit.entity.Player p = ((org.bukkit.entity.Player) params.get(2));
            p.setMaxHealth(p.getMaxHealth() + add);
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