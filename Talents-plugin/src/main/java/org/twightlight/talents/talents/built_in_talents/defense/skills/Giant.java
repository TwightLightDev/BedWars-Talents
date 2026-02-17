package org.twightlight.talents.talents.built_in_talents.defense.skills;


import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.List;

public class Giant extends Talent {
    public Giant(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());

            if (level == 0) return;

            int interval = (93 - 3 * level + 3 * (int)Math.floor((level / 20))) * 20;
            BukkitTask t = (new BukkitRunnable() {
                int count = 0;

                public void run() {
                    if (this.count >= 5) {
                        this.cancel();
                    } else if (!User.isPlaying(p)) {
                        this.cancel();
                    } else {
                        if (User.getUserFromBukkitPlayer(p) != null) {
                            User user = User.getUserFromBukkitPlayer(p);
                            if (user.isPlaying()) {
                                p.setMaxHealth(p.getMaxHealth() + 1.0D);
                            }

                            map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_MAX_HEALTH.name()).add(1D);
                        }

                        ++this.count;
                    }
                }
            }).runTaskTimer(Talents.getInstance(), interval, interval);
        });
    }
}
