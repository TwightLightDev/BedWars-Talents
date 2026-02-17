package org.twightlight.talents.menus.runes;

import com.cryptomorin.xseries.XMaterial;
import me.clip.placeholderapi.PlaceholderAPI;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.twightlight.pvpmanager.utils.Rounding;
import org.twightlight.talents.Talents;
import org.twightlight.talents.database.SQLite;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.ChatSessionService;
import org.twightlight.talents.runes.Rune;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ItemBuilder;
import org.twightlight.talents.utils.Utility;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class RuneItem {

    private String runeID;

    private String displayName;

    private String requiredRune = "";

    private ItemStack base;
    private Rune linkedRune;

    public RuneItem(String id, YamlConfiguration wrapper, Rune linkedRune) {
        this.runeID = id;
        displayName = wrapper.getString(id + ".display-name");
        this.linkedRune = linkedRune;
        if (wrapper.contains(id + ".required-rune")) {
            requiredRune = wrapper.getString(id + ".required-rune");
        }
        base = ItemBuilder.parse(wrapper.getConfigurationSection(id), "item").toItemStack();
    }

    public String getDisplayName() {
        return displayName;
    }

    public Rune getLinkedRune() {
        return linkedRune;
    }

    public ItemStack getBaseItemStack() {
        return base;
    }

    public String getRuneID() {
        return runeID;
    }

    public String getRequiredRune() {
        return requiredRune;
    }
}
