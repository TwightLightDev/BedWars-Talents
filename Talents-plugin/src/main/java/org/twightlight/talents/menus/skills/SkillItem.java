package org.twightlight.talents.menus.skills;

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
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ItemBuilder;
import org.twightlight.talents.utils.Utility;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class SkillItem {

    private String skillID;

    private String displayName;

    private int slot;
    private int page;

    private ItemStack base;
    private Button button;
    private Skill linkedSkill;
    private List<String> custom_values;

    private Function round;

    private static DecimalFormat df = new DecimalFormat("#.###");

    static {
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public SkillItem(String id, YamlConfiguration wrapper, Skill linkedSkill) {
        skillID = id;
        displayName = wrapper.getString(id + ".display-name");
        slot = wrapper.getInt(id + ".location.slot", 0);
        page = wrapper.getInt(id + ".location.page", 1);
        custom_values = wrapper.getStringList(id + ".custom-values");
        this.linkedSkill = linkedSkill;

        round = new Function("round", 1) {
            @Override
            public double apply(double... args) {
                return Math.round(args[0]);
            }
        };

        base = ItemBuilder.parse(wrapper.getConfigurationSection(id), "item").toItemStack();

        button = new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            User user = User.getUserFromBukkitPlayer(p);
            if (user == null) return;

            SQLite database = Talents.getInstance().getDb();
            int level = user.getSkillLevel(skillID);

            List<Integer> costlist = linkedSkill.getCostList();

            if (level >= costlist.size()) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã max kĩ năng này!"));
            } else {
                if (e.isLeftClick()) {
                    int magicalSpirits = database.getMagicalSpirits(p);
                    if (magicalSpirits >= Utility.totalCost(costlist, level, level)) {
                        database.setMagicalSpirits(p, magicalSpirits - Utility.totalCost(costlist, level, level));
                        Talents.getInstance().getSkillsManager().upgradeSkill(1, skillID, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công kĩ năng này"));
                        SkillMenu.open(p, page);
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không đủ linh hồn ma thuật!"));
                    }
                } else if (e.isRightClick()) {
                    p.closeInventory();
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNhập một số nguyên tương ứng với số cấp độ bạn muốn nâng. Nhập 'cancel' để bỏ qua!"));
                    ChatSessionService.createSession(p, (s) -> {
                        if (s.equals("cancel")) {
                            ChatSessionService.end(p);
                            SkillMenu.open(p, page);
                        } else {
                            if (!Utility.isInteger(s)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn phải nhập một số nguyên!"));
                                SkillMenu.open(p, page);
                            } else {
                                int increment = Integer.parseInt(s);
                                if (level + increment > costlist.size()) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn nhập số hơi lớn rồi đó! Tối đa là " + (costlist.size() - level)));
                                    SkillMenu.open(p, page);
                                } else {
                                    int totalCost = Utility.totalCost(costlist, level, level + increment - 1);
                                    if (database.getMagicalSpirits(p) < totalCost) {
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không đủ linh hồn ma thuật!"));
                                        SkillMenu.open(p, page);
                                    } else {
                                        database.setMagicalSpirits(p, database.getMagicalSpirits(p) - totalCost);
                                        Talents.getInstance().getSkillsManager().upgradeSkill(increment, skillID, p);
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công kĩ năng này"));
                                        SkillMenu.open(p, page);
                                    }
                                }
                            }
                            ChatSessionService.end(p);
                        }
                    });
                }
            }
        }, (player) -> {

            User user = User.getUserFromBukkitPlayer(player);
            if (user == null) return base.clone();
            int level = user.getSkillLevel(this.skillID);
            ItemMeta im = base.getItemMeta().clone();
            List<String> lore = im.getLore();
            List<Integer> costlist = linkedSkill.getCostList();
            ListIterator<String> it = lore.listIterator();
            int magicalSpirits = Talents.getInstance().getDb().getMagicalSpirits(player);

            while (it.hasNext()) {
                String s = it.next();

                s = s.replace("{level}", String.valueOf(level));
                for (int i = 0; i < custom_values.size(); i++) {
                    Expression builder = new ExpressionBuilder(custom_values.get(i)).function(round).variable("level").build();
                    builder.setVariable("level", level == 0 ? level + 1 : level);
                    s = s.replace("{value_" + i + "}", df.format(Rounding.roundDecimal(builder.evaluate(), 3)));
                }
                s = PlaceholderAPI.setPlaceholders(player, s);

                if (s.equals("{state}")) {
                    it.remove();

                    List<String> stateLore = new ArrayList<>();

                    if (level >= costlist.size()) {
                        stateLore.add("");
                        stateLore.add("&aBạn đã nâng kĩ năng này lên tối đa!");
                    } else if (magicalSpirits < Utility.totalCost(linkedSkill.getCostList(), level, level)) {
                        stateLore.add("");
                        stateLore.add("&cBạn không có đủ linh hồn ma thuật để nâng cấp kĩ năng này");
                    } else {
                        stateLore.add("");
                        stateLore.add("&eBạn cần &d" + costlist.get(level) + " &elinh hồn ma thuật để nâng cấp!");
                        stateLore.add("&bChuột phải để nâng cấp nhanh!");
                    }

                    for (String line : stateLore) {
                        it.add(ChatColor.translateAlternateColorCodes('&', line));
                    }

                    continue;
                }

                it.set(ChatColor.translateAlternateColorCodes('&', s));
            }

            String name = im.getDisplayName();
            String roman = Utility.toRoman(level);
            name = name.replace("{color}", getColor(player))
                    .replace("{displayName}", displayName)
                    .replace("{levelRoman}", (!roman.isEmpty() ? " " : "") + roman);
            im.setLore(lore);
            im.setDisplayName(name);

            ItemStack is = base.clone();
            is.setItemMeta(im);

            try {
                if (level >= costlist.size() && !is.getType().name().contains("ENCHANTED")) {
                    is.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                }
            } catch (RuntimeException ignore) {
            }

            if (level == 0) {
                is.setType(XMaterial.BEDROCK.parseMaterial());
            }

            is.setAmount(Math.max(1, level));
            return is;
        });
    }

    public int getSlot() {
        return slot;
    }

    public int getPage() {
        return page;
    }

    public String getSkillID() {
        return skillID;
    }

    public String getDisplayName() {
        return displayName;
    }

    private String getColor(Player p) {
        if (User.getUserFromBukkitPlayer(p) == null) {
            return ChatColor.COLOR_CHAR + "c";
        }
        int magicalSpirits = Talents.getInstance().getDb().getMagicalSpirits(p);
        int level = User.getUserFromBukkitPlayer(p).getSkillLevel(getSkillID());
        if (level >= linkedSkill.getCostList().size()) {
            return ChatColor.COLOR_CHAR + "e";
        }
        if (magicalSpirits < Utility.totalCost(linkedSkill.getCostList(), level, level)) {
            return ChatColor.COLOR_CHAR + "c";
        } else {
            return ChatColor.COLOR_CHAR + "a";
        }
    }

    public Button getButton() {
        return button;
    }

    public Skill getLinkedSkill() {
        return linkedSkill;
    }
}
