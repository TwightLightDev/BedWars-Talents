package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;
import java.util.UUID;

public class ChampionsBonus extends Talent {

    public ChampionsBonus(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameEnd(GameEndEvent e) {
        List<UUID> ps = e.getWinners();

        for (UUID uuid : ps) {
            Player p = Bukkit.getPlayer(uuid);
            User user = User.getUserFromBukkitPlayer(p);
            int level = user.getTalentLevel(getTalentId());
            if (level == 0) return;
            int ss = Talents.getInstance().getDb().getSoulStones(p);
            Talents.getInstance().getDb().setSoulStones(p, ss + level);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bBạn nhận được thêm " + level + " &dĐá linh hồn &bnhờ tài năng &l&eChơi đá!"));
        }

    }

}
