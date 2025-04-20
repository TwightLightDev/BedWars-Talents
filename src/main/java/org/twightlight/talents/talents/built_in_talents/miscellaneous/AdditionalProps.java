package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AdditionalProps implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    private final List<ItemStack> normal = new ArrayList<>();
    private final List<ItemStack> special = new ArrayList<>();
    private final Random random = new Random();

    public AdditionalProps(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;

        special.add(new ItemStack(Material.ENDER_PEARL));
        special.add(ConversionUtil.createItemLite(Material.EGG, 1, "&rBridge Egg"));
        special.add(ConversionUtil.createItemLite(Material.MONSTER_EGG, 1, "&rDream Defender"));

        normal.add(new ItemStack(Material.GOLDEN_APPLE));
        normal.add(new ItemStack(Material.TNT));
        normal.add(new ItemStack(Material.SPONGE));
        normal.add(new ItemStack(Material.WATER_BUCKET));
        normal.add(ConversionUtil.
                createItemLite(Material.FIREBALL, 1,
                        "&rFireball"));
        normal.add(ConversionUtil.
                createItemLite(Material.SNOW_BALL, 1,
                        "&rBed Bug"));
        normal.add(ConversionUtil.
                createItemLite(Material.MILK_BUCKET, 1, "&rMagic Milk"));
        normal.add(ConversionUtil.
                createItemLite(Material.CHEST, 1, "&rCompact Pop Up Tower"));
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        Player p = (Player) params.get(0);
        int totalItem = 1 + Math.round(level/5);
        for (int i = 0; i <= totalItem; i++) {
            if (Utility.rollChance(level * 1.5)) {
                p.getInventory().addItem(special.get(random.nextInt(special.size())));
            } else {
                p.getInventory().addItem(normal.get(random.nextInt(normal.size())));

            }
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
