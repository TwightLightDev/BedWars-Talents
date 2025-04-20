package org.twightlight.talents.listeners.general;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.DebugService;

import java.util.Arrays;
import java.util.List;

public class GeneralFallCheck implements Listener {
    public static List<String> VICTIM_TALENTS_DEFEND = Arrays.asList("FDR");

    @EventHandler
    public void onPlayerFall(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (e.getEntity() instanceof Player) {
                e.setDamage(calculateReduction(e.getDamage(), e));
                if (e.getDamage() <= 0) {
                    e.setCancelled(true);
                }
            }
        }
    }

    private double calculateReduction(double base_value, EntityDamageEvent e) {
        DebugService.debugMsg("Started calculate reduction amount, starting value: " + base_value);
        for (String id : VICTIM_TALENTS_DEFEND) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Protective.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    (Player) e.getEntity(),
                                    TalentsCategory.Protective,
                                    id)
                            , Arrays.asList(base_value, e));
            if (returned instanceof Double) {
                base_value = (Double) returned;
            }
        }
        DebugService.debugMsg("Finished calculation, final value: " + base_value);
        return base_value;
    }
}
