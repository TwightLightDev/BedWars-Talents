package org.twightlight.talents.utils;

import org.bukkit.Material;

public class PricePair {
    private Material a;
    private Integer b;

    public PricePair(Material A, Integer B) {
        a = A;
        b = B;
    }

    public Material getA() {
        return a;
    }

    public Integer getB() {
        return b;
    }

    public void setA(Material a) {
        this.a = a;
    }

    public void setB(Integer b) {
        this.b = b;
    }
}
