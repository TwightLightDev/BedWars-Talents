package org.twightlight.talents.menus.skills;

import org.twightlight.talents.menus.Button;
import org.twightlight.talents.menus.Collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static registry that holds all skill buttons organized by page.
 * Each page maps slot numbers (0-53) to Buttons.
 * SkillItems register themselves here during loading.
 */
public class SkillsMenuRegistry {

    // page -> (slot -> Button)
    private static Map<Integer, Map<Integer, Button>> pages = new HashMap<>();

    // page -> list of SkillItem for reference
    private static Map<Integer, List<SkillItem>> skillItemsByPage = new HashMap<>();

    public static void setItem(int page, int slot, Button button) {
        pages.computeIfAbsent(page, k -> new HashMap<>());
        pages.get(page).put(slot, button);
    }

    public static void registerSkillItem(int page, SkillItem item) {
        skillItemsByPage.computeIfAbsent(page, k -> new ArrayList<>());
        skillItemsByPage.get(page).add(item);
    }

    public static Button getButton(int page, int slot) {
        Map<Integer, Button> pageMap = pages.get(page);
        if (pageMap == null) {
            return Collections.getEmptyButton();
        }
        return pageMap.getOrDefault(slot, Collections.getEmptyButton());
    }

    public static boolean hasPage(int page) {
        return pages.containsKey(page) && !pages.get(page).isEmpty();
    }

    public static int getMaxPage() {
        return pages.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
    }

    public static void clear() {
        pages.clear();
        skillItemsByPage.clear();
    }
}

