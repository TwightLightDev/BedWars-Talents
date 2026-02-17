package org.twightlight.talents.internal;

public enum PlayerAttribute {
    TOTAL_ADDITIONAL_HEALTH(0, Double.class),
    MELEE_DAMAGE(1, Double.class),
    ARROW_DAMAGE(2, Double.class),
    MELEE_CRITICAL_CHANCE(3, Double.class),
    MELEE_CRITICAL_DAMAGE(4, Double.class),
    ARROW_CRITICAL_CHANCE(5, Double.class),
    ARROW_CRITICAL_DAMAGE(6, Double.class),
    MELEE_LIFE_STEALS(7, Double.class),
    ARROW_LIFE_STEALS(8, Double.class),
    CURRENT_KILLS(9, Integer.class),
    CURRENT_DEATHS(10, Integer.class),
    HIT_STREAK(11, Integer.class),
    MELEE_PENETRATION(12, Double.class),
    ARROW_PENETRATION(13, Double.class),
    TOTAL_ADDITIONAL_DAMAGE_REDUCTION(14, Double.class),
    TOTAL_ADDITIONAL_FALL_DAMAGE_REDUCTION(15, Double.class),
    TOTAL_ADDITIONAL_CRITICAL_DAMAGE_REDUCTION(16, Double.class),
    TOTAL_ADDITIONAL_BLOCK_CHANCE(17, Double.class),
    TOTAL_ADDITIONAL_BLOCK_AMOUNT(18, Double.class);


    private int index;
    private Class<?> clazz;

    PlayerAttribute(int index, Class<?> clazz) {
        this.index = index;
        this.clazz = clazz;
    }

    public int getIndex() {
        return index;
    }

    public Class<?> getTypeClass() {
        return clazz;
    }
}
