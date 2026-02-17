package org.twightlight.talents.listeners.menu;

import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.menus.AttributeMenu;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.runes.MenuHolder;
import org.twightlight.talents.menus.talents.PlayerMenu;

public class InventoryClickEvent implements Listener {
   @EventHandler
   public void onClickEvent(org.bukkit.event.inventory.InventoryClickEvent e) {
      Player p = (Player)e.getWhoClicked();
      Button b;
      if (PlayerMenu.getInstance(p) != null && e.getInventory().getHolder() instanceof PlayerMenu.MenuHolder && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
         e.setCancelled(true);
         PlayerMenu menu = PlayerMenu.getInstance(p);
         b = menu.getMenu().get(e.getRawSlot());
         if (b == null) {
            return;
         }

         if (b.getExecutable() != null) {
            b.getExecutable().execute(e);
         }
      }

       if (e.getInventory().getHolder() instanceof MenuHolder && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
           e.setCancelled(true);
           MenuHolder menuHolder = (MenuHolder) e.getInventory().getHolder();
           b = menuHolder.getButton(e.getRawSlot());
           if (b == null) {
               return;
           }

           if (b.getExecutable() != null) {
               b.getExecutable().execute(e);
           }
       }

      if (AttributeMenu.getInstance(p) != null && e.getView().getTopInventory().getHolder() instanceof AttributeMenu.MenuHolder && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
         e.setCancelled(true);
         AttributeMenu menu = AttributeMenu.getInstance(p);
         b = menu.getMenu().get(e.getRawSlot());
         if (b == null) {
            return;
         }

         if (b.getExecutable() != null) {
            b.getExecutable().execute(e);
         }
      }

      String title = e.getInventory().getTitle();
      if (title.equals(Language.getMsg(p, "shop-items-messages.inventory-name"))) {
         if (org.twightlight.talents.users.User.getUserFromBukkitPlayer(p) == null) {
            return;
         }

         org.twightlight.talents.users.User user = org.twightlight.talents.users.User.getUserFromBukkitPlayer(p);
         if (!user.isPlaying()) {
            return;
         }

         if (e.getCurrentItem() == null && e.getCurrentItem().getType() == Material.AIR) {
            return;
         }

         if (e.getRawSlot() == 46) {
            e.setCancelled(true);
            AttributeMenu attributeMenu = AttributeMenu.getInstance(p);
            attributeMenu.getHolder().open();
         }
      }

   }
}
