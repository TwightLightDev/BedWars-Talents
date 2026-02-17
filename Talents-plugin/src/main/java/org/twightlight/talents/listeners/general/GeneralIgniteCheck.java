package org.twightlight.talents.listeners.general;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.twightlight.talents.nmsbridge.abstracts.enums.DamageSource;
import org.twightlight.talents.Talents;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GeneralIgniteCheck implements Listener {
    private final Set<UUID> ignoring = new HashSet();

    @EventHandler
    public void onEntityIgnite(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            UUID uuid = e.getEntity().getUniqueId();
            if (this.ignoring.contains(uuid)) {
                return;
            }

            e.setCancelled(true);
            LivingEntity entity = (LivingEntity)e.getEntity();
            int c_invulnerableTicks = entity.getNoDamageTicks();
            entity.setNoDamageTicks(0);
            this.ignoring.add(uuid);
            if (entity.getHealth() > 0.5D

            ) {
                Talents.getInstance().getNMSBridge().getLivingEntityHelper().damageEntity(entity, DamageSource.BURN, 1.0F);
            }

            this.ignoring.remove(uuid);
            entity.setNoDamageTicks(c_invulnerableTicks);
        }
    }
}
