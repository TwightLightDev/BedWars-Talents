package org.twightlight.talents.internal;

import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.PricePair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum ShopAttributeType {
    MELEE_DAMAGE(0, PlayerAttribute.MELEE_DAMAGE, Arrays.asList(0.5D, 0.75D, 0.75D), ConversionUtil.getPriceMap()),
    MELEE_PENETRATION(1, PlayerAttribute.MELEE_PENETRATION, Arrays.asList(0.03D, 0.03D, 0.04D), ConversionUtil.getPriceMap()),
    MELEE_CRITICAL_CHANCE(2, PlayerAttribute.MELEE_CRITICAL_CHANCE, Arrays.asList(6D, 6D, 8D), ConversionUtil.getPriceMap()),
    MELEE_CRITICAL_DAMAGE(3, PlayerAttribute.MELEE_CRITICAL_DAMAGE, Arrays.asList(0.15D, 0.15D, 0.2D), ConversionUtil.getPriceMap()),
    ARROW_DAMAGE(4, PlayerAttribute.ARROW_DAMAGE, Arrays.asList(0.75D, 1D, 1.25D), ConversionUtil.getPriceMap()),
    ARROW_PENETRATION(5, PlayerAttribute.ARROW_PENETRATION, Arrays.asList(0.04D, 0.05D, 0.06D), ConversionUtil.getPriceMap()),
    ARROW_CRITICAL_CHANCE(6, PlayerAttribute.ARROW_CRITICAL_CHANCE, Arrays.asList(6D, 6D, 8D), ConversionUtil.getPriceMap()),
    ARROW_CRITICAL_DAMAGE(7, PlayerAttribute.ARROW_CRITICAL_DAMAGE, Arrays.asList(0.15D, 0.15D, 0.2D), ConversionUtil.getPriceMap()),
    ADDITIONAL_HEALTH(8, PlayerAttribute.TOTAL_ADDITIONAL_HEALTH, Arrays.asList(1D, 2D, 2D), ConversionUtil.getPriceMap()),
    DAMAGE_REDUCTION(9, PlayerAttribute.TOTAL_ADDITIONAL_DAMAGE_REDUCTION, Arrays.asList(0.045D, 0.045D, 0.06D), ConversionUtil.getPriceMap()),
    CRITICAL_DAMAGE_REDUCTION(10, PlayerAttribute.TOTAL_ADDITIONAL_CRITICAL_DAMAGE_REDUCTION, Arrays.asList(0.2D, 0.3D, 0.3D), ConversionUtil.getPriceMap()),
    FALL_DAMAGE_REDUCTION(11, PlayerAttribute.TOTAL_ADDITIONAL_FALL_DAMAGE_REDUCTION, Arrays.asList(0.1D, 0.15D, 0.15D), ConversionUtil.getPriceMap());


    private int index;
    private List<Double> addition;
    private PlayerAttribute playerAttribute;
    private Map<Integer, PricePair> price;
    ShopAttributeType(int index, PlayerAttribute attribute, List<Double> addition, Map<Integer, PricePair> upgrade) {
        this.index = index;
        this.addition = addition;
        playerAttribute = attribute;
        this.price = upgrade;
    }

    public int getIndex() {
        return index;
    }

    public List<Double> getAddition() {
        return addition;
    }

    public PlayerAttribute getPlayerAttribute() {
        return playerAttribute;
    }

    public Map<Integer, PricePair> getPrice() {
        return price;
    }
}