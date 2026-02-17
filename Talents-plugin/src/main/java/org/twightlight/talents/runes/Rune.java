package org.twightlight.talents.runes;

import org.bukkit.event.Event;
import org.twightlight.talents.dispatcher.CustomListener;

public abstract class Rune implements CustomListener {

    protected String runeId;
    protected String category;
    protected int tier;

    public Rune(String category, int tier) {
        this.runeId = getClass().getSimpleName();
        this.category = category;
        this.tier = tier;
    }

    public String getCategory() {
        return category;
    }

    public String getRuneId() {
        return runeId;
    }

    public void onRegister() {
        return;
    }

    public void onUnregister() {
        return;
    }

    public boolean onDispatchError(Event event, String method, Exception exception) {
        return false;
    }

    public int getTier() {
        return tier;
    }
}
