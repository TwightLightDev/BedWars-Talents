package org.twightlight.talents.listeners.general;

import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GeneralIgniteCheck implements Listener {

    private final Set<UUID> ignoring = new HashSet<>();

    @EventHandler
    public void onEntityIgnite(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {

            UUID uuid = e.getEntity().getUniqueId();

            if (ignoring.contains(uuid)) return;

            e.setCancelled(true);

            LivingEntity entity = (LivingEntity) e.getEntity();
            int c_invulnerableTicks = entity.getNoDamageTicks();
            entity.setNoDamageTicks(0);

            ignoring.add(uuid);
            EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
            handle.damageEntity(DamageSource.BURN, 1);
            ignoring.remove(uuid);

            entity.setNoDamageTicks(c_invulnerableTicks);
        }
    }
}
