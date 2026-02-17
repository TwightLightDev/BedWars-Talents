package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.Arrays;

public class GameReSpawnEvent implements Listener {
    @EventHandler
    public void onReSpawn(PlayerReSpawnEvent e) {
        Player p = e.getPlayer();

        p.setMaxHealth(20);
        p.setHealth(20);

        Double addition_hp = ConversionUtil.getPlayerFromBukkitPlayer(p).getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH);

        p.setMaxHealth((p.getMaxHealth() + addition_hp));
        p.setHealth(p.getMaxHealth());

        EntityLiving handle = ((CraftLivingEntity) p).getHandle();
        handle.setAbsorptionHearts((float) 0);

        Talents.getInstance().getTalentsManagerService().
                handle(e.getPlayer(), "AGF", TalentsCategory.Miscellaneous, Arrays.asList(p));
        Talents.getInstance().getTalentsManagerService().
                handle(e.getPlayer(), "WDT", TalentsCategory.Miscellaneous, Arrays.asList(p));
        Talents.getInstance().getTalentsManagerService().
                handle(e.getPlayer(), "EST", TalentsCategory.Miscellaneous, Arrays.asList(p));
        Talents.getInstance().getTalentsManagerService().
                handle(e.getPlayer(), "WT", TalentsCategory.Miscellaneous, Arrays.asList(p, e.getTeam()));
        ConversionUtil.getPlayerFromBukkitPlayer(p).setVulnerable(false);
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            ConversionUtil.getPlayerFromBukkitPlayer(p).setVulnerable(true);
        }, 20);
    }
}
