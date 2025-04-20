package org.twightlight.talents.listeners.general;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;

public class GeneralShotBowEvent implements Listener {
    @EventHandler
    public void onBowShot(EntityShootBowEvent e) {
        if (e.getProjectile() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getProjectile();
            if (arrow.getShooter() instanceof Player) {
                Player p = (Player) arrow.getShooter();
                Talents.getInstance().getTalentsManagerService().
                        handle(p, "TWIN", TalentsCategory.Ranged, Arrays.asList(e));
            }
        }
    }
}
