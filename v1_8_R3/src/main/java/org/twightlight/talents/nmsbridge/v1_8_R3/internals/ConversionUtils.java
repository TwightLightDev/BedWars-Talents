package org.twightlight.talents.nmsbridge.v1_8_R3.internals;

import net.minecraft.server.v1_8_R3.DamageSource;

public class ConversionUtils {
   public static DamageSource getDamageSource(org.twightlight.talents.nmsbridge.abstracts.enums.DamageSource damageSource) {
      switch(damageSource) {
      case BURN:
         return DamageSource.BURN;
      case FALL:
         return DamageSource.FALL;
      case FIRE:
         return DamageSource.FIRE;
      case LAVA:
         return DamageSource.LAVA;
      case ANVIL:
         return DamageSource.ANVIL;
      case DROWN:
         return DamageSource.DROWN;
      case MAGIC:
         return DamageSource.MAGIC;
      case STUCK:
         return DamageSource.STUCK;
      case CACTUS:
         return DamageSource.CACTUS;
      case STARVE:
         return DamageSource.STARVE;
      case WITHER:
         return DamageSource.WITHER;
      case LIGHTNING:
         return DamageSource.LIGHTNING;
      case OUT_OF_WORLD:
         return DamageSource.OUT_OF_WORLD;
      case FALLING_BLOCK:
         return DamageSource.FALLING_BLOCK;
      default:
         return DamageSource.GENERIC;
      }
   }
}
