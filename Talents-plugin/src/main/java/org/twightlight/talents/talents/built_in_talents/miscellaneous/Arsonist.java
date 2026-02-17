package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Arsonist extends Talent {

    public Arsonist(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)

    public void onShopBuy(ShopBuyEvent e) {

        Player p = e.getBuyer();
        User user = User.getUserFromBukkitPlayer(p);
        int level = user.getTalentLevel(getTalentId());

        if (Utility.rollChance((double) level * 3.5D) && e.getCategoryContent().getIdentifier().equals("utility-category.category-content.tnt")) {
            ItemStack fireball = ConversionUtils.createItemLite(Material.FIREBALL, 1, "&rFireball");
            p.getInventory().addItem(new ItemStack[]{fireball});
        }
    }
}
