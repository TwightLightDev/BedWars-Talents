package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class ResourcesBonus implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public ResourcesBonus(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        PlayerGeneratorCollectEvent e = (PlayerGeneratorCollectEvent) params.get(0);
        Player p = e.getPlayer();
        Material material = e.getItemStack().getType();
        ItemStack itemStack = new ItemStack(material, 1);
        DebugService.debugMsg("Rolling chance for more items, current chance: " + level * 0.75 + "%");
        if (Utility.rollChance(level*0.75)) {
            p.getInventory().addItem(itemStack);
            DebugService.debugMsg("Bingo, received!");
        }
        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
