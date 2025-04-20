package org.twightlight.talents.talents.built_in_talents.protective.skills;

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

public class Blocking  implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Blocking(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        float percentage = (float) level;
        Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                get(TalentsCategory.Protective.getColumn()).
                get("BLC").handle(
                        Talents.getInstance().getDatabase().getTalentLevel(
                                (Player) e.getEntity(),
                                TalentsCategory.Protective,
                                "BLC")
                        , Arrays.asList(percentage, e));
        if (returned instanceof Float) {
            percentage = (Float) returned;
        }
        DebugService.debugMsg("Rolling chance for blocking, current chance: " + percentage + "%");
        double reduction = level*0.025;
        if (ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getEntity()) != null) {
            org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getEntity());
            Double total_addition_chance = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_BLOCK_CHANCE);
            percentage += total_addition_chance;
            DebugService.debugMsg("Attribute modified, new value: " + percentage);
            Double total_addition = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_BLOCK_AMOUNT);
            reduction += total_addition;
            DebugService.debugMsg("Attribute modified, new value: " + reduction);
        }


        double past_damage = ((Double) params.get(0));
        if (Utility.rollChance(percentage)) {
            DebugService.debugMsg("Bingo! Attack blocked!");
            DebugService.debugMsg("Modified value to: " + past_damage * (1 - reduction));
            return past_damage * (1 - (reduction));
        }
        return past_damage;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
