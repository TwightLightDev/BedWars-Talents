package org.twightlight.talents.listeners.general;

import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.Arrays;

public class GeneralInventoryOpenEvent implements Listener {

    private ItemStack icon = ConversionUtil.createItem(Material.BEACON, 1, "", 0, "&eCửa hàng thuộc tính", Arrays.asList("&7Đây là nơi bạn có thể nâng cấp thuộc", "&7tính cho bản thân để trở nên", "&7mạnh mẽ hơn", "", "&eBấm để xem!"), false);

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        String title = e.getInventory().getTitle();
        if (title.equals(Language.getMsg((Player) e.getPlayer(), "shop-items-messages.inventory-name"))) {
            Inventory inv = e.getInventory();
            inv.setItem(46, icon);
        }
    }
}
