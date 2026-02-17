package org.twightlight.talents.talents.built_in_talents.defense.skills;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.api.packets.UndefinedDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Block extends Talent {
    public Block(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("BLOCK_CHANCE").add(level);
            map.getStatContainer("BLOCK_RATIO").add(level * 2.5);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        double chance = v.getStatContainer("BLOCK_CHANCE").get();

        if (Utility.rollChance(chance)) {
            packet.putMetadata("block-activated", true);

            packet.getDamageProperty().addLayer("blockLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
            packet.getDamageProperty().addValueToLayer("blockLayer", (1 - v.getStatContainer("BLOCK_RATIO").get()/100));

        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        double chance = v.getStatContainer("BLOCK_CHANCE").get();

        if (Utility.rollChance(chance)) {
            packet.putMetadata("block-activated", true);

            packet.getDamageProperty().addLayer("blockLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
            packet.getDamageProperty().addValueToLayer("blockLayer", (1 - v.getStatContainer("BLOCK_RATIO").get()/100));

        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onUndefinedAttack(UndefinedDamageEvent e) {
        UndefinedDamagePacket packet = e.getDamagePacket();

        StatsMap v = packet.getVictimStatsMap();
        if (v == null) return;
        double chance = v.getStatContainer("BLOCK_CHANCE").get();

        if (Utility.rollChance(chance)) {
            packet.putMetadata("block-activated", true);

            packet.getDamageProperty().addLayer("blockLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
            packet.getDamageProperty().addValueToLayer("blockLayer", (1 - v.getStatContainer("BLOCK_RATIO").get()/100));

        }
    }
}

