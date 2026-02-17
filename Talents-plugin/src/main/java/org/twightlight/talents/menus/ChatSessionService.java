package org.twightlight.talents.menus;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.function.Consumer;

public class ChatSessionService {
   public static HashMap<Player, Consumer<String>> sessions = new HashMap<>();

   public static void createSession(Player p, Consumer<String> consumer) {
      sessions.putIfAbsent(p, consumer);
   }

   public static void handle(Player p, String input) {
       if ((sessions.get(p)) != null) {
           (sessions.get(p)).accept(input);
           end(p);
       }

   }

   public static void end(Player p) {
      sessions.remove(p);
   }

   public static boolean isInSession(Player p) {
      return sessions.containsKey(p);
   }
}
