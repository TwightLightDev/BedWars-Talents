package org.twightlight.talents.talents.built_in_talents.protective.directly;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.DebugService;

import java.util.List;

public class FallDamageReduction implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public FallDamageReduction(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        double totalReduction = level*0.025;
        EntityDamageEvent e = (EntityDamageEvent) params.get(1);
        Player p = (Player) e.getEntity();
        if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
            org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
            Double add = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_FALL_DAMAGE_REDUCTION);
            totalReduction += add;
            DebugService.debugMsg("Attribute modified, new value: " + totalReduction);
        }
        totalReduction = Math.min(totalReduction, 1);
        DebugService.debugMsg("Modified value to: " + ((Double) params.get(0)) * (1 - (totalReduction)));
        return ((Double) params.get(0)) * (1 - (totalReduction));
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
