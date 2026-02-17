package org.twightlight.talents.talents.built_in_talents.offense.skills.ranged;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.metadata.FixedMetadataValue;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Frozen extends Talent {

    public Frozen(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("RANGED_FROZEN_CHANCE").add(level * 1.5);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onProjectileDamage(RangedDamageEvent e) {
        StatsMap astatmap = e.getDamagePacket().getAttackerStatsMap();
        double chance = astatmap.getStatContainer("RANGED_FROZEN_CHANCE").get();

        if (Utility.rollChance(chance)) {
            e.getDamagePacket().getVictim().setMetadata("frozen", new FixedMetadataValue(Talents.getInstance(), true));
            Sound sound = XSound.ENTITY_SNOWBALL_THROW.parseSound();

            e.getDamagePacket().getAttacker().getWorld().playSound(e.getDamagePacket().getAttacker().getLocation(), sound, 10, 5);
            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                e.getDamagePacket().getVictim().removeMetadata("frozen", Talents.getInstance());
                }, 20L);
        }

    }

}
