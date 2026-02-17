package org.twightlight.talents.talents.built_in_talents.offense.skills.ranged;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.entity.Despawnable;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Summoner extends Talent {

    public Summoner(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onProjectileDamage(RangedDamageEvent e) {
        Player p = e.getDamagePacket().getAttacker();

        User user = User.getUserFromBukkitPlayer(e.getDamagePacket().getAttacker());
        int level = user.getTalentLevel(getTalentId());

        ITeam iTeam = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p).getTeam(p);
        if (Utility.rollChance((double)level * 3.5D)) {
            LivingEntity silverfish = (LivingEntity)e.getDamagePacket().getVictim().getWorld().spawnEntity(e.getDamagePacket().getVictim().getLocation(), EntityType.SILVERFISH);
            new Despawnable(silverfish, iTeam, level + 10, "shop-utility-silverfish", PlayerKillEvent.PlayerKillCause.SILVERFISH, PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL);
            e.getDamagePacket().putMetadata("summoner-activated", true);
        }
    }

}
