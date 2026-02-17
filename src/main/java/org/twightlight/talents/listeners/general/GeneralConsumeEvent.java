package org.twightlight.talents.listeners.general;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;


public class GeneralConsumeEvent implements Listener {
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getItem().getType() == Material.GOLDEN_APPLE) {
            Talents.getInstance().getTalentsManagerService().
                    handle(e.getPlayer(), "BGA", TalentsCategory.Miscellaneous, Arrays.asList(e.getPlayer()));
        }
    }
}
