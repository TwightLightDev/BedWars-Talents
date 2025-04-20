package org.twightlight.talents.talents.built_in_talents.intersection;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class ArmorPenetration implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;

    public ArmorPenetration(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        double past_damage = (Double) params.get(0);
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        Player p = null;
        PlayerAttribute attribute = null;
        if (e.getDamager() instanceof Arrow) {
            p = (Player) params.get(2);
            attribute = PlayerAttribute.ARROW_PENETRATION;
        } else if (e.getDamager() instanceof Player) {
            p = (Player) e.getDamager();
            attribute = PlayerAttribute.MELEE_PENETRATION;
        }
        if (e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            float totalPen = (float) (level * 1.2) / 100;

            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null && attribute != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                Double add = player.getAttribute(attribute);
                totalPen += add;
            }

            int armorpoint = Utility.getArmorPoints(victim);
            float afterReduction = armorpoint * (1 - totalPen);
            double multiplier = (1 - (afterReduction * 0.04)) / (1 - armorpoint * 0.04);
            DebugService.debugMsg("New armor value: " + afterReduction);
            return past_damage * multiplier;

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