package org.twightlight.talents.talents.built_in_talents.intersection;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.Arrays;
import java.util.List;

public class MultiLifeSteal implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public MultiLifeSteal(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        TalentsCategory cat = (TalentsCategory) params.get(3);
        Player p = null;
        PlayerAttribute attribute = null;
        if (e.getDamager() instanceof Arrow) {
            p = (Player) params.get(2);
            attribute = PlayerAttribute.ARROW_LIFE_STEALS;
        } else if (e.getDamager() instanceof Player) {
            p = (Player) e.getDamager();
            attribute = PlayerAttribute.MELEE_LIFE_STEALS;
        }
        float coefficient = (float) 0.015*level;
        if (p == null) {
            return ((Double) params.get(0)) * coefficient;
        }
        Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                get(cat.getColumn()).
                get("SLS").handle(
                        Talents.getInstance().getDatabase().getTalentLevel(
                                p,
                                cat,
                                "SLS")
                        , Arrays.asList(coefficient, e));
        if (returned instanceof Float) {
            coefficient = (Float) returned;
        }
        if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null && attribute != null) {
            org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
            Double add = player.getAttribute(attribute);
            coefficient += add;
        }
        return ((Double) params.get(0)) * coefficient;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
