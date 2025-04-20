package org.twightlight.talents.talents.built_in_talents.protective.skills;

import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class YellowHearts implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public YellowHearts(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        EntityLiving handle = ((CraftLivingEntity) p).getHandle();
        handle.setAbsorptionHearts((float) level);
        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
