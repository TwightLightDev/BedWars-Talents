package org.twightlight.talents.utils;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConversionUtils {
   private static final Map<Integer, PricePair> priceMap = new HashMap<>();
   private static final Map<DyeColor, Short> colorToData;
   private static final Map<Short, DyeColor> dataToColor;

   public static String getMaterialName(Material material) {
      if (material == Material.IRON_INGOT) {
         return "&fSắt&r";
      } else if (material == Material.GOLD_INGOT) {
         return "&eVàng&r";
      } else {
         return material == Material.EMERALD ? "&aLục Bảo&r" : "";
      }
   }

   public static String getMaterialColorCode(Material material) {
      if (material == Material.IRON_INGOT) {
         return "&f";
      } else if (material == Material.GOLD_INGOT) {
         return "&e";
      } else if (material == Material.EMERALD) {
         return "&a";
      } else {
         return material == Material.DIAMOND_SWORD ? "&b" : "";
      }
   }

   public static short getDataValue(DyeColor color) {
      return colorToData.getOrDefault(color, Short.valueOf("0"));
   }

   public static DyeColor getColorName(short dataValue) {
      return dataToColor.getOrDefault(dataValue, DyeColor.WHITE);
   }

   public static Map<Integer, PricePair> getPriceMap() {
      return priceMap;
   }

   static {
      priceMap.put(0, new PricePair(Material.IRON_INGOT, 20));
      priceMap.put(1, new PricePair(Material.GOLD_INGOT, 7));
      priceMap.put(2, new PricePair(Material.EMERALD, 2));
      colorToData = new HashMap();
      dataToColor = new HashMap();
      colorToData.put(DyeColor.WHITE, Short.valueOf("0"));
      colorToData.put(DyeColor.ORANGE, Short.valueOf("1"));
      colorToData.put(DyeColor.MAGENTA, Short.valueOf("2"));
      colorToData.put(DyeColor.LIGHT_BLUE, Short.valueOf("3"));
      colorToData.put(DyeColor.YELLOW, Short.valueOf("4"));
      colorToData.put(DyeColor.LIME, Short.valueOf("5"));
      colorToData.put(DyeColor.PINK, Short.valueOf("6"));
      colorToData.put(DyeColor.GRAY, Short.valueOf("7"));
      colorToData.put(DyeColor.SILVER, Short.valueOf("8"));
      colorToData.put(DyeColor.CYAN, Short.valueOf("9"));
      colorToData.put(DyeColor.PURPLE, Short.valueOf("10"));
      colorToData.put(DyeColor.BLUE, Short.valueOf("11"));
      colorToData.put(DyeColor.BROWN, Short.valueOf("12"));
      colorToData.put(DyeColor.GREEN, Short.valueOf("13"));
      colorToData.put(DyeColor.RED, Short.valueOf("14"));
      colorToData.put(DyeColor.BLACK, Short.valueOf("15"));

       for (Entry<DyeColor, Short> entry : colorToData.entrySet()) {
           dataToColor.put(entry.getValue(), (DyeColor) entry.getKey());
       }

   }

    public static ItemStack createItemStack(String material, int amount, short data) {
        ItemStack i;
        try {
            Material Xmaterial = XMaterial.valueOf(material).parseMaterial();
            if (Xmaterial == null) {
                return XMaterial.BEDROCK.parseItem();
            }

            if (amount <= 0) {
                amount = 1;
            }

            i = new ItemStack(Xmaterial, amount, data);
        } catch (Exception var5) {
            i = XMaterial.BEDROCK.parseItem();
        }

        return i;
    }

    public static ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        return createItem(material, 1, headUrl, data, displayName, lore, enchanted);
    }

    public static ItemStack createItemLite(Material material, int amount, String displayName) {
        ItemStack is = createItemStack(XMaterial.matchXMaterial(material).name(), amount, (short)0);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        is.setItemMeta(meta);
        return is;
    }

    public static ItemStack createItem(Material material, int amount, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = createItemStack(XMaterial.matchXMaterial(material).name(), amount, (short)data);
        if (i == null) {
            return null;
        } else {
            ItemMeta itemMeta = i.getItemMeta();
            if (itemMeta == null) {
                return null;
            } else {
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                if (lore != null && !lore.isEmpty()) {
                    List<String> coloredLore = lore.stream().map((line) -> {
                        return ChatColor.translateAlternateColorCodes('&', line);
                    }).collect(Collectors.toList());
                    itemMeta.setLore(coloredLore);
                }

                if (enchanted) {
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
                }

                itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                i.setItemMeta(itemMeta);
                if (material == XMaterial.PLAYER_HEAD.parseMaterial() && headUrl != null && !headUrl.trim().isEmpty()) {
                    try {
                        SkullMeta skullMeta = (SkullMeta)i.getItemMeta();
                        GameProfile profile = new GameProfile(UUID.randomUUID(), (String)null);
                        if (isValidBase64(headUrl)) {
                            profile.getProperties().put("textures", new Property("textures", headUrl));
                            Field field = skullMeta.getClass().getDeclaredField("profile");
                            field.setAccessible(true);
                            field.set(skullMeta, profile);
                            i.setItemMeta(skullMeta);
                        } else {
                            i.setItemMeta(skullMeta);
                            Bukkit.getLogger().warning("Invalid head URL: " + headUrl);
                        }
                    } catch (IllegalAccessException | NoSuchFieldException var12) {
                        var12.printStackTrace();
                    }
                }

                return i;
            }
        }
    }

    public static boolean isValidBase64(String base64) {
        String base64Pattern = "^[A-Za-z0-9+/]+={0,2}$";
        return base64 != null && base64.length() % 4 == 0 && Pattern.matches(base64Pattern, base64);
    }
}
