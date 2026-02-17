package org.twightlight.talents.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class Utility {
   private static final DecimalFormat df = new DecimalFormat("0.#");

   public static void info(String message) {
      System.out.println(message);
   }

   public static void error(String message) {
      System.err.println(message);
   }

   public static boolean rollChance(double percent) {
      return Math.random() * 100.0D < percent;
   }

   public static String toDecimal(double value) {
      return df.format(value);
   }

   public static double sum(List<Double> list, int a, int b) {
      double sum = 0.0D;

      for(int i = a; i <= b && i < list.size(); ++i) {
         sum += list.get(i);
      }

      return sum;
   }

   public static int totalCost(List<Integer> costList, int from, int to) {
      int[] sum = prefixSum(costList);
      return sum[to + 1] - sum[from];
   }

   public static int[] prefixSum(List<Integer> arr) {
      int[] result = new int[arr.size() + 1];
      result[0] = 0;

      for(int i = 0; i < arr.size(); ++i) {
         result[i + 1] = result[i] + (Integer)arr.get(i);
      }

      return result;
   }


   public static boolean isInteger(String s) {
      try {
         Integer.parseInt(s);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static String toRoman(int number) {
      if (number == 0) {
         return "";
      } else if (number > 0 && number <= 3999) {
         int[] values = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
         String[] romans = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
         StringBuilder result = new StringBuilder();

         for(int i = 0; i < values.length; ++i) {
            while(number >= values[i]) {
               number -= values[i];
               result.append(romans[i]);
            }
         }

         return result.toString();
      } else {
         throw new IllegalArgumentException("Number must be between 1 and 3999");
      }
   }

   public static boolean contains(List<String> array, String target) {
       List<String> var2 = array;
      int var3 = array.size();

      for(int var4 = 0; var4 < var3; ++var4) {
         String s = var2.get(var4);
         if (Objects.equals(s, target)) {
            return true;
         }
      }

      return false;
   }

   public static double distance(Location pointLocation, Location targetLocation) {
      targetLocation.clone().setY(pointLocation.getY());
      return pointLocation.distance(targetLocation);
   }

   static {
      df.setMaximumFractionDigits(2);
   }
}
