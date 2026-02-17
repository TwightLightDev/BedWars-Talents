package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Shepherd extends Talent {

    public Shepherd(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)

    public void onShopBuy(ShopBuyEvent e) {

        Player p = e.getBuyer();
        User user = User.getUserFromBukkitPlayer(p);
        int level = user.getTalentLevel(getTalentId());

        if (e.getCategoryContent().getIdentifier().equals("blocks-category.category-content.wool")) {
            ItemStack wool = new ItemStack(Material.WOOL, level, ConversionUtils.getDataValue(e.getArena().getTeam(p).getColor().dye()));
            p.getInventory().addItem(new ItemStack[]{wool});
        }
    }
}
