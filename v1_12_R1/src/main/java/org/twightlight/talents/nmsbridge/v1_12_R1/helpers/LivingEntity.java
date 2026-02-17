package org.twightlight.talents.nmsbridge.v1_12_R1.helpers;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.twightlight.talents.nmsbridge.NMSBridge;
import org.twightlight.talents.nmsbridge.abstracts.enums.DamageSource;
import org.twightlight.talents.nmsbridge.abstracts.objects.BoundingBox;
import org.twightlight.talents.nmsbridge.v1_12_R1.internals.ConversionUtils;

public class LivingEntity implements NMSBridge.LivingEntity {
   public void setAbsorptionHearts(org.bukkit.entity.LivingEntity entity, float amount) {
      EntityLiving handle = ((CraftLivingEntity)entity).getHandle();
      handle.setAbsorptionHearts(amount);
   }

   public float getAbsorptionHearts(org.bukkit.entity.LivingEntity entity) {
      EntityLiving handle = ((CraftLivingEntity)entity).getHandle();
      return handle.getAbsorptionHearts();
   }

   public void damageEntity(org.bukkit.entity.LivingEntity entity, DamageSource source, float amount) {
      EntityLiving handle = ((CraftLivingEntity)entity).getHandle();
      handle.damageEntity(ConversionUtils.getDamageSource(source), 1.0F);
   }

   public BoundingBox getBoundingBox(org.bukkit.entity.LivingEntity e) {
      EntityLiving handle = ((CraftLivingEntity)e).getHandle();
      AxisAlignedBB bb = handle.getBoundingBox();
      return new BoundingBox(bb.a, bb.b, bb.c, bb.d, bb.e, bb.f);
   }
}
