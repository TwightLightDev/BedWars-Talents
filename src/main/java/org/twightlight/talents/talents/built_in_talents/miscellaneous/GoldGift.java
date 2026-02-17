package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.List;

public class GoldGift implements Talent<BukkitTask> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public GoldGift(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public BukkitTask handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);

        ItemStack i = new ItemStack(Material.GOLD_INGOT, 1, Short.valueOf("0"));

        int interval = ((31 - level) + (int) Math.floor(level/20)) * 20;
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(Talents.getInstance(),
                () -> {
                    if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                        if (ConversionUtil.getPlayerFromBukkitPlayer(p).isPlaying()) {
                            p.getInventory().addItem(i);
                        }
                    }
                }, interval, interval);

        return t;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
