package org.twightlight.talents.talents.built_in_talents.offense.skills.ranged;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Scouter extends Talent {
    public Scouter(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("RANGED_SPEED_BOOST_CHANCE").add(level * 2.5);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();
        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        Integer level = user.getTalentLevel(getTalentId());
        StatsMap astatsMap = packet.getAttackerStatsMap();

        double chance = astatsMap.getStatContainer("RANGED_SPEED_BOOST_CHANCE").get();

        if (Utility.rollChance(chance)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) ((float)(level / 4) + 2.0F) * 20, 0));
            packet.putMetadata("flame-activated", true);

        }
    }
}
