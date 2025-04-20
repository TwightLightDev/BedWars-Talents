package org.twightlight.talents.talents.built_in_talents.protective.skills;

import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.List;

public class Giant implements Talent<BukkitTask> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Giant(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public BukkitTask handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        IArena arena = (IArena) params.get(1);
        int interval = ((93 - 3 * level) +  (3 * (int) Math.floor(level/20))) * 20;
        BukkitTask t = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 5) {
                    cancel();
                    return;
                } else if (Talents.getInstance().getApi().getArenaUtil().getArenaByPlayer(p) != arena) {
                    cancel();
                    return;
                }
                if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                    org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                    if (player.isPlaying()) {
                        p.setMaxHealth(p.getMaxHealth() + 1);
                    }
                    Double prev = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH);
                    player.modifyAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH, prev + 1D);
                }
                count++;
            }
        }.runTaskTimer(Talents.getInstance(), interval, interval);


        return t;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
