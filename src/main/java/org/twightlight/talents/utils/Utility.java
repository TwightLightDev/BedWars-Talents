package org.twightlight.talents.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class Utility {
    public static void info(String message) {
        System.out.println(message);
    }

    public static void error(String message) {
        System.err.println(message);
    }


    public static boolean rollChance(double percent) {
        return Math.random() * 100 < percent;
    }

    private static final DecimalFormat df;

    static {
        df = new DecimalFormat("0.#");
        df.setMaximumFractionDigits(2);
    }

    public static String toDecimal(double value) {
        return df.format(value);
    }

    public static double sum(List<Double> list, int a, int b) {
        double sum = 0;
        for (int i = a; i <= b && i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum;
    }

    public static int totalCost(List<Integer> costList, int from, int to) {
        int[] sum = prefixSum(costList);
        return (sum[to+1] - sum[from]);
    }

    public static int[] prefixSum(List<Integer>  arr) {
        int[] result = new int[arr.size() + 1];
        result[0] = 0;
        for (int i = 0; i < arr.size(); i++) {
            result[i + 1] = result[i] + arr.get(i);
        }
        return result;
    }

    public static int getArmorPoints(Player player) {
        int armorPoints = 0;

        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece == null || armorPiece.getType() == Material.AIR) continue;

            switch (armorPiece.getType()) {
                case LEATHER_HELMET: armorPoints += 1; break;
                case LEATHER_CHESTPLATE: armorPoints += 3; break;
                case LEATHER_LEGGINGS: armorPoints += 2; break;
                case LEATHER_BOOTS: armorPoints += 1; break;

                case GOLD_HELMET: armorPoints += 2; break;
                case GOLD_CHESTPLATE: armorPoints += 5; break;
                case GOLD_LEGGINGS: armorPoints += 3; break;
                case GOLD_BOOTS: armorPoints += 1; break;

                case CHAINMAIL_HELMET: armorPoints += 2; break;
                case CHAINMAIL_CHESTPLATE: armorPoints += 5; break;
                case CHAINMAIL_LEGGINGS: armorPoints += 4; break;
                case CHAINMAIL_BOOTS: armorPoints += 1; break;

                case IRON_HELMET: armorPoints += 2; break;
                case IRON_CHESTPLATE: armorPoints += 6; break;
                case IRON_LEGGINGS: armorPoints += 5; break;
                case IRON_BOOTS: armorPoints += 2; break;

                case DIAMOND_HELMET: armorPoints += 3; break;
                case DIAMOND_CHESTPLATE: armorPoints += 8; break;
                case DIAMOND_LEGGINGS: armorPoints += 6; break;
                case DIAMOND_BOOTS: armorPoints += 3; break;
            }
        }

        return armorPoints;
    }

    public static int getTotalEPF(Player player, EntityDamageEvent.DamageCause cause) {
        int epf = 0;

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) continue;

            for (Map.Entry<Enchantment, Integer> enchant : armor.getEnchantments().entrySet()) {
                Enchantment ench = enchant.getKey();
                int level = enchant.getValue();

                if (ench == Enchantment.PROTECTION_ENVIRONMENTAL) {
                    epf += level * 1;
                } else if (ench == Enchantment.PROTECTION_PROJECTILE && cause == EntityDamageEvent.DamageCause.PROJECTILE) {
                    epf += level * 2;
                } else if (ench == Enchantment.PROTECTION_FIRE &&
                        (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.LAVA)) {
                    epf += level * 2;
                } else if (ench == Enchantment.PROTECTION_EXPLOSIONS &&
                        (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                    epf += level * 2;
                } else if (ench == Enchantment.PROTECTION_FALL && cause == EntityDamageEvent.DamageCause.FALL) {
                    epf += level * 2;
                }
            }
        }

        return Math.min(epf, 20);
    }

    public static double calculateDamageReduction(Player player, double baseDamage, EntityDamageEvent.DamageCause cause) {
        double damage = baseDamage;

        int armorPoints = getArmorPoints(player);
        double armorMultiplier = 1.0 - Math.min(20, armorPoints) * 0.04;
        damage *= armorMultiplier;

        int epf = getTotalEPF(player, cause);
        double protMultiplier = 1.0 - Math.min(20, epf) * 0.04;
        damage *= protMultiplier;

        if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            int level = player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier();
            damage *= (1.0 - (level + 1) * 0.20);
        }

        return baseDamage - damage;
    }


    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Object getDefaultValue(Class<?> clazz) {
        if (clazz == boolean.class || clazz == Boolean.class) return false;
        if (clazz == byte.class || clazz == Byte.class) return (byte) 0;
        if (clazz == short.class || clazz == Short.class) return (short) 0;
        if (clazz == int.class || clazz == Integer.class) return 0;
        if (clazz == long.class || clazz == Long.class) return 0L;
        if (clazz == float.class || clazz == Float.class) return 0f;
        if (clazz == double.class || clazz == Double.class) return 0d;
        if (clazz == char.class || clazz == Character.class) return '\0';
        return null;
    }

    public static String toRoman(int number) {
        if (number == 0) {
            return "";
        }

        if (number <= 0 || number > 3999) {
            throw new IllegalArgumentException("Number must be between 1 and 3999");
        }

        int[] values = {1000, 900, 500, 400, 100,  90,  50,  40,  10,   9,   5,   4,   1};
        String[] romans = {"M", "CM", "D","CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                result.append(romans[i]);
            }
        }

        return result.toString();
    }

}
