package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.List;

public class Blasting extends Talent {

    public Blasting(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)

    public void onTNTDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed)e.getDamager();
            if (tnt.getSource() instanceof Player) {
                Player p = (Player) tnt.getSource();

                User user = User.getUserFromBukkitPlayer(p);
                int level = user.getTalentLevel(getTalentId());
                e.setDamage((1.0D + (double)level * 0.025D));
            }
        }
    }
}
