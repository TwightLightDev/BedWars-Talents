package org.twightlight.talents.listeners.general;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.DebugService;

import java.util.HashMap;
import java.util.Map;

public class GeneralPlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        try {
            DebugService.debugMsg("Calling PlayerJoinEvent");
            if (Talents.getInstance().getDatabase().createPlayerData(player)) {
                for (TalentsCategory category : TalentsCategory.values()) {
                    Map<String, Integer> defaults = new HashMap<>();
                    for (String talent : Talents.getInstance().getTalentsManagerService().getInnerTalents(category)) {
                        defaults.put(talent, 0);
                    }
                    Talents.getInstance().getDatabase().pull(
                            player,
                            defaults,
                            category.getColumn()
                    );
                }
            }
        } catch (Exception ex) {
            Bukkit.getLogger().severe("Error setting up talents for " + player.getName());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
