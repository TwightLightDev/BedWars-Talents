package org.twightlight.talents.menus;

import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.attributes.AttributeShopType;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.PricePair;
import org.twightlight.talents.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AttributeMenu {
   public static HashMap<Player, AttributeMenu> shopAttributeMenus = new HashMap<>();
   private final MenuHolder holder;
   private final HashMap<Integer, Button> menu = new HashMap<>();
   private final Player p;

   public AttributeMenu(Player p) {
      this.p = p;
      shopAttributeMenus.put(p, this);
      this.updateMenu();
      this.holder = new MenuHolder();
   }

   public void updateMenu() {
      this.menu.put(10, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.MELEE_DAMAGE);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.MELEE_DAMAGE);
            String color = "&e";
            List<String> lore = new ArrayList<>();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.MELEE_DAMAGE.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7ST cận chiến &a+" + Utility.toDecimal(totalAddition) + "&7.");
               PricePair pricePair = (PricePair)AttributeShopType.MELEE_DAMAGE.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7ST cận chiến &a+" + Utility.toDecimal(totalAddition) + "&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.IRON_SWORD, level, "", 0, color + "ST cận chiến" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(12, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.MELEE_PENETRATION);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.MELEE_PENETRATION);
            String color = "&e";
            List<String> lore = new ArrayList<>();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.MELEE_PENETRATION.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7Xuyên giáp cận chiến &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = (PricePair)AttributeShopType.MELEE_PENETRATION.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7Xuyên giáp cận chiến &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.SKULL_ITEM, level, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRhNzNhYzhjMTUyZDgwZGJlNTYyNGQ5YWQ0MzFjNTA2MzhjMzU4Mjk2OGE1YmZmZmRkYjVhY2RmYzZjOGI5YSJ9fX0=", 3, color + "Xuyên giáp" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(14, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.MELEE_CRITICAL_CHANCE);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.MELEE_CRITICAL_CHANCE);
            String color = "&e";
            List<String> lore = new ArrayList<>();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.MELEE_CRITICAL_CHANCE.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7TLCM cận chiến &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = AttributeShopType.MELEE_CRITICAL_CHANCE.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7TLCM cận chiến &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.DIAMOND_SWORD, level, "", 0, color + "Tỷ lệ chí mạng (cận chiến)" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(16, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.MELEE_CRITICAL_DAMAGE);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.MELEE_CRITICAL_DAMAGE);
            String color = "&e";
            List<String> lore = new ArrayList<>();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.MELEE_CRITICAL_DAMAGE.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7STCM cận chiến &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = (PricePair)AttributeShopType.MELEE_CRITICAL_DAMAGE.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7STCM cận chiến &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.SKULL_ITEM, level, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk4MWI5NWVhN2MxYjkzNWQ2Yzg3NDYyMGVlZGNlMWYzNGE4NzIzMzIyNDI1OWU5M2NiOTU3ZWNmMTAzZmJlZSJ9fX0=", 3, color + "Sát thương chí mạng (cận chiến)" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(19, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.ARROW_DAMAGE);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.ARROW_DAMAGE);
            String color = "&e";
            List<String> lore = new ArrayList<>();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.ARROW_DAMAGE.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7ST mũi tên &a+" + Utility.toDecimal(totalAddition) + "&7.");
               PricePair pricePair = (PricePair)AttributeShopType.ARROW_DAMAGE.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7ST mũi tên &a+" + Utility.toDecimal(totalAddition) + "&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.ARROW, level, "", 0, color + "Tỷ lệ chí mạng (cận chiến)" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(21, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.ARROW_PENETRATION);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.ARROW_PENETRATION);
            String color = "&e";
            List<String> lore = new ArrayList<>();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.ARROW_PENETRATION.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7Xuyên giáp mũi tên &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = AttributeShopType.ARROW_PENETRATION.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7Xuyên giáp mũi tên &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.ARROW, level, "", 0, color + "Xuyên giáp mũi tên" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(23, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.ARROW_CRITICAL_CHANCE);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.ARROW_CRITICAL_CHANCE);
            String color = "&e";
            List<String> lore = new ArrayList<>();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.ARROW_CRITICAL_CHANCE.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7TLCM mũi tên &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = (PricePair)AttributeShopType.ARROW_CRITICAL_CHANCE.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7TLCM mũi tên &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.BLAZE_ROD, level, "", 0, color + "Tỷ lệ chí mạng (mũi tên)" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(25, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.ARROW_CRITICAL_DAMAGE);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.ARROW_CRITICAL_DAMAGE);
            String color = "&e";
            List<String> lore = new ArrayList();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.ARROW_CRITICAL_DAMAGE.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7STCM mũi tên &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = (PricePair)AttributeShopType.ARROW_CRITICAL_DAMAGE.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7STCM mũi tên &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.SKULL_ITEM, level, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk4MWI5NWVhN2MxYjkzNWQ2Yzg3NDYyMGVlZGNlMWYzNGE4NzIzMzIyNDI1OWU5M2NiOTU3ZWNmMTAzZmJlZSJ9fX0=", 3, color + "Sát thương chí mạng (mũi tên)" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(28, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.ADDITIONAL_HEALTH);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.ADDITIONAL_HEALTH);
            List<Double> upgradesData = AttributeShopType.ADDITIONAL_HEALTH.getAddition();
            double increment = (Double)upgradesData.get(level - 1);
            p.setMaxHealth(p.getMaxHealth() + increment);
            p.setHealth(p.getHealth() + increment);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.ADDITIONAL_HEALTH);
            String color = "&e";
            List<String> lore = new ArrayList();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.ADDITIONAL_HEALTH.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7Máu tối đa &a+" + Utility.toDecimal(totalAddition) + "&7.");
               PricePair pricePair = (PricePair)AttributeShopType.ADDITIONAL_HEALTH.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7Máu tối đa &a+" + Utility.toDecimal(totalAddition) + "&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.POTION, level, "", 8229, color + "Máu tối đa" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(30, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.DAMAGE_REDUCTION);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.DAMAGE_REDUCTION);
            String color = "&e";
            List<String> lore = new ArrayList();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.DAMAGE_REDUCTION.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7Giảm ST &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = (PricePair)AttributeShopType.DAMAGE_REDUCTION.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7Giảm ST &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.IRON_CHESTPLATE, level, "", 0, color + "Giảm sát thương" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(32, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.CRITICAL_DAMAGE_REDUCTION);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.CRITICAL_DAMAGE_REDUCTION);
            String color = "&e";
            List<String> lore = new ArrayList();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.CRITICAL_DAMAGE_REDUCTION.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7Giảm STCM &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = AttributeShopType.CRITICAL_DAMAGE_REDUCTION.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7Giảm STCM &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.CHAINMAIL_CHESTPLATE, level, "", 0, color + "Giảm sát thương chí mạng" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(34, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            Arena.ShopUtils.upgradeAttribute(p.getUniqueId(), AttributeShopType.FALL_DAMAGE_REDUCTION);
            this.getHolder().open();
         }

      }, (p) -> {
         if (User.getUserFromBukkitPlayer(p) != null) {
            User user = User.getUserFromBukkitPlayer(p);
            int level = Arena.ShopUtils.getLevelAttribute(p.getUniqueId(), AttributeShopType.FALL_DAMAGE_REDUCTION);
            String color = "&e";
            List<String> lore = new ArrayList();
            boolean enchanted = false;
            List<Double> upgradeData = AttributeShopType.FALL_DAMAGE_REDUCTION.getAddition();
            double totalAddition;
            if (level < 3) {
               totalAddition = Utility.sum(upgradeData, 0, level);
               lore.add("&7Giảm ST rơi &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               PricePair pricePair = AttributeShopType.FALL_DAMAGE_REDUCTION.getPrice().get(level);
               lore.add("");
               if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!");
               } else {
                  lore.add("&7Giá nâng cấp: " + ConversionUtils.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtils.getMaterialName(pricePair.getA()) + "&f.");
                  lore.add("");
                  lore.add("&eBấm để nâng cấp.");
               }
            } else {
               totalAddition = Utility.sum(upgradeData, 0, 2);
               lore.add("&7Giảm ST rơi &a+" + Utility.toDecimal(totalAddition) + "%&7.");
               lore.add("");
               lore.add("&aBạn đã nâng tối đa điểm này!");
               color = "&b";
               enchanted = true;
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
               roman = " " + roman;
            }

            return ConversionUtils.createItem(Material.FEATHER, level, "", 0, color + "Giảm sát thương rơi" + roman, lore, enchanted);
         } else {
            return ConversionUtils.createItemLite(Material.BEDROCK, 1, "&cUnknown");
         }
      }));
      this.menu.put(49, Collections.getCloseButton());
      this.menu.put(45, new Button((e) -> {
         Player p = (Player)e.getWhoClicked();
         ShopManager.getShop().open(p, PlayerQuickBuyCache.getQuickBuyCache(p.getUniqueId()), true);
      }, (p) -> {
         return ConversionUtils.createItem(Material.ARROW, 1, "", 0, "&aBack!", java.util.Collections.emptyList(), false);
      }));
   }

   public HashMap<Integer, Button> getMenu() {
      return this.menu;
   }

   public MenuHolder getHolder() {
      return this.holder;
   }

   public static AttributeMenu getInstance(Player p) {
      return shopAttributeMenus.get(p);
   }

   public void remove() {
      shopAttributeMenus.remove(this.p);
      this.menu.clear();
   }

   public class MenuHolder implements InventoryHolder {
      private Inventory inv;

      public Inventory getInventory() {
         return this.inv;
      }

      public void open() {
         this.inv = Bukkit.createInventory(this, 54, ChatColor.GRAY + "Attribute Shop");
         Iterator var1 = AttributeMenu.this.menu.keySet().iterator();

         while(var1.hasNext()) {
            int i = (Integer)var1.next();
            this.inv.setItem(i, (ItemStack)((Button)AttributeMenu.this.menu.get(i)).getItemStackConsumer().accept(AttributeMenu.this.p));
         }

         AttributeMenu.this.p.openInventory(this.inv);
      }

      public void refreshSlot(int slot) {
         if (AttributeMenu.this.menu.get(slot) != null) {
            this.inv.setItem(slot, (ItemStack)((Button)AttributeMenu.this.menu.get(slot)).getItemStackConsumer().accept(AttributeMenu.this.p));
            AttributeMenu.this.p.openInventory(this.inv);
         }

      }
   }
}
