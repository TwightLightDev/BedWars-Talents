package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.twightlight.talents.utils.ConversionUtil;

public class GameLeaveEvent implements Listener {
    @EventHandler
    public void onQuit(PlayerLeaveArenaEvent e) {
        if (ConversionUtil.getPlayerFromBukkitPlayer(e.getPlayer()) != null) {
            ConversionUtil.getPlayerFromBukkitPlayer(e.getPlayer()).setPlaying(false);
            e.getPlayer().setMaxHealth(20);
            e.getPlayer().setHealth(20);
            EntityLiving handle = ((CraftLivingEntity) e.getPlayer()).getHandle();
            handle.setAbsorptionHearts((float) 0);
        }
    }
}
