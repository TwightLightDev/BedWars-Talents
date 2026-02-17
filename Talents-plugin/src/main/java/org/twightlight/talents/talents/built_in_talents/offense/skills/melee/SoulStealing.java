package org.twightlight.talents.talents.built_in_talents.offense.skills.melee;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class SoulStealing extends Talent {
    public SoulStealing(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("SOUL_STEALING").add(level * 1.25);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        StatsMap astatsMap = packet.getAttackerStatsMap();

        double chance = astatsMap.getStatContainer("SOUL_STEALING").get();

        if (!Utility.rollChance(chance)) return;

        packet.putMetadata("soulstealing-activated", true);

        Player attacker = e.getDamagePacket().getAttacker();
        LivingEntity victim = e.getDamagePacket().getVictim();

        attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 0.5));
        victim.setHealth(Math.max(0.5D, victim.getHealth() - 0.5));
    }
}
