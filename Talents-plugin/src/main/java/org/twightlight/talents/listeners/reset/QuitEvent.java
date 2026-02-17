package org.twightlight.talents.listeners.reset;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.users.User;

public class QuitEvent implements Listener {
   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      if (User.getUserFromBukkitPlayer(e.getPlayer()) != null) {
         e.getPlayer().setMaxHealth(20.0D);
         e.getPlayer().setHealth(20.0D);
         Talents.getInstance().getNMSBridge().getLivingEntityHelper().setAbsorptionHearts(e.getPlayer(), 0.0F);
      }
   }
}
