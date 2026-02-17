package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.player.PlayerReJoinEvent;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.internal.Player;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.utils.ConversionUtil;

public class GameRejoinEvent implements Listener {
    @EventHandler
    public void onPlayerRejoin(PlayerReJoinEvent e) {
        if (ConversionUtil.getPlayerFromBukkitPlayer(e.getPlayer()) != null) {
            Player p = ConversionUtil.getPlayerFromBukkitPlayer(e.getPlayer());
            p.setPlaying(true);
            e.getPlayer().setMaxHealth(20);
            e.getPlayer().setHealth(20);
            EntityLiving handle = ((CraftLivingEntity) e.getPlayer()).getHandle();
            handle.setAbsorptionHearts((float) 0);
            Double addition_hp = p.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH);

            e.getPlayer().setMaxHealth((e.getPlayer().getMaxHealth() + addition_hp));
            e.getPlayer().setHealth((e.getPlayer().getMaxHealth()));

        }
    }
}
