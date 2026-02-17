package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.users.InGameData;

public class GameInGameDataEvent implements Listener {
    @EventHandler
    public void onPlayerKill(PlayerKillEvent e) {
        IArena iArena = e.getArena();
        Arena arena = Talents.getInstance().getArenaManager().getArenaFromIArena(iArena);
        if (e.getKiller() != null) {
            InGameData aData = arena.getInGameDataOfUUID(e.getKiller().getUniqueId());
            aData.add(InGameData.CURRENT_KILLS, 1);
        }
        InGameData vData = arena.getInGameDataOfUUID(e.getVictim().getUniqueId());
        vData.add(InGameData.CURRENT_DEATH, 1);
    }
}
