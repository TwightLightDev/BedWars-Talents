package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.List;

public class Merchant implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Merchant(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        ShopBuyEvent e = (ShopBuyEvent) params.get(0);
        Player p = e.getBuyer();
        if (e.getCategoryContent().getIdentifier().equals("blocks-category.category-content.wool")) {
            ItemStack wool = new ItemStack(Material.WOOL, level, ConversionUtil.getDataValue(e.getArena().getTeam(p).getColor().dye()));
            p.getInventory().addItem(wool);
        }

        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
