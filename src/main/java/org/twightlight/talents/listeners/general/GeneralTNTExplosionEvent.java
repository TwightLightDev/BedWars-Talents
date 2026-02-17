package org.twightlight.talents.listeners.general;

import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;

public class GeneralTNTExplosionEvent implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTNTDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof TNTPrimed)) {
            return;
        }
        TNTPrimed tnt = (TNTPrimed) e.getDamager();
        if (tnt.getSource() instanceof Player) {
            Object returned = Talents.getInstance().getTalentsManagerService().handle((Player) tnt.getSource(), "TNTD", TalentsCategory.Miscellaneous, Arrays.asList(e.getDamage()));
            if (returned instanceof Double) {
                e.setDamage((Double) returned);
            }
        }
    }
}
