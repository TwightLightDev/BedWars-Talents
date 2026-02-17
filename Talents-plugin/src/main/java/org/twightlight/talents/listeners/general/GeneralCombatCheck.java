package org.twightlight.talents.listeners.general;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GeneralCombatCheck implements Listener {
    private static Map<UUID, Long> combatMap = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!e.isCancelled()) {
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                combatMap.put(e.getDamager().getUniqueId(), System.currentTimeMillis());
                combatMap.put(e.getEntity().getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    public static boolean isInCombat(Player player) {
        return System.currentTimeMillis() - combatMap.getOrDefault(player.getUniqueId(), 0L) <= 5000L;
    }
}

