package org.twightlight.talents.utils;

import com.andrei1058.bedwars.api.arena.IArena;
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
import org.twightlight.talents.internal.Arena;
import org.twightlight.talents.internal.Player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConversionUtil {
    public static Player getPlayerFromBukkitPlayer(org.bukkit.entity.Player player) {
        return Player.playersInstance.getOrDefault(player, null);
    }

    private static Map<Integer, PricePair> priceMap = new HashMap<>();
    static {
        priceMap.put(0, new PricePair(Material.IRON_INGOT, 20));
        priceMap.put(1, new PricePair(Material.GOLD_INGOT, 7));
        priceMap.put(2, new PricePair(Material.EMERALD, 2));
    }

    public static Arena getArenaFromIArena(IArena arena) {
        return Arena.arenas.getOrDefault(arena, null);
    }
    public static ItemStack createItemStack(String material, int amount, short data) {
        ItemStack i;
        try {
            Material Xmaterial = XMaterial.valueOf(material).get();
            if (Xmaterial == null) {
                return XMaterial.BEDROCK.parseItem();
            }
            i = new ItemStack(Xmaterial, amount, data);
        } catch (Exception ex) {
            i = XMaterial.BEDROCK.parseItem();
        }
        return i;
    }

    public static ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        return createItem(material, 1, "", data, displayName, lore, enchanted);
    }

    public static ItemStack createItemLite(Material material, int amount, String displayName) {
        ItemStack is = createItemStack(XMaterial.matchXMaterial(material).name(), amount, (short) 0);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        is.setItemMeta(meta);
        return is;
    }

    public static ItemStack createItem(Material material, int amount, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = createItemStack(XMaterial.matchXMaterial(material).name(), amount, (short) data);
        if (i == null)
            return null;

        ItemMeta itemMeta = i.getItemMeta();
        if (itemMeta == null)
            return null;

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            itemMeta.setLore(coloredLore);
        }
        if (enchanted) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(itemMeta);

        if (material == XMaterial.PLAYER_HEAD.get() && headUrl != null && !headUrl.trim().isEmpty()) {
            try {
                SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);

                if (isValidBase64(headUrl)) {
                    profile.getProperties().put("textures", new Property("textures", headUrl));

                    Field field = skullMeta.getClass().getDeclaredField("profile");
                    field.setAccessible(true);
                    field.set(skullMeta, profile);
                    i.setItemMeta(skullMeta);
                } else {
                    Bukkit.getLogger().warning("Invalid head URL: " + headUrl);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return i;
    }

    public static boolean isValidBase64(String base64) {
        String base64Pattern = "^[A-Za-z0-9+/]+={0,2}$";
        return base64 != null && base64.length() % 4 == 0 && Pattern.matches(base64Pattern, base64);
    }

    private static final Map<DyeColor, Short> colorToData = new HashMap<>();
    private static final Map<Short, DyeColor> dataToColor = new HashMap<>();

    static {
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

        for (Map.Entry<DyeColor, Short> entry : colorToData.entrySet()) {
            dataToColor.put(entry.getValue(), entry.getKey());
        }
    }

    public static String getMaterialName(Material material) {
        if (material == Material.IRON_INGOT) {
            return "&fSắt&r";
        } else if (material == Material.GOLD_INGOT) {
            return "&eVàng&r";
        } else if (material == Material.EMERALD) {
            return "&aLục Bảo&r";
        }
        return "";
    }

    public static String getMaterialColorCode(Material material) {
        if (material == Material.IRON_INGOT) {
            return "&f";
        } else if (material == Material.GOLD_INGOT) {
            return "&e";
        } else if (material == Material.EMERALD) {
            return "&a";
        } else if (material == Material.DIAMOND_SWORD) {
            return "&b";
        }
        return "";
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
}
