package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.entity.Player;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class InstantRespawn implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public InstantRespawn(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        PlayerKillEvent e = (PlayerKillEvent) params.get(0);
        Player p = e.getVictim();
        DebugService.debugMsg("Rolling chance for fast respawn, current chance: " + level * 3 + "%");
        if (Utility.rollChance(level*3)) {
            e.getArena().getRespawnSessions().put(p, 1);
            DebugService.debugMsg("Bingo, respawned!");
        }
        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
