package org.twightlight.talents.nmsbridge.v1_12_R1.internals;

import net.minecraft.server.v1_12_R1.DamageSource;

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
      case CRAMMING:
         return DamageSource.CRAMMING;
      case LIGHTNING:
         return DamageSource.LIGHTNING;
      case HOT_FLOOR:
         return DamageSource.HOT_FLOOR;
      case OUT_OF_WORLD:
         return DamageSource.OUT_OF_WORLD;
      case DRAGON_BREATH:
         return DamageSource.DRAGON_BREATH;
      case FALLING_BLOCK:
         return DamageSource.FALLING_BLOCK;
      case FLY_INTO_WALL:
         return DamageSource.FLY_INTO_WALL;
      default:
         return DamageSource.GENERIC;
      }
   }
}
