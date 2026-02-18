package org.twightlight.talents.menus.skills;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.talents.Talents;
import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.runes.MenuHolder;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.ItemBuilder;

import java.util.*;

/**
 * Menu for selecting which skills to activate in the 4 skill slots.
 * Shows 4 "slot" items representing the player's active skill slots,
 * and a paginated list of all owned skills to pick from.
 */
public class SkillSelectMenu {

    private static Map<UUID, SkillSelectMenu> menus = new HashMap<>();

    // Slots in the inventory where the 4 active skill slots are displayed
    private static final int[] ACTIVE_SLOTS = {10, 12, 14, 16};

    // Slots for the list of available skills to select from
    private static final List<Integer> AVAILABLE_SLOTS = Arrays.asList(
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );

    private Inventory inv;

    private SkillSelectMenu(Player p) {
        menus.put(p.getUniqueId(), this);
        inv = Bukkit.createInventory(new MenuHolder(), 54, ChatColor.LIGHT_PURPLE + "Chọn kĩ năng");
    }

    private void clear() {
        inv.clear();
        ((MenuHolder) inv.getHolder()).clear();
    }

    private void loadItems(Player p, int selectingSlot, int page) {
        if (User.getUserFromBukkitPlayer(p) == null) return;

        User user = User.getUserFromBukkitPlayer(p);
        List<String> activating = user.getActivatingSkills();

        // ===== Top section: 4 active skill slots =====
        for (int i = 0; i < ACTIVE_SLOTS.length; i++) {
            int invSlot = ACTIVE_SLOTS[i];
            int skillSlotIndex = i;
            String currentSkillId = (i < activating.size()) ? activating.get(i) : "";

            Button slotButton = new Button((e) -> {
                if (e.isRightClick()) {
                    // Right-click to deselect
                    Talents.getInstance().getSkillsManager().deselectSkill(p, skillSlotIndex);
                    // Refresh user data
                    user.getActivatingSkills().set(skillSlotIndex, "");
                    clear();
                    loadItems(p, selectingSlot, page);
                    p.openInventory(getInventory());
                } else {
                    // Left-click to select this slot for assignment
                    clear();
                    loadItems(p, skillSlotIndex, page);
                    p.openInventory(getInventory());
                }
            }, (player) -> {
                String skillId = (skillSlotIndex < activating.size()) ? activating.get(skillSlotIndex) : "";

                ItemBuilder builder;
                if (skillId != null && !skillId.isEmpty() && Talents.getInstance().getSkillsManager().skillExist(skillId)) {
                    SkillItem si = Talents.getInstance().getSkillsManager().getSkillItemByID(skillId);
                    String name = si != null ? si.getDisplayName() : skillId;
                    int level = user.getSkillLevel(skillId);
                    builder = new ItemBuilder(XMaterial.ENCHANTED_BOOK);
                    builder.setName((selectingSlot == skillSlotIndex ? "&b&l> " : "") + "&eÔ " + (skillSlotIndex + 1) + ": &a" + name + " " + org.twightlight.talents.utils.Utility.toRoman(level));
                } else {
                    builder = new ItemBuilder(XMaterial.BOOK);
                    builder.setName((selectingSlot == skillSlotIndex ? "&b&l> " : "") + "&7Ô " + (skillSlotIndex + 1) + ": &8Trống");
                }

                List<String> lore = new ArrayList<>();
                lore.add("");
                if (selectingSlot == skillSlotIndex) {
                    lore.add("&b&l>> Đang chọn ô này <<");
                } else {
                    lore.add("&eChuột trái để chọn ô này");
                }
                lore.add("&eChuột phải để xóa kĩ năng");
                builder.setLore(lore);

                return builder.toItemStack();
            });
            setItem(p, invSlot, slotButton);
        }

        // ===== Separator line =====
        Button separator = new Button(null, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE);
            builder.setName(" ");
            return builder.toItemStack();
        });
        for (int s = 18; s <= 26; s++) {
            setItem(p, s, separator);
        }

        // ===== Bottom section: Available skills list (paginated) =====
        List<String> allSkills = new ArrayList<>(Talents.getInstance().getSkillsManager().getRegisteredSkills());
        // Sort for consistent ordering
        java.util.Collections.sort(allSkills);

        // Filter to only show skills the player has leveled (level > 0)
        // Or show all skills with their current level
        int slotsPerPage = AVAILABLE_SLOTS.size();
        int from = slotsPerPage * (page - 1);
        int total = allSkills.size();

        if (from < total) {
            List<String> pageSkills = new ArrayList<>(allSkills.subList(from, Math.min(from + slotsPerPage, total)));

            for (int i = 0; i < pageSkills.size(); i++) {
                String skillId = pageSkills.get(i);
                int invSlot = AVAILABLE_SLOTS.get(i);

                Button skillButton = new Button((e) -> {
                    if (selectingSlot < 0 || selectingSlot > 3) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cHãy chọn một ô kĩ năng ở trên trước!"));
                        return;
                    }

                    int level = user.getSkillLevel(skillId);
                    if (level <= 0) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBạn chưa mở khóa kĩ năng này!"));
                        return;
                    }

                    // Check if skill is already in another slot
                    if (org.twightlight.talents.utils.Utility.contains(activating, skillId)) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cKĩ năng này đã được chọn ở ô khác!"));
                        return;
                    }

                    Talents.getInstance().getSkillsManager().selectSkill(p, selectingSlot, skillId);
                    activating.set(selectingSlot, skillId);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aĐã chọn kĩ năng &e" + skillId + " &avào ô " + (selectingSlot + 1)));

                    // Refresh
                    clear();
                    loadItems(p, selectingSlot, page);
                    p.openInventory(getInventory());
                }, (player) -> {
                    int level = user.getSkillLevel(skillId);
                    SkillItem si = Talents.getInstance().getSkillsManager().getSkillItemByID(skillId);
                    String name = si != null ? si.getDisplayName() : skillId;

                    ItemBuilder builder;
                    if (level > 0) {
                        builder = new ItemBuilder(XMaterial.BLAZE_POWDER);
                        builder.setName("&a" + name + " " + org.twightlight.talents.utils.Utility.toRoman(level));
                    } else {
                        builder = new ItemBuilder(XMaterial.GUNPOWDER);
                        builder.setName("&c" + name + " &7(Chưa mở khóa)");
                    }

                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    if (org.twightlight.talents.utils.Utility.contains(activating, skillId)) {
                        lore.add("&a✔ Đã trang bị");
                    } else if (level > 0) {
                        lore.add("&eBấm để trang bị vào ô đang chọn");
                    } else {
                        lore.add("&cNâng cấp kĩ năng trước để sử dụng");
                    }
                    builder.setLore(lore);

                    return builder.toItemStack();
                });

                setItem(p, invSlot, skillButton);
            }
        }

        // Pagination for bottom section
        int totalPages = (int) Math.ceil((double) total / slotsPerPage);
        if (totalPages <= 0) totalPages = 1;

        if (page > 1) {
            int prevPage = page - 1;
            Button prev = new Button((e) -> {
                clear();
                loadItems(p, selectingSlot, prevPage);
                p.openInventory(getInventory());
            }, (player) -> {
                ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);
                builder.setName("&a<-- Trang trước");
                return builder.toItemStack();
            });
            setItem(p, 45, prev);
        }

        if (page < totalPages) {
            int nextPage = page + 1;
            Button next = new Button((e) -> {
                clear();
                loadItems(p, selectingSlot, nextPage);
                p.openInventory(getInventory());
            }, (player) -> {
                ItemBuilder builder = new ItemBuilder(XMaterial.ARROW);
                builder.setName("&aTrang sau -->");
                return builder.toItemStack();
            });
            setItem(p, 53, next);
        }

        // Back button
        Button backButton = new Button((e) -> {
            SkillMenu.open(p);
        }, (player) -> {
            ItemBuilder builder = new ItemBuilder(XMaterial.BARRIER);
            builder.setName("&cTrở về");
            return builder.toItemStack();
        });
        setItem(p, 49, backButton);
    }

    public void setItem(Player p, int i, Button button) {
        ((MenuHolder) inv.getHolder()).setButton(i, button);
        inv.setItem(i, button.getItemStackConsumer().accept(p));
    }

    public Inventory getInventory() {
        return inv;
    }

    public static void open(Player p) {
        open(p, 0, 1);
    }

    public static void open(Player p, int selectingSlot, int page) {
        if (!menus.containsKey(p.getUniqueId())) {
            new SkillSelectMenu(p);
        }
        SkillSelectMenu menu = menus.get(p.getUniqueId());
        menu.clear();
        menu.loadItems(p, selectingSlot, page);
        p.openInventory(menu.getInventory());
    }

    public static void remove(Player p) {
        menus.remove(p.getUniqueId());
    }
}

