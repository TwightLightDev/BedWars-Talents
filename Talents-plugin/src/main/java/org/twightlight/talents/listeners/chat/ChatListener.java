package org.twightlight.talents.listeners.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.menus.ChatSessionService;

public class ChatListener implements Listener {
   @EventHandler
   public void onPlayerChat(AsyncPlayerChatEvent e) {
      Player p = e.getPlayer();
      if (ChatSessionService.isInSession(p)) {
         e.setCancelled(true);
         Bukkit.getScheduler().runTask(Talents.getInstance(), () -> {
            try {
               ChatSessionService.handle(p, e.getMessage());
            } catch (NumberFormatException var3) {
               throw new RuntimeException(var3);
            }
         });
      }

   }
}
