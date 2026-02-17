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

public class InitialWool implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public InitialWool(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        ITeam team = (ITeam) params.get(1);
        DyeColor color = team.getColor().dye();

        ItemStack i = new ItemStack(Material.WOOL, level * 4, ConversionUtil.getDataValue(color));
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
