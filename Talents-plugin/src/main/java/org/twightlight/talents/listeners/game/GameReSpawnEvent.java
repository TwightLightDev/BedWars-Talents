package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.users.User;

public class GameReSpawnEvent implements Listener {
   @EventHandler
   public void onReSpawn(PlayerReSpawnEvent e) {
      Player p = e.getPlayer();
      p.setMaxHealth(20.0D);
      p.setHealth(20.0D);

      User.getUserFromBukkitPlayer(p).setVulnerable(false);
      Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
          User.getUserFromBukkitPlayer(p).setVulnerable(true);
      }, 20L);
   }
}
