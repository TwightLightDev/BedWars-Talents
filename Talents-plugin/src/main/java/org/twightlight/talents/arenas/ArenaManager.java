package org.twightlight.talents.arenas;

import com.andrei1058.bedwars.api.arena.IArena;

import java.util.HashMap;
import java.util.Map;

public class ArenaManager {
    public Map<IArena, Arena> arenaMap;

    public ArenaManager() {
        arenaMap = new HashMap<>();
    }

    public Map<IArena, Arena> getArenaMap() {
        return arenaMap;
    }

    public Arena getArenaFromIArena(IArena arena) {
        return arenaMap.getOrDefault(arena, null);
    }

    public Arena createNewArena(IArena iArena) {
        Arena arena = new Arena(iArena);

        arenaMap.put(iArena, arena);
        return arena;
    }

}
