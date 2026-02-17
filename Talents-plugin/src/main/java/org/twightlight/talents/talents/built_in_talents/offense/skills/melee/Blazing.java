package org.twightlight.talents.talents.built_in_talents.offense.skills.melee;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Blazing extends Talent {
    public Blazing(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("MELEE_IGNITE_CHANCE").add(level * 1.5);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        StatsMap astatsMap = packet.getAttackerStatsMap();

        User user = User.getUserFromUUID(packet.getAttacker().getUniqueId());
        Integer level = user.getTalentLevel(getTalentId());

        double chance = astatsMap.getStatContainer("MELEE_IGNITE_CHANCE").get();
        int ticks = (1 + Math.round((float) level / 5)) * 20;
        if (Utility.rollChance(chance)) {
            e.getDamagePacket().getVictim().setFireTicks(ticks);
            packet.putMetadata("blazing-activated", true);
            Sound sound = XSound.BLOCK_FIRE_AMBIENT.parseSound();

            e.getDamagePacket().getAttacker().getWorld().playSound(e.getDamagePacket().getAttacker().getLocation(), sound, 10, 5);
        }
    }
}
