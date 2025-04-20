package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class AngelGift implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;

    public AngelGift(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        int gold = 1 + Math.round(level / 10);
        int iron = level;
        ItemStack i = new ItemStack(Material.GOLD_INGOT, gold, Short.valueOf("0"));
        ItemStack i1 = new ItemStack(Material.IRON_INGOT, iron, Short.valueOf("0"));

        p.getInventory().addItem(i);
        p.getInventory().addItem(i1);

        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
