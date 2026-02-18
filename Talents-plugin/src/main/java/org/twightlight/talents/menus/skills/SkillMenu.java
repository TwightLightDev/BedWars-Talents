package org.twightlight.talents.menus.skills;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.talents.Talents;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.runes.MenuHolder;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ItemBuilder;

import java.util.*;

/**
 * Paginated skill upgrade menu.
 * Each page is a 54-slot chest inventory.
 * Skill items are placed at their configured slot+page positions.
 * Navigation buttons (next/prev page, close, currency display, reset, select skills)
 * are placed in the bottom row.
 */
public class SkillMenu {

    private static Map<UUID, SkillMenu> menus = new HashMap<>();

    private Inventory inv;

    private SkillMenu(Player p) {
        menus.put(p.getUniqueId(), this);
        inv = Bukkit.createInventory(new MenuHolder(), 54, ChatColor.LIGHT_PURPLE + "Kĩ năng");
    }

    private void clear() {
        inv.clear();
        ((MenuHolder) inv.getHolder()).clear();
    }

    private void loadItems(Player p, int page) {
        if (User.getUserFromBukkitPlayer(p) == null) return;

        int maxPage = SkillsMenuRegistry.getMaxPage();

        // Load skill items for this page from the registry
        for (int slot = 0; slot < 45; slot++) {
            Button skillButton = SkillsMenuRegistry.getButton(page, slot);
            if (skillButton != null && skillButton.getExecutable() != null) {
                setItem(p, slot, skillButton);
            }
        }

        // ===== Bottom navigation bar (slots 45-53) =====

        // Slot 45: Previous page
        if (page > 1) {
            int prevPage = page - 1;
            Button prev = new Button((e) -> {
                clear();
                loadItems(p, prevPage);
                p.openInventory(getInventory());
            }, (player) -> {
                ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);
                builder.setName("&a<-- Trang trước");
                return builder.toItemStack();
            });
            setItem(p, 45, prev);
        }

        // Slot 47: Select skills menu
        Button selectSkillsButton = new Button((e) -> {
            SkillSelectMenu.open(p);
        }, (player) -> {
            User user = User.getUserFromBukkitPlayer(player);
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (user != null) {
                List<String> activating = user.getActivatingSkills();
                for (int i = 0; i < activating.size(); i++) {
                    String skillId = activating.get(i);
                    if (skillId != null && !skillId.isEmpty() && Talents.getInstance().getSkillsManager().skillExist(skillId)) {
                        SkillItem si = Talents.getInstance().getSkillsManager().getSkillItemByID(skillId);
                        String name = si != null ? si.getDisplayName() : skillId;
                        lore.add("&7Ô " + (i + 1) + ": &e" + name);
                    } else {
                        lore.add("&7Ô " + (i + 1) + ": &8Trống");
                    }
                }
            }
            lore.add("");
            lore.add("&eBấm để chọn kĩ năng kích hoạt!");

            ItemBuilder builder = new ItemBuilder(XMaterial.NETHER_STAR);
            builder.setName("&d&lChọn kĩ năng");
            builder.setLore(lore);
            return builder.toItemStack();
        });
        setItem(p, 47, selectSkillsButton);

        // Slot 49: Magical spirits display
        Button currencyButton = new Button(null, (player) -> {
            int ms = Talents.getInstance().getDb().getMagicalSpirits(player);
            ItemBuilder builder = new ItemBuilder(XMaterial.MAGMA_CREAM);
            builder.setName("&bLinh hồn ma thuật của bạn: &d" + ms);
            return builder.toItemStack();
        });
        setItem(p, 49, currencyButton);

        // Slot 50: Reset skills
        Button resetButton = new Button((e) -> {
            Player player = (Player) e.getWhoClicked();
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBạn chắc chứ. Nhập 'yes' để tiếp tục, 'no' để hủy!"));
            org.twightlight.talents.menus.ChatSessionService.createSession(player, (s) -> {
                if (s.equalsIgnoreCase("yes")) {
                    int refund = Talents.getInstance().getSkillsManager().resetSkills(player);
                    int currentMs = Talents.getInstance().getDb().getMagicalSpirits(player);
                    Talents.getInstance().getDb().setMagicalSpirits(player, currentMs + refund);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aĐã reset kĩ năng. Nhận lại &d" + refund + " &alinh hồn ma thuật!"));
                    SkillMenu.open(player, page);
                    org.twightlight.talents.menus.ChatSessionService.end(player);
                } else if (s.equalsIgnoreCase("no")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aĐã hủy yêu cầu!"));
                    SkillMenu.open(player, page);
                    org.twightlight.talents.menus.ChatSessionService.end(player);
                }
            });
        }, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.CLOCK);
            builder.setName("&cReset kĩ năng?");
            return builder.toItemStack();
        });
        setItem(p, 50, resetButton);

        // Slot 51: Close
        Button closeButton = new Button((e) -> {
            ((Player) e.getWhoClicked()).closeInventory();
        }, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.BARRIER);
            builder.setName("&cĐóng");
            return builder.toItemStack();
        });
        setItem(p, 51, closeButton);

        // Slot 53: Next page
        if (page < maxPage && SkillsMenuRegistry.hasPage(page + 1)) {
            int nextPage = page + 1;
            Button next = new Button((e) -> {
                clear();
                loadItems(p, nextPage);
                p.openInventory(getInventory());
            }, (player) -> {
                ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);
                builder.setName("&aTrang sau -->");
                return builder.toItemStack();
            });
            setItem(p, 53, next);
        }

        // Page indicator in slot 48
        int finalPage = page;
        Button pageIndicator = new Button(null, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.PAPER);
            builder.setName("&eTrang " + finalPage + "/" + maxPage);
            return builder.toItemStack();
        });
        setItem(p, 48, pageIndicator);
    }

    public void setItem(Player p, int i, Button button) {
        ((MenuHolder) inv.getHolder()).setButton(i, button);
        inv.setItem(i, button.getItemStackConsumer().accept(p));
    }

    public Inventory getInventory() {
        return inv;
    }

    public static void open(Player p, int page) {
        if (!menus.containsKey(p.getUniqueId())) {
            new SkillMenu(p);
        }
        SkillMenu menu = menus.get(p.getUniqueId());
        menu.clear();
        menu.loadItems(p, page);
        p.openInventory(menu.getInventory());
    }

    public static void open(Player p) {
        open(p, 1);
    }

    public static void remove(Player p) {
        menus.remove(p.getUniqueId());
    }
}

