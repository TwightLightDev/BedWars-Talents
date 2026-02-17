package org.twightlight.talents.menus.talents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.Collections;

import java.util.HashMap;

public class PlayerMenu {
   public static HashMap<Player, PlayerMenu> playerMenus = new HashMap<>();
   private final MenuHolder holder;
   private final HashMap<Integer, Button> menu = new HashMap<>();
   private final Player p;
   private int x;
   private int y;

   public PlayerMenu(Player p, int x, int y) {
      this.p = p;
      playerMenus.put(p, this);
      this.updateMenu(x, y);
      this.holder = new MenuHolder();
   }

   public void updateMenu(int x, int y) {
      this.menu.clear();
      this.x = x;
      this.y = y;

      int i;
      for(i = -3; i <= 3; ++i) {
         for(int j = -1; j <= 2; ++j) {
            int slotInMenu = 22 + i + 9 * j;
            Button button = TalentsMenu.getButton(x + i, y - j);
            this.menu.put(slotInMenu, button);
         }
      }

      for(i = 2; i <= 3; ++i) {
         this.menu.put(i, Collections.getUpButton());
      }

      this.menu.put(4, Collections.getSoulStonesButton());

      for(i = 5; i <= 6; ++i) {
         this.menu.put(i, Collections.getUpButton());
      }

      this.menu.put(47, Collections.getDownButton());
      this.menu.put(49, Collections.getResetButton());
      this.menu.put(51, Collections.getDownButton());
      this.menu.put(48, Collections.getResetPosButton());
      this.menu.put(50, Collections.getCloseButton());
      this.menu.put(26, Collections.getRightButton());
      this.menu.put(35, Collections.getRightButton());
      this.menu.put(18, Collections.getLeftButton());
      this.menu.put(27, Collections.getLeftButton());
      this.menu.put(0, Collections.getUpLeftButton());
      this.menu.put(1, Collections.getUpLeftButton());
      this.menu.put(9, Collections.getUpLeftButton());
      this.menu.put(7, Collections.getUpRightButton());
      this.menu.put(8, Collections.getUpRightButton());
      this.menu.put(17, Collections.getUpRightButton());
      this.menu.put(36, Collections.getDownLeftButton());
      this.menu.put(45, Collections.getDownLeftButton());
      this.menu.put(46, Collections.getDownLeftButton());
      this.menu.put(44, Collections.getDownRightButton());
      this.menu.put(52, Collections.getDownRightButton());
      this.menu.put(53, Collections.getDownRightButton());
   }

   public HashMap<Integer, Button> getMenu() {
      return this.menu;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public MenuHolder getHolder() {
      return this.holder;
   }

   public static PlayerMenu getInstance(Player p) {
      return (PlayerMenu)playerMenus.get(p);
   }

   public class MenuHolder implements InventoryHolder {
      private Inventory inv;

      public Inventory getInventory() {
         return inv;
      }

      public void open() {
         inv = Bukkit.createInventory(this, 54, ChatColor.YELLOW + "Talents");

          for (int i : menu.keySet()) {
              inv.setItem(i, (menu.get(i)).getItemStackConsumer().accept(p));
          }

         p.openInventory(inv);
      }

      public void refreshSlot(int slot) {
         if (menu.get(slot) != null) {
            inv.setItem(slot, (menu.get(slot)).getItemStackConsumer().accept(p));
            p.openInventory(inv);
         }

      }
   }
}
