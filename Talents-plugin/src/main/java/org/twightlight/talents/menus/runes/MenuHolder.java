package org.twightlight.talents.menus.runes;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.twightlight.talents.menus.Button;

import java.util.HashMap;
import java.util.Map;

public class MenuHolder implements InventoryHolder {

    private Map<Integer, Button> buttons;

    public MenuHolder() {
        buttons = new HashMap<>();
    }

    public Map<Integer, Button> getButtons() {
        return buttons;
    }

    public void setButton(int i, Button b) {
        buttons.put(i, b);
    }

    public Button getButton(int i) {
        return buttons.get(i);
    }

    public void clear() {
        buttons.clear();
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
