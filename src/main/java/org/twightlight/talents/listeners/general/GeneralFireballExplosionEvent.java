package org.twightlight.talents.listeners.general;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;

public class GeneralFireballExplosionEvent implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFireBallDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball)) {
            return;
        }
        Fireball fireball = (Fireball) e.getDamager();
        if (fireball.getShooter() instanceof Player) {
            Object returned = Talents.getInstance().getTalentsManagerService().handle((Player) fireball.getShooter(), "FBD", TalentsCategory.Miscellaneous, Arrays.asList(e.getDamage()));
            if (returned instanceof Double) {
                e.setDamage((Double) returned);
            }
        }
    }
}
