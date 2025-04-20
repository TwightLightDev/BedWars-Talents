package org.twightlight.talents.talents.built_in_talents.ranged.skills;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class SpeedAspect implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public SpeedAspect(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(2);
        DebugService.debugMsg("Rolling chance for speed, current chance: " + level*2.5 + "%");
        if (Utility.rollChance(level*2.5)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Math.round((float) (level/4) + 2) * 20, 0));
            DebugService.debugMsg("Bingo, apply speed for: " + Math.round((float) (level/4) + 2) * 20 + " ticks!");
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
