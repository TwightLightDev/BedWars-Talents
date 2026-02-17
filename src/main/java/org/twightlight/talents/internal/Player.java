package org.twightlight.talents.internal;

import org.bukkit.ChatColor;
import org.twightlight.talents.Talents;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.PricePair;
import org.twightlight.talents.utils.Utility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Player {
    public static Map<org.bukkit.entity.Player, Player> playersInstance = new ConcurrentHashMap<>();

    private final org.bukkit.entity.Player player;
    private boolean isPlaying;
    private boolean vulnerable;

    private final List<Object> modifiers = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
    private final List<Integer> attributeTypes = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

    public Player(org.bukkit.entity.Player player) {
        playersInstance.put(player, this);
        this.player = player;
        isPlaying = true;
        vulnerable = true;
    }

    public void modifyAttribute(PlayerAttribute attribute, Object value) {
        if (!attribute.getTypeClass().isInstance(value)) {
            throw new IllegalArgumentException("Invalid type for index " + attribute.getIndex() + ". Expected " + attribute.getTypeClass().getSimpleName());
        }
        modifiers.set(attribute.getIndex(), value);
    }

    public <T> T getAttribute(PlayerAttribute attribute) {
        if (!attribute.getTypeClass().isInstance(modifiers.get(attribute.getIndex()))) {
            throw new IllegalArgumentException("Invalid type for index " + attribute.getIndex() + ". Expected " + attribute.getTypeClass().getSimpleName());
        }
        if (!isPlaying) {
            return (T) attribute.getTypeClass().cast(Utility.getDefaultValue(attribute.getTypeClass()));
        }
        @SuppressWarnings("unchecked")
        T result = (T) attribute.getTypeClass().cast(modifiers.get(attribute.getIndex()));
        return result;
    }

    public int getLevelAttribute(ShopAttributeType type) {
        return attributeTypes.get(type.getIndex());
    }

    public void upgradeAttribute(ShopAttributeType type) {
        int level = getLevelAttribute(type);
        if (level >= 3) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp tối đa điểm này!"));

            return;
        }
        PricePair pricePair = type.getPrice().get(level);
        if (Talents.getInstance().getApi().getShopUtil().calculateMoney(player, pricePair.getA()) < pricePair.getB()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không có đủ "+ ConversionUtil.getMaterialName(pricePair.getA()) + "&c!"));
            return;
        }
        Talents.getInstance().getApi().getShopUtil().takeMoney(player, pricePair.getA(), pricePair.getB());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNâng cấp thành công!"));
        attributeTypes.set(type.getIndex(), level + 1);
        Double addAmount = type.getAddition().get(level);
        Double currentAmount = getAttribute(type.getPlayerAttribute());
        modifyAttribute(type.getPlayerAttribute(), currentAmount + addAmount);
    }

    public void remove() {
        modifiers.clear();
        attributeTypes.clear();
        playersInstance.remove(this.player);
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean state) {
        isPlaying = state;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean state) {
        vulnerable = state;
    }

}
