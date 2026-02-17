package org.twightlight.talents.talents;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.dispatcher.CustomListener;

import java.util.List;

public abstract class Talent implements CustomListener {

    protected String talentId;
    protected List<Integer> costList;

    public Talent(String talentId, List<Integer> costList) {
        this.talentId = talentId;
        this.costList = costList;
    }

    public List<Integer> getCostList() {
        return costList;
    }

    public String getTalentId() {
        return talentId;
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
}
