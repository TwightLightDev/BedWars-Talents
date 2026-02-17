package org.twightlight.talents.talents.built_in_talents.melee.directly;

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

public class CriticalHit implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public CriticalHit(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        double value = (Double) params.get(0);
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        double chance = level;
        float multiplier = (float) 1.5;
        Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                get(TalentsCategory.Melee.getColumn()).
                get("CD").handle(
                        Talents.getInstance().getDatabase().getTalentLevel(
                                (Player) e.getDamager(),
                                TalentsCategory.Melee,
                                "CD")
                        , Arrays.asList(multiplier, e));
        if (returned instanceof Float) {
            multiplier = (Float) returned;
        }

        if (ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getDamager()) != null) {
            org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getDamager());
            Double total_addition_chance = player.getAttribute(PlayerAttribute.MELEE_CRITICAL_CHANCE);
            chance += total_addition_chance;
            DebugService.debugMsg("Attribute modified, new value: " + chance);
            Double total_addition = player.getAttribute(PlayerAttribute.MELEE_CRITICAL_DAMAGE);
            multiplier += total_addition;
            DebugService.debugMsg("Attribute modified, new value: " + multiplier);
        }

        if (e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Protective.getColumn()).
                    get("CDR").handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    victim,
                                    TalentsCategory.Protective,
                                    "CDR")
                            , Arrays.asList(multiplier, e));
            if (returned instanceof Float) {
                multiplier = (Float) returned;
            }

            if (ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getEntity()) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getEntity());
                Double total_addition = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_CRITICAL_DAMAGE_REDUCTION);
                multiplier -= total_addition;
                DebugService.debugMsg("Attribute modified, new value: " + multiplier);
            }
        }

        multiplier = Math.max(multiplier, 1);

        DebugService.debugMsg("Rolling for critical hit, current chance: " + chance + "%");
        if (Utility.rollChance(chance)) {
            value *= multiplier;
            DebugService.debugMsg("Bingo! Critical called. New damage: " + value);
        }
        return value;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
