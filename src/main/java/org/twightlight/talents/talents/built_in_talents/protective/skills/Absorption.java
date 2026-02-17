package org.twightlight.talents.talents.built_in_talents.protective.skills;

import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Absorption implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Absorption(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        LivingEntity entity = (LivingEntity) e.getEntity();
        DebugService.debugMsg("Rolling chance for absorption, current chance: " + level*2 + "%");
        if (Utility.rollChance(level*2)) {
            DebugService.debugMsg("Bingo! You got 2 yellow hearts!");
            EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
            handle.setAbsorptionHearts(4.0F);
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
