package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.Arena;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.menus.AttributeMenu;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.ConversionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameTeamAssign implements Listener {

    private static final List<String> miscellaneous = Arrays.asList("IRSS", "IW", "IWD", "IES", "APR");


    @EventHandler
    public void onTeamAssign(TeamAssignEvent e) {
        Player p = e.getPlayer();
        Arena arenaInstance = ConversionUtil.getArenaFromIArena(e.getArena());
        if (arenaInstance == null) {
            arenaInstance = new Arena(e.getArena(), new ArrayList<>());
        }
        org.twightlight.talents.internal.Player player = new org.twightlight.talents.internal.Player(p);
        arenaInstance.addPlayer(player);
        AttributeMenu menu = new AttributeMenu(p);
        arenaInstance.addAttributeMenu(menu);
        Object returned = Talents.getInstance().getTalentsManagerService()
                .handle(p, "MHP", TalentsCategory.Protective, null);

        if (returned instanceof Double) {
            p.setMaxHealth((p.getMaxHealth() + (double) returned));
            p.setHealth(p.getMaxHealth());
            Double prev = player.getAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH);
            player.modifyAttribute(PlayerAttribute.TOTAL_ADDITIONAL_HEALTH, prev + (double) returned);
        }

        EntityLiving handle = ((CraftLivingEntity) p).getHandle();
        handle.setAbsorptionHearts((float) 0);

        Talents.getInstance().getTalentsManagerService()
                .handle(p, "YH", TalentsCategory.Protective, Arrays.asList(p));

        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            for (String id : miscellaneous) {
                Talents.getInstance().getTalentsManagerService()
                        .handle(p, id, TalentsCategory.Miscellaneous, Arrays.asList(p, e.getTeam()));
            }

        }, 2);

        arenaInstance.addTask((BukkitTask) Talents.getInstance().getTalentsManagerService()
                .handle(p, "GGF", TalentsCategory.Miscellaneous, Arrays.asList(p)));
        arenaInstance.addTask((BukkitTask) Talents.getInstance().getTalentsManagerService()
                .handle(p, "GNT", TalentsCategory.Protective, Arrays.asList(p, e.getArena())));

    }
}
