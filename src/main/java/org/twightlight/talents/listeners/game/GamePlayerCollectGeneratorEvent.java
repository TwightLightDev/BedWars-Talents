package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;

public class GamePlayerCollectGeneratorEvent implements Listener {
    @EventHandler
    public void onPlayerCollectGenerator(PlayerGeneratorCollectEvent e) {
        Talents.getInstance().getTalentsManagerService().handle(e.getPlayer(), "RSSB", TalentsCategory.Miscellaneous, Arrays.asList(e));
    }
}
