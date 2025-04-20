package org.twightlight.talents.listeners.general;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;
import java.util.List;


public class GeneralHealEvent implements Listener {
    public static List<String> TALENTS_HEAL = Arrays.asList("SRG", "IHE");

    @EventHandler
    public void onHeal(EntityRegainHealthEvent e) {
        if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            if (e.getEntity() instanceof Player) {
                double regain = e.getAmount();
                for (String id : TALENTS_HEAL) {
                    Object returned = Talents.getInstance().getTalentsManagerService().
                            handle((Player) e.getEntity(), id, TalentsCategory.Protective, Arrays.asList(regain));
                    if (returned instanceof Double) {
                        regain = (Double) returned;
                    }
                }

                e.setAmount(regain);
            }
        }
    }
}
