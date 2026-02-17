package org.twightlight.talents.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;

public class PricePair {
   private Material a;
   private Integer b;

   public PricePair(Material A, Integer B) {
      this.a = A;
      this.b = B;
   }

   public Material getA() {
      return this.a;
   }

   public Integer getB() {
      return this.b;
   }

   public void setA(Material a) {
      this.a = a;
   }

   public void setB(Integer b) {
      this.b = b;
   }
}
