package org.twightlight.talents.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.menus.interfaces.Consumer;
import org.twightlight.talents.menus.interfaces.Executable;

public class Button {
   private final Executable executable;
   private final Consumer<ItemStack, Player> itemStackConsumer;

   public Button(Executable executable, Consumer<ItemStack, Player> itemStackConsumer) {
      this.executable = executable;
      this.itemStackConsumer = itemStackConsumer;
   }

   public Executable getExecutable() {
      return this.executable;
   }

   public Consumer<ItemStack, Player> getItemStackConsumer() {
      return this.itemStackConsumer;
   }
}
