package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;

import java.util.List;
import java.util.Random;

public class WoolSupply extends Talent {
    private final Random random = new Random();

    public WoolSupply(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            e.getArena().getPlayers().forEach((p) -> {

                User user = User.getUserFromBukkitPlayer(p);
                int level = user.getTalentLevel(getTalentId());

                ITeam team = e.getArena().getTeam(p);
                DyeColor color = team.getColor().dye();
                ItemStack i = new ItemStack(Material.WOOL, level * 4, ConversionUtils.getDataValue(color));
                p.getInventory().addItem(new ItemStack[]{i});

            });
        }, 20L);
    }
}
