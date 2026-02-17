package org.twightlight.talents.listeners.game;

import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.Arena;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;
import java.util.UUID;

public class GameEndEvent implements Listener {
    @EventHandler
    public void onGameEnd(com.andrei1058.bedwars.api.events.gameplay.GameEndEvent e) {
        Arena arena = Arena.arenas.get(e.getArena());
        for (UUID uuid : e.getWinners()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                Talents.getInstance().getTalentsManagerService().
                        handle(p, "MSS", TalentsCategory.Miscellaneous, Arrays.asList(p));
                EntityLiving handle = ((CraftLivingEntity) p).getHandle();
                handle.setAbsorptionHearts((float) 0);
                p.setMaxHealth(20);
                p.setHealth(20);
            }
        }

        if (arena != null) {
            arena.closeInstance();
        }
    }
}
