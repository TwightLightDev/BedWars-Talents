package org.twightlight.talents.talents.built_in_talents.offense.skills.melee;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Breezing extends Talent {
    public Breezing(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("MELEE_SLOWING_CHANCE").add(level * 1.5);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        StatsMap astatsMap = packet.getAttackerStatsMap();

        double chance = astatsMap.getStatContainer("MELEE_SLOWING_CHANCE").get();

        if (Utility.rollChance(chance)) {
            e.getDamagePacket().getVictim().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0, false, false));
            packet.putMetadata("breezing-activated", true);
            Sound sound = XSound.ENTITY_SNOWBALL_THROW.parseSound();

            e.getDamagePacket().getAttacker().getWorld().playSound(e.getDamagePacket().getAttacker().getLocation(), sound, 10, 5);
        }
    }
}
