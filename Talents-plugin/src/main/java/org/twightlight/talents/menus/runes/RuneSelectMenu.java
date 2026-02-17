package org.twightlight.talents.menus.runes;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.twightlight.talents.Talents;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ItemBuilder;

import java.util.*;

public class RuneSelectMenu {

    private static Map<UUID, RuneSelectMenu> menus = new HashMap<>();
    private static List<Integer> usableSlots = Arrays.asList(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);

    private Inventory inv;

    private RuneSelectMenu(Player p) {

        menus.put(p.getUniqueId(), this);

        inv = Bukkit.createInventory(new MenuHolder(), 54, ChatColor.YELLOW + "Hãy chọn cổ ngữ...");
    }

    private void clear() {
        inv.clear();
        ((MenuHolder) inv.getHolder()).clear();
    }

    private void loadItems(Player p, String category, String prefix, int slot, int page) {
        if (User.getUserFromBukkitPlayer(p) == null) return;

        List<String> runeList = Talents.getInstance().getRunesManager().getRuneByCategory(prefix, category);
        runeList = Talents.getInstance().getRunesManager().sort(runeList);

        int size = usableSlots.size();
        int from = size * (page-1);
        int total = runeList.size();

        Button back = new Button((e) -> {
            RuneMenu.open(p, category);

        }, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.BARRIER);

            builder.setName("&cTrở về");

            return builder.toItemStack();
        });

        setItem(p, 49, back);

        if (from >= total || from < 0) {
            return;
        }

        runeList = new ArrayList<>(runeList.subList(from, Math.min(from + size, total)));;

        for (int i = 0; i < runeList.size(); i++) {
            String runeID = runeList.get(i);
            int invSlot = usableSlots.get(i);


            Button button = new Button((e) -> {
                Player player = (Player) e.getWhoClicked();
                Talents.getInstance().getRunesManager().equip(player, category, runeID, prefix, slot);
                RuneMenu.open(p, category);
            }, (player) -> {

                RuneItem item = Talents.getInstance().getRunesManager().getRuneItemByID(runeID);

                ItemStack is = item.getBaseItemStack().clone();

                ItemBuilder ib = new ItemBuilder(is);

                List<String> lore = ib.getLore();
                if (lore == null) lore = new ArrayList<>();
                ListIterator<String> it = lore.listIterator();

                while (it.hasNext()) {
                    String s = it.next();

                    if (s.equals("{state}")) {
                        it.remove();

                        List<String> stateLore = getStateLore(p, runeID);
                        for (String s1 : stateLore) {
                            it.add(s1);
                        }

                        continue;
                    }

                    it.set(
                            s.replace("{color}", getColor(p, prefix, runeID))
                                    .replace("{amount}", "0")
                    );
                }

                ib.setLore(lore);

                is = ib.toItemStack();

                return is;

            });

            setItem(p, invSlot, button);
        }


        if (total > from + size) {
            Button next = new Button((e) -> {
                clear();
                loadItems(p, category, prefix, slot, page+1);
                p.openInventory(getInventory());
            }, (player) -> {
                ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);

                builder.setName("&aTrang sau -->");

                return builder.toItemStack();
            });

            setItem(p, 53, next);
        }



        if (page > 1) {
            Button prev = new Button((e) -> {
                clear();
                loadItems(p, category, prefix, slot, page-1);
                p.openInventory(getInventory());
            }, (player) -> {
                ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);

                builder.setName("&a <-- Trang trước");

                return builder.toItemStack();
            });

            setItem(p, 45, prev);
        }
    }

    public void setItem(Player p, int i, Button button) {
        ((MenuHolder) inv.getHolder()).setButton(i, button);
        inv.setItem(i, button.getItemStackConsumer().accept(p));
    }

    public Inventory getInventory() {
        return inv;
    }

    public static void open(Player p, String category, String prefix, int slot) {
        if (!menus.containsKey(p.getUniqueId())) {
            new RuneSelectMenu(p);
        }
        RuneSelectMenu menu = menus.get(p.getUniqueId());

        menu.clear();
        menu.loadItems(p, category, prefix, slot, 1);
        p.openInventory(menu.getInventory());
    }

    private List<String> getStateLore(Player p, String runeID) {
        int amount = Talents.getInstance().getRunesManager().getStorageAmount(p, runeID);
        List<String> res = new ArrayList<>();
        res.add("");
        res.add(" &7+ &aSở hữu: " + amount);
        if (amount == 0) {
            res.add("&cBạn không sở hữu cổ ngữ này");
        } else {
            res.add("&aNhấn để chọn cổ ngữ");
        }

        return res;
    }

    private String getColor(Player p, String prefix, String runeID) {
        RuneItem runeItem = Talents.getInstance().getRunesManager().getRuneItemByID(runeID);

        String required_rune = runeItem.getRequiredRune();

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, prefix, required_rune);

        return amount >= 3 ? "&a" : "&7";
    }
}
