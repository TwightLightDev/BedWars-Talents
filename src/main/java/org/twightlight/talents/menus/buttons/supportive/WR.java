package org.twightlight.talents.menus.buttons.supportive;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.twightlight.talents.Talents;
import org.twightlight.talents.database.SQLite;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.ChatSessionService;
import org.twightlight.talents.menus.PlayerMenu;
import org.twightlight.talents.menus.TalentsMenu;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class WR {
    private int x = 6;
    private int y = 16;
    private TalentsCategory category = TalentsCategory.Supportive;
    private String id = "WR";

    public WR() {
        Button button = new Button((e) -> {
            Player p = (Player) e.getWhoClicked();
            SQLite database = Talents.getInstance().getDatabase();
            int level = database.getTalentLevel(p, category, id);
            Talent<?> talent = Talents.getInstance().getTalentsManagerService().Talents.get(category.getColumn()).get(id);
            List<Integer> costlist = talent.getCostList();
            if (database.getTalentLevel(p, TalentsCategory.Miscellaneous, "BGA") < 10) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn cần nâng cấp các tài năng trước đó lên cấp cần thiết trước đã!"));
                return;
            }
            if (level >= costlist.size()) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã max điểm tài năng này!"));
                return;
            }
            if (e.isLeftClick()) {
                int soulstones = database.getSoulStones(p);
                if (soulstones >= Utility.totalCost(costlist, level, level)) {
                    database.setSoulStones(p, soulstones - Utility.totalCost(costlist, level, level));
                    database.upgradeTalents(1, talent, id, p);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công điểm tài năng này"));

                    if (PlayerMenu.getInstance(p) != null) {
                        PlayerMenu menu = PlayerMenu.getInstance(p);
                        menu.getHolder().open();
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&cBạn không đủ đá linh hồn!"));
                }
            } else if (e.isRightClick()) {
                p.closeInventory();
                PlayerMenu menu = PlayerMenu.getInstance(p);

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNhập một số nguyên tương ứng với số cấp độ bạn muốn nâng. Nhập 'cancel' để bỏ qua!"));
                ChatSessionService.createSession(p, (s) -> {
                    if (s.equals("cancel")) {
                        ChatSessionService.end(p);
                        p.openInventory(e.getClickedInventory());
                        return;
                    }
                    if (!Utility.isInteger(s)) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&cBạn phải nhập một số nguyên!"));
                        p.openInventory(e.getClickedInventory());
                    } else {
                        int increment = Integer.parseInt(s);
                        if (level + increment > costlist.size()) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&cBạn nhập số hơi lớn rồi đó! Tối đa là " + (costlist.size() - level)));
                            p.openInventory(e.getClickedInventory());
                        } else {
                            int totalCost = Utility.totalCost(costlist, level, level+increment-1);
                            if (database.getSoulStones(p) < totalCost) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&cBạn không đủ đá linh hồn!"));
                                p.openInventory(e.getClickedInventory());
                            } else {
                                database.setSoulStones(p, database.getSoulStones(p) - totalCost);

                                database.upgradeTalents(increment, talent, id, p);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn đã nâng cấp thành công điểm tài năng này"));

                                menu.getHolder().open();

                            }
                        }
                    }
                    ChatSessionService.end(p);
                });
            }

        }, (player) -> {
            int level = Talents.getInstance().getDatabase().getTalentLevel(player, category, id);
            List<String> lore = new ArrayList<>();
            lore.add("&7Khi kết liễu đối thủ, nhận được " + Utility.toDecimal(level * 0.05) + " máu tối đa,");
            lore.add("&7cộng dồn tối đa 5 lần.");
            List<Integer> costlist = Talents.getInstance().getTalentsManagerService().Talents.get(category.getColumn()).get(id).getCostList();
            boolean enchanted = false;

            boolean req = true;
            boolean first = true;

            if (Talents.getInstance().getDatabase().getTalentLevel(player, TalentsCategory.Miscellaneous, "BGA") < 10) {
                if (first) {
                    lore.add("");
                    lore.add("&cYêu cầu:");
                    first = false;
                }
                lore.add("&cTáo kim cương X");
                req = false;
            }


            if (req) {
                if (level >= costlist.size()) {
                    enchanted = true;
                    lore.add("");
                    lore.add("&aBạn đã nâng điểm tài năng này lên tối đa");
                } else {
                    lore.add("");
                    lore.add("&eBạn cần &d" + costlist.get(level) + " &eđá linh hồn để nâng cấp!");
                    lore.add("&bChuột phải để nâng cấp nhanh!");
                }
            } else {
                lore.add("");
                lore.add("&cBạn chưa đủ điều kiện để nâng cấp tài năng này!");
            }

            String roman = Utility.toRoman(level);
            if (!roman.isEmpty()) {
                roman = " " + roman;
            }
            return ConversionUtil.createItem(Material.WOOL, level, "",
                    ConversionUtil.getDataValue(DyeColor.RED), "&eChiến binh" + roman,
                    lore,
                    enchanted);
        });
        TalentsMenu.setItem(x, y, button);
    }
}
