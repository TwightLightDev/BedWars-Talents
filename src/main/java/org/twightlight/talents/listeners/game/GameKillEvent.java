package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.Player;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.DebugService;

import java.util.Arrays;
import java.util.List;

public class GameKillEvent implements Listener {

    private static List<String> SUPPORTIVE_TALENTS = Arrays.asList("ARS", "WR", "ASA", "SLD");

    @EventHandler
    public void onPlayerKill(PlayerKillEvent e) {
        if (e.getCause().isFinalKill() && ConversionUtil.getPlayerFromBukkitPlayer(e.getVictim()) != null) {
            ConversionUtil.getPlayerFromBukkitPlayer(e.getVictim()).setPlaying(false);
            e.getVictim().setMaxHealth(20);
            e.getVictim().setHealth(20);
        }
        if (ConversionUtil.getPlayerFromBukkitPlayer(e.getKiller()) != null) {
            Player killer = ConversionUtil.getPlayerFromBukkitPlayer(e.getKiller());

            Integer kills = killer.getAttribute(PlayerAttribute.CURRENT_KILLS);
            killer.modifyAttribute(PlayerAttribute.CURRENT_KILLS, kills + 1);
            DebugService.debugMsg("Detected a kill, this player kills is: " + (kills + 1));

            for (String id : SUPPORTIVE_TALENTS) {
                Talents.getInstance().getTalentsManagerService().
                        handle(e.getKiller(), id, TalentsCategory.Supportive,
                                Arrays.asList(killer, kills+1, e.getKiller()));
            }
        }
        Talents.getInstance().getTalentsManagerService().handle(e.getVictim(), "IRS", TalentsCategory.Miscellaneous, Arrays.asList(e));

        Talents.getInstance().getTalentsManagerService().handle(e.getKiller(), "SHR", TalentsCategory.Supportive, Arrays.asList(e));
        Talents.getInstance().getTalentsManagerService().handle(e.getKiller(), "BTH", TalentsCategory.Supportive, Arrays.asList(e));

    }
}
