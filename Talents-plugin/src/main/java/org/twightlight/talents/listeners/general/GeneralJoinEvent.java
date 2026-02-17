package org.twightlight.talents.listeners.general;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.users.User;

import java.util.Map;

public class GeneralJoinEvent implements Listener {
   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player player = e.getPlayer();
      try {

         Talents.getInstance().getDb().createPlayerData(player);
         Talents.getInstance().getDb().createPlayerSkillsData(player);
         Talents.getInstance().getDb().createPlayerRuneData(player);

          User user = new User(player);

      } catch (Exception var10) {
         Bukkit.getLogger().severe("Error setting up talents for " + player.getName());
         var10.printStackTrace();
         throw new RuntimeException(var10);
      }
   }
}
