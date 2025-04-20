package org.twightlight.talents.talents.built_in_talents.miscellaneous;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.List;

public class MoreSoulStones implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public MoreSoulStones(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        try {
            int ss = Talents.getInstance().getDatabase().getSoulStones(p);
            Talents.getInstance().getDatabase().setSoulStones(p, ss + level);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bBạn nhận được thêm " + level + "&dĐá linh hồn nhờ tài năng &l&eChơi đá!"));
        } catch (Exception ignored) {}


        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
