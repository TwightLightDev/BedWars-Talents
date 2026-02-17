package org.twightlight.talents.listeners.reset;

import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.users.User;

public class GameLeaveEvent implements Listener {
   @EventHandler
   public void onQuit(PlayerLeaveArenaEvent e) {
      if (User.getUserFromBukkitPlayer(e.getPlayer()) != null) {
         e.getPlayer().setMaxHealth(20.0D);
         e.getPlayer().setHealth(20.0D);
         Talents.getInstance().getNMSBridge().getLivingEntityHelper().setAbsorptionHearts(e.getPlayer(), 0.0F);
      }
   }
}
