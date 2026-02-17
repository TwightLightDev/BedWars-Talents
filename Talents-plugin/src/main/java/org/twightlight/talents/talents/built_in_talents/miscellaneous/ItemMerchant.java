package org.twightlight.talents.talents.built_in_talents.miscellaneous;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemMerchant extends Talent {
    private final List<ItemStack> normal = new ArrayList<>();
    private final List<ItemStack> special = new ArrayList<>();
    private final Random random = new Random();

    public ItemMerchant(String talentId, List<Integer> costList) {
        super(talentId, costList);
        this.special.add(new ItemStack(Material.ENDER_PEARL));
        this.special.add(ConversionUtils.createItemLite(Material.EGG, 1, "&rBridge Egg"));
        this.special.add(ConversionUtils.createItemLite(Material.MONSTER_EGG, 1, "&rDream Defender"));
        this.normal.add(new ItemStack(Material.GOLDEN_APPLE));
        this.normal.add(new ItemStack(Material.TNT));
        this.normal.add(new ItemStack(Material.SPONGE));
        this.normal.add(new ItemStack(Material.WATER_BUCKET));
        this.normal.add(ConversionUtils.createItemLite(Material.FIREBALL, 1, "&rFireball"));
        this.normal.add(ConversionUtils.createItemLite(Material.SNOW_BALL, 1, "&rBed Bug"));
        this.normal.add(ConversionUtils.createItemLite(Material.MILK_BUCKET, 1, "&rMagic Milk"));
        this.normal.add(ConversionUtils.createItemLite(Material.CHEST, 1, "&rCompact Pop Up Tower"));
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            e.getArena().getPlayers().forEach((p) -> {

                User user = User.getUserFromBukkitPlayer(p);
                int level = user.getTalentLevel(getTalentId());

                if (level == 0) {
                    return;
                }

                int totalItem = 1 + Math.round((float)(level / 5));

                for(int i = 0; i <= totalItem; ++i) {
                    if (Utility.rollChance((double)level * 1.5D)) {
                        p.getInventory().addItem(this.special.get(this.random.nextInt(this.special.size())));
                    } else {
                        p.getInventory().addItem(this.normal.get(this.random.nextInt(this.normal.size())));
                    }
                }
            });
        }, 20L);
    }
}
