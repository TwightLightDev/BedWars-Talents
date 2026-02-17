package org.twightlight.talents.talents.built_in_talents.defense.stats;


import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.List;

public class Endurance extends Talent {
    public Endurance(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            int level = user.getTalentLevel(getTalentId());
            map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_MAX_HEALTH.name()).add(level * 0.25);
            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                p.setMaxHealth(20 + map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_MAX_HEALTH.name()).get());
                p.setHealth(p.getMaxHealth());
                }
            , 20L);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onGameStateChange(PlayerReSpawnEvent e) {
        Player p = e.getPlayer();
        StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    p.setMaxHealth(20 + map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_MAX_HEALTH.name()).get());
                    p.setHealth(p.getMaxHealth());
                }
                , 20L);
    }
}
