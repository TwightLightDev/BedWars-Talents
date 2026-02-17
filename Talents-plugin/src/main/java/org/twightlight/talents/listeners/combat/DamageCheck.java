package org.twightlight.talents.listeners.combat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.talents.users.User;

public class DamageCheck implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onMeleeDamage(MeleeDamageEvent e) {
        if (!e.getDamagePacket().victimIsPlayer()) return;
        if (User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()) == null) return;
        if (!User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()).isPlaying() || !User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()).isVulnerable()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRangedDamage(RangedDamageEvent e) {
        if (!e.getDamagePacket().victimIsPlayer()) return;
        if (User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()) == null) return;
        if (!User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()).isPlaying() || !User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()).isVulnerable()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUndefinedDamage(UndefinedDamageEvent e) {
        if (User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()) == null) return;
        if (!User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()).isPlaying() || !User.getUserFromBukkitPlayer(e.getDamagePacket().getVictimAsPlayer()).isVulnerable()) e.setCancelled(true);
    }
}
