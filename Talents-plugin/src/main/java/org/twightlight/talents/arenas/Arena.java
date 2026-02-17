package org.twightlight.talents.arenas;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.attributes.AttributeShopType;
import org.twightlight.talents.attributes.AttributesData;
import org.twightlight.talents.users.InGameData;
import org.twightlight.talents.utils.ConversionUtils;
import org.twightlight.talents.utils.PricePair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Arena {
    private IArena arena;

    private Map<UUID, StatsMap> statsMaps;
    private Map<UUID, AttributesData> attributesDataMap;
    private Map<UUID, InGameData> inGameDataMap;

    public Arena(IArena arena) {
        this.arena = arena;
        statsMaps = new HashMap<>();
        attributesDataMap = new HashMap<>();
        inGameDataMap = new HashMap<>();
    }

    public IArena getArena() {
        return arena;
    }

    public StatsMap getStatsMapOfUUID(UUID uuid) {
        return statsMaps.getOrDefault(uuid, null);
    }

    public Map<UUID, StatsMap> getStatsMaps() {
        return statsMaps;
    }

    public AttributesData getAttributesDataOfUUID(UUID uuid) {
        return attributesDataMap.getOrDefault(uuid, null);
    }

    public Map<UUID, AttributesData> getAttributesDataMap() {
        return attributesDataMap;
    }

    public Map<UUID, InGameData> getInGameDataMap() {
        return inGameDataMap;
    }

    public InGameData getInGameDataOfUUID(UUID uuid) {
        return inGameDataMap.getOrDefault(uuid, null);
    }

    public static class ShopUtils {
        public static void upgradeAttribute(UUID uuid, AttributeShopType type) {

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            BedWars api = Talents.getInstance().getAPI();
            IArena iarena = api.getArenaUtil().getArenaByPlayer(player);
            if (iarena == null) return;
            Arena arena = Talents.getInstance().getArenaManager().getArenaFromIArena(iarena);
            if (arena == null) return;
            AttributesData data = arena.getAttributesDataOfUUID(uuid);
            if (data == null) return;
            StatsMap statsMap = arena.getStatsMapOfUUID(uuid);
            if (statsMap == null) return;
            int level = data.getLevelOfAttribute(type);

            if (level >= type.getAddition().size()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp tối đa điểm này!"));
                return;
            }

            PricePair pricePair = type.getPrice().get(level);
            if (pricePair == null) {
                player.sendMessage(ChatColor.RED + "Price configuration missing!");
                return;
            }

            if (Talents.getInstance().getAPI().getShopUtil().calculateMoney(player, pricePair.getA()) < pricePair.getB()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không có đủ " + ConversionUtils.getMaterialName(pricePair.getA()) + "&c!"));
                return;
            }

            Talents.getInstance().getAPI().getShopUtil().takeMoney(player, pricePair.getA(), pricePair.getB());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNâng cấp thành công!"));
            data.getAttributeShopData().put(type, level + 1);

            String statID = type.getTargetStat();
            double addAmount = type.getAddition().get(level);
            statsMap.getStatContainer(statID).add(addAmount);
        }

        public static int getLevelAttribute(UUID uuid, AttributeShopType type) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return 0;
            BedWars api = Talents.getInstance().getAPI();
            IArena iarena = api.getArenaUtil().getArenaByPlayer(player);
            if (iarena == null) return 0;
            Arena arena = Talents.getInstance().getArenaManager().getArenaFromIArena(iarena);
            if (arena == null) return 0;
            AttributesData data = arena.getAttributesDataOfUUID(uuid);
            if (data == null) return 0;
            StatsMap statsMap = arena.getStatsMapOfUUID(uuid);
            if (statsMap == null) return 0;
            return data.getLevelOfAttribute(type);
        }
    }
}
