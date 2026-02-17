package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.List;

public class Explode extends Talent {

    public Explode(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onTNTDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Fireball) {
            Fireball tnt = (Fireball)e.getDamager();
            if (tnt.getShooter() instanceof Player) {
                Player p = (Player) tnt.getShooter();

                User user = User.getUserFromBukkitPlayer(p);
                int level = user.getTalentLevel(getTalentId());
                e.setDamage((1.0D + (double)level * 0.025D));
            }

        }
    }

}
