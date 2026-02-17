package org.twightlight.talents.talents.built_in_talents.ranged.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;


import java.util.List;

public class Twins implements Talent<Double> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    private FixedMetadataValue metadata = new FixedMetadataValue(Talents.getInstance(), true);
    public Twins(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Double handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        if (params.get(0) instanceof EntityShootBowEvent) {
            DebugService.debugMsg("EntityShootBowEvent");
            EntityShootBowEvent e = (EntityShootBowEvent) params.get(0);
            if (e.getEntity() instanceof Player) {
                Player p = (Player) e.getEntity();
                p.setNoDamageTicks(9);
                Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    Arrow arrow = p.launchProjectile(Arrow.class);
                    arrow.setShooter(p);
                    arrow.setVelocity(p.getLocation().getDirection().normalize().multiply(e.getForce() * 3));
                    arrow.setCritical(((Arrow) e.getProjectile()).isCritical());
                    arrow.setMetadata("twin", metadata);
                }, 10);
            }
            return null;
        } else if (params.get(1) instanceof EntityDamageByEntityEvent) {
            DebugService.debugMsg("EntityDamageByEntityEvent");
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
            double base_damage = (double) params.get(0);
            if (e.getDamager().hasMetadata("twin")) {
                base_damage *= level * 0.015;
            }
            return base_damage;
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
