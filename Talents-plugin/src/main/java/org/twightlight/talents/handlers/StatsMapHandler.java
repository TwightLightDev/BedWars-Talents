package org.twightlight.talents.handlers;

import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Bukkit;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;

import java.util.UUID;

public class StatsMapHandler extends org.twightlight.pvpmanager.handlers.StatsMapHandler {

    @Override
    public StatsMap getStatMap(UUID uuid) {
        IArena iArena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(Bukkit.getPlayer(uuid));
        if (iArena == null) return  null;
        Arena arena = Talents.getInstance().getArenaManager().getArenaFromIArena(iArena);
        if (arena == null) return null;

        return arena.getStatsMapOfUUID(uuid);
    }
}
