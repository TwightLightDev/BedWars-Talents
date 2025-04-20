package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.List;

public class WoodTomb implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;

    public WoodTomb(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        ItemStack i = new ItemStack(Material.WOOD, level, Short.valueOf("0"));
        p.getInventory().addItem(i);
        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
