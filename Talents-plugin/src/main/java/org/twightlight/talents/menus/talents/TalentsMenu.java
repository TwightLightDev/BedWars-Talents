package org.twightlight.talents.menus.talents;

import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.Collections;

import java.util.HashMap;

public class TalentsMenu {
   public static HashMap<Integer, HashMap<Integer, Button>> menuItems = new HashMap<>();

   public static void setItem(int x, int y, Button button) {
      menuItems.computeIfAbsent(x, (k) -> {
         return new HashMap<>();
      });
      (menuItems.get(x)).put(y, button);
   }

   public static Button getButton(int x, int y) {
      if (menuItems.get(x) == null) {
         return Collections.getEmptyButton();
      } else {
         return (menuItems.get(x)).get(y) != null ? (menuItems.get(x)).get(y) : Collections.getEmptyButton();
      }
   }
}
