package org.twightlight.talents.menus.runes;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ItemBuilder;

import java.util.*;

public class RuneMenu {

    private static Map<UUID, RuneMenu> menus = new HashMap<>();

    private Inventory inv;

    public RuneMenu(Player p) {
        menus.put(p.getUniqueId(), this);
        inv = Bukkit.createInventory(new MenuHolder(), 54, ChatColor.YELLOW + "Cổ ngữ");
    }

    private void clear() {
        inv.clear();
        ((MenuHolder) inv.getHolder()).clear();
    }

    private void loadItems(Player p, String category) {
        if (!RunesManager.categories.contains(category)) {
            return;
        }
        if (User.getUserFromBukkitPlayer(p) == null) return;

        User user = User.getUserFromBukkitPlayer(p);
        
        int index = RunesManager.categories.indexOf(category);

        List<String> names = Arrays.asList("Tấn công ", "&9Phòng thủ &c", "&eKĩ năng &c", "&5Linh tinh &c");

        int size = RunesManager.categories.size();
        Button next = new Button((e) -> {
            clear();
            loadItems(p, RunesManager.categories.get((index + 1)%size));
            p.openInventory(getInventory());
        }, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);

            builder.setName("&aChuyển trang -->");

            return builder.toItemStack();
        });

        setItem(p, 53, next);


        Button close = new Button((e) -> {
            p.closeInventory();

        }, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.BARRIER);

            builder.setName("&cĐóng");

            return builder.toItemStack();
        });

        setItem(p, 49, close);

        Button prev = new Button((e) -> {
            clear();
            loadItems(p, RunesManager.categories.get((index - 1 + size) % size));
            p.openInventory(getInventory());
        }, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);
            builder.setName("&a <-- Chuyển trang");
            return builder.toItemStack();
        });

        setItem(p, 45, prev);

        List<Integer> normalRunesSlots = Arrays.asList(10,13,16,20,24,39,41);
        List<String> normalRunes = user.getSelectingRunes(RunesManager.NORMAL_PREFIX).get(category);
        List<String> specialRunes = user.getSelectingRunes(RunesManager.SPECIAL_PREFIX).get(category);

        for (int i = 0; i < normalRunesSlots.size(); i++) {
            int invSlot = normalRunesSlots.get(i);

            int finalI = i;
            Button n_rune = new Button((e) -> {

                if (e.isRightClick()) {
                    Talents.getInstance().getRunesManager().equip(p, category, "", RunesManager.NORMAL_PREFIX, finalI);
                    open(p, category);
                } else {
                    RuneSelectMenu.open(p, category, RunesManager.NORMAL_PREFIX, finalI);
                }
            }, (player) -> {
                String runeID = normalRunes.get(finalI);

                if (Talents.getInstance().getRunesManager().runeExist(runeID)) {

                    RuneItem item = Talents.getInstance().getRunesManager().getRuneItemByID(runeID);
                    ItemStack is = item.getBaseItemStack().clone();
                    ItemBuilder ib = getItemBuilder(p, RunesManager.NORMAL_PREFIX, category, runeID, is);

                    is = ib.toItemStack();
                    return is;
                }

                ItemBuilder ib = new ItemBuilder(XMaterial.GLASS);
                ib.setName("&cÔ cổ ngữ " + names.get(index) + "số "+ (finalI+1) +" đang trống!");
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("&eChuột trái để chọn cổ ngữ!");
                lore.add("&eChuột phải để xóa ô!");

                ib.setLore(lore);
                return ib.toItemStack();
            });

            setItem(p, invSlot, n_rune);
        }

        Button s_rune = new Button((e) -> {
            if (e.isRightClick()) {
                Talents.getInstance().getRunesManager().equip(p, category, "", RunesManager.SPECIAL_PREFIX, 0);
                open(p, category);
            } else {
                RuneSelectMenu.open(p, category, RunesManager.SPECIAL_PREFIX, 0);
            }
        }, (player) -> {
            String runeID = specialRunes.get(0);
            
            if (Talents.getInstance().getRunesManager().runeExist(runeID)) {
                
                RuneItem item = Talents.getInstance().getRunesManager().getRuneItemByID(runeID);
                ItemStack is = item.getBaseItemStack().clone();
                ItemBuilder ib = getItemBuilder(p, RunesManager.NORMAL_PREFIX, category, runeID, is);

                is = ib.toItemStack();
                return is;
            }

            ItemBuilder ib = new ItemBuilder(XMaterial.GLASS);
            ib.setName("&cÔ cổ ngữ " + names.get(index) + "S đang trống!");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&eChuột trái để chọn cổ ngữ!");
            lore.add("&eChuột phải để xóa ô!");
            
            ib.setLore(lore);
            return ib.toItemStack();
        });

        setItem(p, 31, s_rune);

    }

    private ItemBuilder getItemBuilder(Player p, String prefix, String category, String runeID, ItemStack is) {
        ItemBuilder ib = new ItemBuilder(is);

        List<String> lore = ib.getLore();
        if (lore == null) lore = new ArrayList<>();
        ListIterator<String> it = lore.listIterator();

        while (it.hasNext()) {
            String s = it.next();
            if (s.equals("{state}")) {
                it.remove();

                List<String> stateLore = new ArrayList<>();
                stateLore.add("");
                stateLore.add("&eChuột trái để thay đổi cổ ngữ!");
                stateLore.add("&eChuột phải để xóa ô!");

                for (String s1 : stateLore) {
                    it.add(s1);
                }
                continue;
            }

            it.set(s.replace("{color}", getColor(p, prefix, category, runeID))
                    .replace("{amount}", getAmount(p, prefix, category, runeID) + ""));

        }

        ib.setLore(lore);
        return ib;
    }

    public Inventory getInventory() {
        return inv;
    }

    public void setItem(Player p, int i, Button button) {
        ((MenuHolder) inv.getHolder()).setButton(i, button);
        inv.setItem(i, button.getItemStackConsumer().accept(p));
    }

    public static void open(Player p, String category) {
        if (!RunesManager.categories.contains(category)) {
            return;
        }

        if (!menus.containsKey(p.getUniqueId())) {
            new RuneMenu(p);

        }
        RuneMenu menu = menus.get(p.getUniqueId());

        menu.clear();
        menu.loadItems(p, category);
        p.openInventory(menu.getInventory());
    }

    private String getColor(Player p, String prefix, String category, String runeID) {
        RuneItem runeItem = Talents.getInstance().getRunesManager().getRuneItemByID(runeID);

        String required_rune = runeItem.getRequiredRune();

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, prefix, category, required_rune);

        return amount >= 3 ? "&a" : "&7";
    }

    private int getAmount(Player p, String prefix, String category, String runeID) {
        RuneItem runeItem = Talents.getInstance().getRunesManager().getRuneItemByID(runeID);

        String required_rune = runeItem.getRequiredRune();

        return Math.min(3, Talents.getInstance().getRunesManager().getRuneAmount(p, prefix, category, required_rune));
    }
}
