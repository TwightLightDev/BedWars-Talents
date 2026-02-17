package org.twightlight.talents.talents.built_in_talents.offense.skills.ranged;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.List;

public class Blow extends Talent {

    public Blow(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("RANGED_KNOCKBACK_POWER").add(level * 2);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onProjectileDamage(RangedDamageEvent e) {
        StatsMap astatmap = e.getDamagePacket().getAttackerStatsMap();
        double additional = astatmap.getStatContainer("RANGED_KNOCKBACK_POWER").get()/100;


        double force = 0.6D * additional;
        Vector velocity = e.getDamagePacket().getProjectile().getVelocity();
        Vector knockback = velocity.multiply(force);
        knockback.setY(additional * 0.3D);
        e.getDamagePacket().getVictim().setVelocity(e.getDamagePacket().getVictim().getVelocity().add(knockback));

    }

}
