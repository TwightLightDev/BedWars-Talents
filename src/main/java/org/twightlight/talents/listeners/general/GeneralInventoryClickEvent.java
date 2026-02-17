package org.twightlight.talents.listeners.general;

import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.twightlight.talents.menus.AttributeMenu;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.PlayerMenu;
import org.twightlight.talents.utils.ConversionUtil;

public class GeneralInventoryClickEvent implements Listener {
    @EventHandler
    public void onClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (PlayerMenu.getInstance(p) != null && e.getInventory().getHolder() instanceof PlayerMenu.MenuHolder && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            PlayerMenu menu = PlayerMenu.getInstance(p);
            Button b = menu.getMenu().get(e.getRawSlot());
            if (b.getExecutable() != null) {
                b.getExecutable().execute(e);
            }
            e.setCancelled(true);
        }

        if (AttributeMenu.getInstance(p) != null && e.getInventory().getHolder() instanceof AttributeMenu.MenuHolder && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            AttributeMenu menu = AttributeMenu.getInstance(p);
            Button b = menu.getMenu().get(e.getRawSlot());
            if (b.getExecutable() != null) {
                b.getExecutable().execute(e);
            }
            e.setCancelled(true);
        }

        String title = e.getInventory().getTitle();
        if (title.equals(Language.getMsg(p, "shop-items-messages.inventory-name"))) {
            if (ConversionUtil.getPlayerFromBukkitPlayer( p) == null) {
                return;
            }
            org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
            if (!player.isPlaying()) {
                return;
            }
            if (e.getCurrentItem() == null && e.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            if (e.getRawSlot() == 46) {
                e.setCancelled(true);
                AttributeMenu attributeMenu = AttributeMenu.getInstance(p);
                attributeMenu.getHolder().open();
            }
        }
    }
}
