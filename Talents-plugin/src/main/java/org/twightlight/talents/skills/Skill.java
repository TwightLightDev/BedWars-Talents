package org.twightlight.talents.skills;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.twightlight.talents.dispatcher.CustomListener;

import java.util.List;

public abstract class Skill implements CustomListener {

    protected String talentId;
    protected List<Integer> costList;

    public Skill(String talentId, List<Integer> costList) {
        this.talentId = talentId;
        this.costList = costList;
    }

    public List<Integer> getCostList() {
        return costList;
    }

    public String getSkillId() {
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
