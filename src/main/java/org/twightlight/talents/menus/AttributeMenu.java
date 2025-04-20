package org.twightlight.talents.menus;

import com.andrei1058.bedwars.shop.ShopCache;
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
import org.twightlight.talents.internal.ShopAttributeType;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.PricePair;
import org.twightlight.talents.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttributeMenu {
    public static HashMap<Player, AttributeMenu> shopAttributeMenus = new HashMap<>();


    private final MenuHolder holder;
    private final HashMap<Integer, Button> menu = new HashMap<>();
    private final Player p;


    public AttributeMenu(Player p) {
        this.p = p;
        shopAttributeMenus.put(p, this);
        updateMenu();
        holder = new MenuHolder();
    }

    public void updateMenu() {
        menu.put(10, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.MELEE_DAMAGE);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.MELEE_DAMAGE);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.MELEE_DAMAGE.getAddition();
                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7ST cận chiến &a+" + Utility.toDecimal(totalAddition) +"&7.");
                    PricePair pricePair = ShopAttributeType.MELEE_DAMAGE.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7ST cận chiến &a+" + Utility.toDecimal(totalAddition) +"&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.IRON_SWORD, level, "", 0, color+"ST cận chiến"+roman, lore, enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(12, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.MELEE_PENETRATION);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.MELEE_PENETRATION);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.MELEE_PENETRATION.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7Xuyên giáp cận chiến &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    PricePair pricePair = ShopAttributeType.MELEE_PENETRATION.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7Xuyên giáp cận chiến &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.SKULL_ITEM, level, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRhNzNhYzhjMTUyZDgwZGJlNTYyNGQ5YWQ0MzFjNTA2MzhjMzU4Mjk2OGE1YmZmZmRkYjVhY2RmYzZjOGI5YSJ9fX0=",
                        3, color+"Xuyên giáp" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(14, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.MELEE_CRITICAL_CHANCE);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.MELEE_CRITICAL_CHANCE);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.MELEE_CRITICAL_CHANCE.getAddition();
                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7TLCM cận chiến &a+" + Utility.toDecimal(totalAddition) + "%&7.");
                    PricePair pricePair = ShopAttributeType.MELEE_CRITICAL_CHANCE.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
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
                return ConversionUtil.createItem(Material.DIAMOND_SWORD, level, "",
                        0, color+"Tỷ lệ chí mạng (cận chiến)" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(16, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.MELEE_CRITICAL_DAMAGE);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.MELEE_CRITICAL_DAMAGE);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.MELEE_CRITICAL_DAMAGE.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7STCM cận chiến &a+" + Utility.toDecimal(100*totalAddition)+"%&7.");
                    PricePair pricePair = ShopAttributeType.MELEE_CRITICAL_DAMAGE.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7STCM cận chiến &a+" + Utility.toDecimal(100*totalAddition)+"%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.SKULL_ITEM, level, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk4MWI5NWVhN2MxYjkzNWQ2Yzg3NDYyMGVlZGNlMWYzNGE4NzIzMzIyNDI1OWU5M2NiOTU3ZWNmMTAzZmJlZSJ9fX0=",
                        3, color+"Sát thương chí mạng (cận chiến)" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(19, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.ARROW_DAMAGE);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.ARROW_DAMAGE);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.ARROW_DAMAGE.getAddition();
                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7ST mũi tên &a+" + Utility.toDecimal(totalAddition)+"&7.");
                    PricePair pricePair = ShopAttributeType.ARROW_DAMAGE.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");


                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7ST mũi tên &a+" + Utility.toDecimal(totalAddition)+"&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.ARROW, level, "",
                        0, color+"Tỷ lệ chí mạng (cận chiến)" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(21, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.ARROW_PENETRATION);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.ARROW_PENETRATION);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.ARROW_PENETRATION.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7Xuyên giáp mũi tên &a+" + Utility.toDecimal(100*totalAddition)+"%&7.");
                    PricePair pricePair = ShopAttributeType.ARROW_PENETRATION.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7Xuyên giáp mũi tên &a+" + Utility.toDecimal(100*totalAddition)+"%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.ARROW, level, "",
                        0, color+"Xuyên giáp mũi tên" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));
        menu.put(23, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.ARROW_CRITICAL_CHANCE);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.ARROW_CRITICAL_CHANCE);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.ARROW_CRITICAL_CHANCE.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7TLCM mũi tên &a+" + Utility.toDecimal(totalAddition)+"%&7.");
                    PricePair pricePair = ShopAttributeType.ARROW_CRITICAL_CHANCE.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7TLCM mũi tên &a+" + Utility.toDecimal(totalAddition)+"%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.BLAZE_ROD, level, "",
                        0, color+"Tỷ lệ chí mạng (mũi tên)" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(25, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.ARROW_CRITICAL_DAMAGE);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.ARROW_CRITICAL_DAMAGE);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.ARROW_CRITICAL_DAMAGE.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7STCM mũi tên &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    PricePair pricePair = ShopAttributeType.ARROW_CRITICAL_DAMAGE.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7STCM mũi tên &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.SKULL_ITEM, level, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk4MWI5NWVhN2MxYjkzNWQ2Yzg3NDYyMGVlZGNlMWYzNGE4NzIzMzIyNDI1OWU5M2NiOTU3ZWNmMTAzZmJlZSJ9fX0=",
                        3, color+"Sát thương chí mạng (mũi tên)" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));
        menu.put(28, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.ADDITIONAL_HEALTH);
                int level = player.getLevelAttribute(ShopAttributeType.ADDITIONAL_HEALTH);
                List<Double> upgradesData = ShopAttributeType.ADDITIONAL_HEALTH.getAddition();
                double increment = upgradesData.get(level-1);

                p.setMaxHealth(p.getMaxHealth() + increment);
                p.setHealth(p.getHealth() + increment);

                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.ADDITIONAL_HEALTH);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.ADDITIONAL_HEALTH.getAddition();
                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7Máu tối đa &a+" + Utility.toDecimal(totalAddition) + "&7.");
                    PricePair pricePair = ShopAttributeType.ADDITIONAL_HEALTH.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
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
                return ConversionUtil.createItem(Material.POTION, level, "",
                        8229, color+"Máu tối đa" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));
        menu.put(30, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.DAMAGE_REDUCTION);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.DAMAGE_REDUCTION);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.DAMAGE_REDUCTION.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7Giảm ST &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    PricePair pricePair = ShopAttributeType.DAMAGE_REDUCTION.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7Giảm ST &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.IRON_CHESTPLATE, level, "",
                        0, color+"Giảm sát thương" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(32, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.CRITICAL_DAMAGE_REDUCTION);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.CRITICAL_DAMAGE_REDUCTION);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.CRITICAL_DAMAGE_REDUCTION.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7Giảm STCM &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    PricePair pricePair = ShopAttributeType.CRITICAL_DAMAGE_REDUCTION.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");
                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7Giảm STCM &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.CHAINMAIL_CHESTPLATE, level, "",
                        0, color+"Giảm sát thương chí mạng" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(34, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                player.upgradeAttribute(ShopAttributeType.FALL_DAMAGE_REDUCTION);
                getHolder().open();
            }
        }, (p) -> {
            if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                int level = player.getLevelAttribute(ShopAttributeType.FALL_DAMAGE_REDUCTION);
                String color = "&e";
                List<String> lore = new ArrayList<>();
                boolean enchanted = false;
                List<Double> upgradeData = ShopAttributeType.FALL_DAMAGE_REDUCTION.getAddition();

                if (level < 3) {
                    double totalAddition = Utility.sum(upgradeData, 0, level);
                    lore.add("&7Giảm ST rơi &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    PricePair pricePair = ShopAttributeType.FALL_DAMAGE_REDUCTION.getPrice().get(level);
                    lore.add("");
                    if (Talents.getInstance().getApi().getShopUtil().calculateMoney(p, pricePair.getA()) < pricePair.getB()) {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&cBạn không có đủ " + ConversionUtil.getMaterialName(pricePair.getA()) + "&c!");

                    } else {
                        lore.add("&7Giá nâng cấp: " + ConversionUtil.getMaterialColorCode(pricePair.getA()) + pricePair.getB() + " " + ConversionUtil.getMaterialName(pricePair.getA()) + "&f.");
                        lore.add("");
                        lore.add("&eBấm để nâng cấp.");
                    }
                } else {
                    double totalAddition = Utility.sum(upgradeData, 0, 2);
                    lore.add("&7Giảm ST rơi &a+" + Utility.toDecimal(100*totalAddition) + "%&7.");
                    lore.add("");
                    lore.add("&aBạn đã nâng tối đa điểm này!");
                    color = "&b";
                    enchanted = true;
                }
                String roman = Utility.toRoman(level);
                if (!roman.isEmpty()) {
                    roman = " " + roman;
                }
                return ConversionUtil.createItem(Material.FEATHER, level, "",
                        0, color+"Giảm sát thương rơi" + roman,
                        lore,
                        enchanted);
            } else {
                return ConversionUtil.createItemLite(Material.BEDROCK, 1, "&cUnknown");
            }
        }));

        menu.put(49, Collections.getCloseButton());
        menu.put(45, new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            ShopManager.getShop().open(p, PlayerQuickBuyCache.getQuickBuyCache(p.getUniqueId()), true);
        }, (p) -> ConversionUtil.createItem(Material.ARROW,
                    1, "", 0, "&aBack!", java.util.Collections.emptyList(), false)));
    }

    public HashMap<Integer, Button> getMenu() {
        return menu;
    }

    public MenuHolder getHolder() {
        return holder;
    }

    public static AttributeMenu getInstance(Player p) {
        return shopAttributeMenus.get(p);
    }

    public void remove() {
        shopAttributeMenus.remove(p);
        menu.clear();
    }

    public class MenuHolder implements InventoryHolder {
        private Inventory inv;
        @Override
        public Inventory getInventory() {
            return inv;
        }

        public void open() {
            inv = Bukkit.createInventory(this, 54, ChatColor.GRAY + "Attribute Shop");
            for (int i : menu.keySet()) {
                inv.setItem(i, menu.get(i).getItemStackConsumer().accept(p));
            }
            p.openInventory(inv);
        }

        public void refreshSlot(int slot) {
            if (menu.get(slot) != null) {
                inv.setItem(slot, menu.get(slot).getItemStackConsumer().accept(p));
                p.openInventory(inv);
            }
        }
    }
}
