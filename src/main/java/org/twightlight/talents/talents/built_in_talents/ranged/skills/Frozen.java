package org.twightlight.talents.talents.built_in_talents.ranged.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Frozen implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Frozen(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        if (!((e.getEntity()) instanceof Player)) {
            return null;
        }
        Player p = (Player) params.get(2);
        DebugService.debugMsg("Rolling chance for frozen victim, current chance: " + level*1.5 + "%");
        if (Utility.rollChance(level*1.5)) {
            (e.getEntity()).setMetadata("frozen", new FixedMetadataValue(Talents.getInstance(), true));
        }
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            (e.getEntity()).removeMetadata("frozen", Talents.getInstance());
        }, 20);
        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
