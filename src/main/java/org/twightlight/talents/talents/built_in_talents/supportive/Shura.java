package org.twightlight.talents.talents.built_in_talents.supportive;

import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;

import java.util.List;

public class Shura implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Shura(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        PlayerKillEvent e = (PlayerKillEvent) params.get(0);
        Player p = e.getKiller();
        p.setHealth(Math.min(((level * 1.5) / 100) * p.getMaxHealth() + p.getHealth(), p.getMaxHealth()));

        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
