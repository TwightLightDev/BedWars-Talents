package org.twightlight.talents.listeners.menu;

import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.utils.ConversionUtils;

import java.util.Arrays;

public class InventoryOpenEvent implements Listener {
   private ItemStack icon;

   public InventoryOpenEvent() {
      this.icon = ConversionUtils.createItem(Material.BEACON, 1, "", 0, "&eCửa hàng thuộc tính", Arrays.asList("&7Đây là nơi bạn có thể nâng cấp thuộc", "&7tính cho bản thân để trở nên", "&7mạnh mẽ hơn", "", "&eBấm để xem!"), false);
   }

   @EventHandler
   public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent e) {
      String title = e.getInventory().getTitle();
      if (title.equals(Language.getMsg((Player)e.getPlayer(), "shop-items-messages.inventory-name"))) {
         Inventory inv = e.getInventory();
         inv.setItem(46, this.icon);
      }

   }
}
