package org.twightlight.talents.talents.built_in_talents.ranged.skills;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;

import java.util.List;

public class ArrowKnockback implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public ArrowKnockback(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);

        Arrow arrow = (Arrow) e.getDamager();

        Entity victim = e.getEntity();
        Vector arrowVelocity = arrow.getVelocity().normalize();

        double bonusPercent = level * 0.02;
        double force = 0.6 * bonusPercent;

        Vector knockback = arrowVelocity.multiply(force);
        knockback.setY(bonusPercent * 0.3);

        victim.setVelocity(victim.getVelocity().add(knockback));
        DebugService.debugMsg("Modifying arrow's knockback. Old velocity: " + arrowVelocity + ". New velocity: " + victim.getVelocity());

        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
