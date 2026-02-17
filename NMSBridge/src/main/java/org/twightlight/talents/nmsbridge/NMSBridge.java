package org.twightlight.talents.nmsbridge;

import org.twightlight.talents.nmsbridge.abstracts.enums.DamageSource;
import org.twightlight.talents.nmsbridge.abstracts.objects.BoundingBox;

public abstract class NMSBridge {
   protected LivingEntity livingEntityHelper;

   public LivingEntity getLivingEntityHelper() {
      return this.livingEntityHelper;
   }

   public interface LivingEntity {
      void setAbsorptionHearts(org.bukkit.entity.LivingEntity var1, float var2);

      float getAbsorptionHearts(org.bukkit.entity.LivingEntity var1);

      void damageEntity(org.bukkit.entity.LivingEntity var1, DamageSource var2, float var3);

      BoundingBox getBoundingBox(org.bukkit.entity.LivingEntity var1);
   }
}
