package org.twightlight.talents.utils;

import org.bukkit.ChatColor;
import org.twightlight.talents.Talents;

public class Debug {
    public static boolean debug = false;
    public static void debugMsg(String message) {
      if (Talents.isDebug()) {
         System.out.println(ChatColor.AQUA + "[Debug] " + message);
      }

   }
}
