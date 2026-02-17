package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.Arrays;

public class GameShopBuyEvent implements Listener {
    @EventHandler
    public void onPlayerBuy(ShopBuyEvent e) {
        if (!e.isCancelled()) {
            Player p = e.getBuyer();
            Talents.getInstance().getTalentsManagerService()
                    .handle(p, "MCH", TalentsCategory.Miscellaneous, Arrays.asList(e));
            Talents.getInstance().getTalentsManagerService()
                    .handle(p, "EMS", TalentsCategory.Miscellaneous, Arrays.asList(e));
        }
    }
}
