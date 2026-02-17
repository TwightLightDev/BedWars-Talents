package org.twightlight.talents.attributes;

import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.PricePair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum AttributeShopType {
   MELEE_DAMAGE(BaseStats.MELEE_DAMAGE.name(), Arrays.asList(0.5D, 0.75D, 0.75D), ConversionUtils.getPriceMap()),
   MELEE_PENETRATION(BaseStats.PENETRATION_RATIO.name(), Arrays.asList(3D, 3D, 4D), ConversionUtils.getPriceMap()),
   MELEE_CRITICAL_CHANCE(BaseStats.MELEE_CRITICAL_RATE.name(), Arrays.asList(6.0D, 6.0D, 8.0D), ConversionUtils.getPriceMap()),
   MELEE_CRITICAL_DAMAGE(BaseStats.MELEE_CRITICAL_DAMAGE.name(), Arrays.asList(15D, 15D, 20D), ConversionUtils.getPriceMap()),
   ARROW_DAMAGE(BaseStats.RANGED_DAMAGE.name(), Arrays.asList(0.75D, 1.0D, 1.25D), ConversionUtils.getPriceMap()),
   ARROW_PENETRATION(BaseStats.RANGED_PENETRATION_RATIO.name(), Arrays.asList(4D, 5D, 6D), ConversionUtils.getPriceMap()),
   ARROW_CRITICAL_CHANCE(BaseStats.RANGED_CRITICAL_RATE.name(), Arrays.asList(6.0D, 6.0D, 8.0D), ConversionUtils.getPriceMap()),
   ARROW_CRITICAL_DAMAGE(BaseStats.RANGED_CRITICAL_DAMAGE.name(), Arrays.asList(15D, 15D, 20D), ConversionUtils.getPriceMap()),
   ADDITIONAL_HEALTH(BaseStats.GENERIC_ADDITIONAL_MAX_HEALTH.name(), Arrays.asList(1.0D, 2.0D, 2.0D), ConversionUtils.getPriceMap()),
   DAMAGE_REDUCTION(BaseStats.DAMAGE_REDUCTION.name(), Arrays.asList(4.5D, 4.5D, 6D), ConversionUtils.getPriceMap()),
   CRITICAL_DAMAGE_REDUCTION(BaseStats.CRITICAL_DAMAGE_REDUCTION.name(), Arrays.asList(20D, 30D, 30D), ConversionUtils.getPriceMap()),
   FALL_DAMAGE_REDUCTION(BaseStats.FALL_DAMAGE_REDUCTION.name(), Arrays.asList(10D, 15D, 15D), ConversionUtils.getPriceMap());

   private List<Double> addition;
   private String stat;
   private Map<Integer, PricePair> price;

   private AttributeShopType(String stat, List<Double> addition, Map<Integer, PricePair> upgrade) {
      this.addition = addition;
      this.stat = stat;
      this.price = upgrade;
   }


   public List<Double> getAddition() {
      return this.addition;
   }

   public String getTargetStat() {
      return this.stat;
   }

   public Map<Integer, PricePair> getPrice() {
      return this.price;
   }

}
