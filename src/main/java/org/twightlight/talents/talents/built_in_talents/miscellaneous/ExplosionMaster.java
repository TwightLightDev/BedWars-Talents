package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.Utility;

import java.util.Collections;
import java.util.List;

public class ExplosionMaster implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public ExplosionMaster(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        ShopBuyEvent e = (ShopBuyEvent) params.get(0);
        Player p = e.getBuyer();
        if (Utility.rollChance(level * 3.5)) {
            if (e.getCategoryContent().getIdentifier().equals("utility-category.category-content.tnt")) {
                ItemStack fireball = ConversionUtil.createItem(Material.FIREBALL, 1, "", 0, "&rFireball", Collections.emptyList(),  false);
                p.getInventory().addItem(fireball);
            }
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
