package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class BetterGoldenApple implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public BetterGoldenApple(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        DebugService.debugMsg("Rolling chance for golden apple boost, current chance: " + level*2.5 + "%");
        if (Utility.rollChance(level*2.5)) {
            p.removePotionEffect(PotionEffectType.ABSORPTION);
            p.removePotionEffect(PotionEffectType.REGENERATION);
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 180 * 20, 2));
            DebugService.debugMsg("Bingo, enchanted gapple!");
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
