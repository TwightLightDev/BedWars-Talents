package org.twightlight.talents.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;

public class PlayerMenu {
    public static HashMap<Player, PlayerMenu> playerMenus = new HashMap<>();

    private final MenuHolder holder;
    private final HashMap<Integer, Button> menu = new HashMap<>();
    private final Player p;
    private int x;
    private int y;

    public PlayerMenu(Player p, int x, int y) {
        this.p = p;
        playerMenus.put(p, this);
        updateMenu(x, y);
        holder = new MenuHolder();
    }

    public void updateMenu(int x, int y) {
        menu.clear();
        this.x = x;
        this.y = y;
        for (int i = -3; i <= 3; i++) {
            for (int j = -1; j <= 2; j ++) {
                int slotInMenu = 22 + i + (9 * j);
                Button button = TalentsMenu.getButton(x + i, y - j);
                menu.put(slotInMenu, button);
            }
        }

        for (int i = 2; i <= 3; i++) {
            menu.put(i, Collections.getUpButton());
        }

        menu.put(4, Collections.getSoulStonesButton());

        for (int i = 5; i <= 6; i++) {
            menu.put(i, Collections.getUpButton());
        }

        menu.put(47, Collections.getDownButton());
        menu.put(49, Collections.getResetButton());
        menu.put(51, Collections.getDownButton());

        menu.put(48, Collections.getResetPosButton());
        menu.put(50, Collections.getCloseButton());

        menu.put(26, Collections.getRightButton());
        menu.put(35, Collections.getRightButton());

        menu.put(18, Collections.getLeftButton());
        menu.put(27, Collections.getLeftButton());

        menu.put(0, Collections.getUpLeftButton());
        menu.put(1, Collections.getUpLeftButton());
        menu.put(9, Collections.getUpLeftButton());

        menu.put(7, Collections.getUpRightButton());
        menu.put(8, Collections.getUpRightButton());
        menu.put(17, Collections.getUpRightButton());

        menu.put(36, Collections.getDownLeftButton());
        menu.put(45, Collections.getDownLeftButton());
        menu.put(46, Collections.getDownLeftButton());

        menu.put(44, Collections.getDownRightButton());
        menu.put(52, Collections.getDownRightButton());
        menu.put(53, Collections.getDownRightButton());
    }

    public HashMap<Integer, Button> getMenu() {
        return menu;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public MenuHolder getHolder() {
        return holder;
    }

    public static PlayerMenu getInstance(Player p) {
        return playerMenus.get(p);
    }


    public class MenuHolder implements InventoryHolder {
        private Inventory inv;
        @Override
        public Inventory getInventory() {
            return inv;
        }

        public void open() {
            inv = Bukkit.createInventory(this, 54, ChatColor.YELLOW + "Talents");
            for (int i : menu.keySet()) {
                inv.setItem(i, menu.get(i).getItemStackConsumer().accept(p));
            }
            p.openInventory(inv);
        }

        public void refreshSlot(int slot) {
            if (menu.get(slot) != null) {
                inv.setItem(slot, menu.get(slot).getItemStackConsumer().accept(p));
                p.openInventory(inv);
            }
        }
    }
}
