package org.twightlight.talents.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.menus.interfaces.Executable;
import org.twightlight.talents.menus.talents.PlayerMenu;
import org.twightlight.talents.utils.ConversionUtils;

public class Collections {
   private static final Button emptyButton = new Button((Executable)null, (p) -> {
      return new ItemStack(Material.AIR);
   });
   private static final Button upButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX(), menu.getY() + 1);
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Up", java.util.Collections.emptyList(), false);
   });

   private static final Button downButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX(), menu.getY() - 1);
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Down", java.util.Collections.emptyList(), false);
   });
   private static final Button rightButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX() + 1, menu.getY());
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Right", java.util.Collections.emptyList(), false);
   });
   private static final Button leftButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX() - 1, menu.getY());
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Left", java.util.Collections.emptyList(), false);
   });
   private static final Button upRightButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX() + 1, menu.getY() + 1);
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Up-Right", java.util.Collections.emptyList(), false);
   });
   private static final Button upLeftButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX() - 1, menu.getY() + 1);
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Up-Left", java.util.Collections.emptyList(), false);
   });
   private static final Button downRightButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX() + 1, menu.getY() - 1);
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Down-Right", java.util.Collections.emptyList(), false);
   });
   private static final Button downLeftButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(menu.getX() - 1, menu.getY() - 1);
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ARROW, "", 0, "&aMove Down-Left", java.util.Collections.emptyList(), false);
   });
   private static final Button resetPosButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      menu.updateMenu(1, 0);
      menu.getHolder().open();
   }, (p) -> {
      return ConversionUtils.createItem(Material.ENDER_PEARL, "", 0, "&aBack to starting location", java.util.Collections.emptyList(), false);
   });
   private static final Button closeButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      p.closeInventory();
   }, (p) -> {
      return ConversionUtils.createItem(Material.BARRIER, "", 0, "&cClose!", java.util.Collections.emptyList(), false);
   });
   private static final Button ssButton = new Button((Executable)null, (p) -> {
      int ss = Talents.getInstance().getDb().getSoulStones(p);
      return ConversionUtils.createItem(Material.QUARTZ, "", 0, "&bSố đá linh hồn của bạn: &d" + ss, java.util.Collections.emptyList(), false);
   });

   private static final Button resetButton = new Button((e) -> {
      Player p = (Player)e.getWhoClicked();
      PlayerMenu menu = PlayerMenu.getInstance(p);
      p.closeInventory();
      p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn chắc chứ. Nhập 'yes' để tiếp tục, 'no' để hủy!"));
      ChatSessionService.createSession(p, (s) -> {
         if (s.equalsIgnoreCase("yes")) {
            int i = Talents.getInstance().getTalentsManager().resetTalent(p);
            int ss = Talents.getInstance().getDb().getSoulStones(p);
            Talents.getInstance().getDb().setSoulStones(p, ss + i);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aĐã reset bảng tài năng của bạn. Nhận lại &d" + i + " &ađá linh hồn!"));
            menu.getHolder().open();
            ChatSessionService.end(p);
         } else if (s.equalsIgnoreCase("no")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aĐã hủy yêu cầu của bạn!"));
             menu.getHolder().open();
            ChatSessionService.end(p);
         }

      });
   }, (p) -> {
      return ConversionUtils.createItem(Material.WATCH, "", 0, "&cReset Talent?", java.util.Collections.emptyList(), false);
   });

   public static Button getEmptyButton() {
      return emptyButton;
   }

   public static Button getUpButton() {
      return upButton;
   }

   public static Button getDownButton() {
      return downButton;
   }

   public static Button getRightButton() {
      return rightButton;
   }

   public static Button getLeftButton() {
      return leftButton;
   }

   public static Button getUpRightButton() {
      return upRightButton;
   }

   public static Button getUpLeftButton() {
      return upLeftButton;
   }

   public static Button getDownRightButton() {
      return downRightButton;
   }

   public static Button getDownLeftButton() {
      return downLeftButton;
   }

   public static Button getResetPosButton() {
      return resetPosButton;
   }

   public static Button getCloseButton() {
      return closeButton;
   }

   public static Button getSoulStonesButton() {
      return ssButton;
   }

   public static Button getResetButton() {
      return resetButton;
   }
}
