package org.twightlight.talents.listeners.general;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class GeneralMoveEvent implements Listener {
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getPlayer().hasMetadata("frozen")) e.setCancelled(true);
    }
}
