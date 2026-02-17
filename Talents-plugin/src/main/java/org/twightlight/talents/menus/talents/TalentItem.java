package org.twightlight.talents.menus.talents;

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
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ItemBuilder;
import org.twightlight.talents.utils.Utility;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class TalentItem {

    private String talentID;

    private Map<String, Integer> requirements;

    private String displayName;

    private int x;
    private int y;

    private ItemStack base;
    private Button button;
    private Talent linkedTalent;
    private List<String> custom_values;

    private Function round;

    private static DecimalFormat df = new DecimalFormat("#.###");

    static {
        df.setRoundingMode(RoundingMode.HALF_UP);

    }

    public TalentItem(String id, YamlConfiguration wrapper, Talent linkedTalent) {
        talentID = id;
        requirements = new HashMap<>();
        displayName = wrapper.getString(id + ".display-name");
        x = wrapper.getInt(id + ".location.x", 0);
        y = wrapper.getInt(id + ".location.y", 0);
        custom_values = wrapper.getStringList(id + ".custom-values");
        if (wrapper.contains(id + ".requirements")) {
            for (String s : wrapper.getStringList(id + ".requirements")) {
                String[] parts = s.split(":", 2);
                if (parts.length < 2 || !Utility.isInteger(parts[1])) {
                    continue;
                }
                requirements.put(parts[0], Integer.parseInt(parts[1]));
            }
        }
        this.linkedTalent = linkedTalent;

        round = new Function("round", 1) {
            @Override
            public double apply(double... args) {
                return Math.round(args[0]);
            }
        };

        base = ItemBuilder.parse(wrapper.getConfigurationSection(id), "item").toItemStack();

        button = new Button((e) -> {
            Player p = (Player)e.getWhoClicked();
            User user = User.getUserFromBukkitPlayer(p);
            if (user == null) return;

            SQLite database = Talents.getInstance().getDb();
            int level = user.getTalentLevel(talentID);

            List<Integer> costlist = linkedTalent.getCostList();

            if (!checkRequirements(p)) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn cần nâng cấp các tài năng trước đó lên cấp cần thiết trước đã!"));
            } else if (level >= costlist.size()) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã max điểm tài năng này!"));
            } else {
                if (e.isLeftClick()) {
                    int soulstones = database.getSoulStones(p);
                    if (soulstones >= Utility.totalCost(costlist, level, level)) {
                        database.setSoulStones(p, soulstones - Utility.totalCost(costlist, level, level));
                        Talents.getInstance().getTalentsManager().upgradeTalent(1, talentID, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công điểm tài năng này"));
                        if (PlayerMenu.getInstance(p) != null) {
                            PlayerMenu menu = PlayerMenu.getInstance(p);
                            menu.getHolder().open();
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không đủ đá linh hồn!"));
                    }
                } else if (e.isRightClick()) {
                    p.closeInventory();
                    PlayerMenu menux = PlayerMenu.getInstance(p);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNhập một số nguyên tương ứng với số cấp độ bạn muốn nâng. Nhập 'cancel' để bỏ qua!"));
                    ChatSessionService.createSession(p, (s) -> {
                        if (s.equals("cancel")) {
                            ChatSessionService.end(p);
                            p.openInventory(e.getClickedInventory());
                        } else {
                            if (!Utility.isInteger(s)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn phải nhập một số nguyên!"));
                                p.openInventory(e.getClickedInventory());
                            } else {
                                int increment = Integer.parseInt(s);
                                if (level + increment > costlist.size()) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn nhập số hơi lớn rồi đó! Tối đa là " + (costlist.size() - level)));
                                    p.openInventory(e.getClickedInventory());
                                } else {
                                    int totalCost = Utility.totalCost(costlist, level, level + increment - 1);
                                    if (database.getSoulStones(p) < totalCost) {
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn không đủ đá linh hồn!"));
                                        p.openInventory(e.getClickedInventory());
                                    } else {
                                        database.setSoulStones(p, database.getSoulStones(p) - totalCost);
                                        Talents.getInstance().getTalentsManager().upgradeTalent(increment, talentID, p);
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công điểm tài năng này"));
                                        menux.getHolder().open();
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
            int level = user.getTalentLevel(this.talentID);
            ItemMeta im = base.getItemMeta().clone();
            List<String> lore = im.getLore();
            List<Integer> costlist = linkedTalent.getCostList();
            ListIterator<String> it = lore.listIterator();
            int soulstones = Talents.getInstance().getDb().getSoulStones(player);

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

                    if (checkRequirements(player)) {
                        if (level >= costlist.size()) {
                            stateLore.add("");
                            stateLore.add("&aBạn đã nâng điểm tài năng này lên tối đa!");
                        } else if (soulstones < Utility.totalCost(linkedTalent.getCostList(), level, level)) {
                            stateLore.add("");
                            stateLore.add("&cBạn không có đủ đá linh hồn để nâng cấp điểm tài năng này ");
                        } else {
                            stateLore.add("");
                            stateLore.add("&eBạn cần &d" + costlist.get(level) + " &eđá linh hồn để nâng cấp!");
                            stateLore.add("&bChuột phải để nâng cấp nhanh!");
                        }
                    } else {
                        stateLore.add("");
                        stateLore.add("&cBạn chưa mở khóa điểm tài năng này!");
                        stateLore.add("");
                        stateLore.add("&cYêu cầu:");
                        stateLore.addAll(getRemainingRequirements(player));
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
            name = name.replace("{color}", getColor(player)).replace("{displayName}", displayName).replace("{levelRoman}", (!roman.isEmpty() ? " " : "") + roman);
            im.setLore(lore);
            im.setDisplayName(name);

            ItemStack is = base.clone();
            is.setItemMeta(im);

            try {
                if (level >= costlist.size() && !is.getType().name().contains("ENCHANTED")) {
                    is.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                }
            } catch (RuntimeException ignore) {}


            if (level == 0) {
                is.setType(XMaterial.BEDROCK.parseMaterial());
            }

            is.setAmount(Math.max(1, level));
            return is;
        });
    }

    private boolean checkRequirements(Player player) {
        if (User.getUserFromBukkitPlayer(player) == null) return false;
        User user = User.getUserFromBukkitPlayer(player);

        for (String s : requirements.keySet()) {
            if (!Talents.getInstance().getTalentsManager().talentExist(s)) {
                requirements.remove(s);
                continue;
            }
            int lv = user.getTalentLevel(s);
            if (lv < requirements.get(s)) {
                return false;
            }
        }
        return true;
    }

    private List<String> getRemainingRequirements(Player player) {
        List<String> res = new ArrayList<>();
        if (User.getUserFromBukkitPlayer(player) == null) return res;
        User user = User.getUserFromBukkitPlayer(player);

        for (String s : requirements.keySet()) {
            int lv = user.getTalentLevel(s);
            if (lv < requirements.get(s) && Talents.getInstance().getTalentsManager().talentExist(s)) {
                TalentItem target = Talents.getInstance().getTalentsManager().getTalentItemByID(s);
                res.add(ChatColor.RED + target.getDisplayName() + " " + Utility.toRoman(requirements.get(s)));
            }
        }
        return res;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getTalentID() {
        return talentID;
    }

    public Map<String, Integer> getRequirements() {
        return requirements;
    }

    public String getDisplayName() {
        return displayName;
    }

    private String getColor(Player p) {
        if (User.getUserFromBukkitPlayer(p) == null) {
            return ChatColor.COLOR_CHAR + "c";
        }
        int soulstones = Talents.getInstance().getDb().getSoulStones(p);
        int level = User.getUserFromBukkitPlayer(p).getTalentLevel(getTalentID());
        if (!checkRequirements(p)) {
            return ChatColor.COLOR_CHAR + "c";
        }
        if (level >= linkedTalent.getCostList().size()) {
            return ChatColor.COLOR_CHAR + "e";
        }
        if (soulstones < Utility.totalCost(linkedTalent.getCostList(), level, level)) {
            return ChatColor.COLOR_CHAR + "c";
        } else {
            return ChatColor.COLOR_CHAR + "a";
        }
    }

    public Button getButton() {
        return button;
    }

    public Talent getLinkedTalent() {
        return linkedTalent;
    }
}
