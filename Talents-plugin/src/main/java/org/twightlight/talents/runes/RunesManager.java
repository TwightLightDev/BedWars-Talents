package org.twightlight.talents.runes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.menus.runes.RuneItem;
import org.twightlight.talents.runes.categories.defense.tier_S.Dodge;
import org.twightlight.talents.runes.categories.defense.tier_S.Endurance;
import org.twightlight.talents.runes.categories.defense.tier_S.Strength;
import org.twightlight.talents.runes.categories.defense.tier_S.Weakness;
import org.twightlight.talents.runes.categories.offense.tier_S.*;
import org.twightlight.talents.runes.listeners.Curser;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Debug;
import org.twightlight.talents.utils.Utility;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class RunesManager {

    public Map<String, Rune> Runes;
    public Map<String, RuneItem> RuneItems;

    public static String NORMAL_PREFIX = "n";
    public static String SPECIAL_PREFIX = "s";

    public static final List<String> categories = Arrays.asList("offense", "defense", "miscellaneous", "skill");

    private EventDispatcher dispatcher = new EventDispatcher();

    public void register(Rune rune) {
        this.register(rune, false);
    }

    public void register(Rune rune, boolean override) {

        String id = rune.getClass().getSimpleName();

        File file = new File("plugins/Talents/runes/" + id + ".yml");
        if (!file.exists()) {
            String path = "runes/" + id + ".yml";

            InputStream in = org.twightlight.talents.Talents.getInstance().getResource(path);
            if (in == null) {
                Debug.debugMsg("File " + id + ".yml not found in jar, cancel the registration!");
                return;
            }

            org.twightlight.talents.Talents.getInstance().saveResource(path, false);
            Debug.debugMsg("File " + id + ".yml not found, load default file!");
            file = new File("plugins/Talents/runes/" + id + ".yml");
        }

        if (override) {
            this.Runes.put(id, rune);
            return;
        }

        RuneItem runeItem = new RuneItem(id, YamlConfiguration.loadConfiguration(file), rune);
        this.RuneItems.putIfAbsent(id, runeItem);
        this.Runes.putIfAbsent(id, rune);
        Debug.debugMsg("Registered rune with id: " + id);
    }

    public Rune getRuneByID(String id) {
        return Runes.getOrDefault(id, null);
    }

    public RuneItem getRuneItemByID(String id) {
        return RuneItems.getOrDefault(id, null);
    }

    public boolean runeExist(String id) {
        return Runes.containsKey(id);
    }

    public RunesManager() {
        Runes = new HashMap<>();
        RuneItems = new HashMap<>();
        Bukkit.getScheduler().runTaskLater(org.twightlight.talents.Talents.getInstance(), () -> {
            registerRunes();

            registerListeners();
            Runes.values().forEach((rune -> {
                dispatcher.register(rune);
            }));

            Debug.debugMsg(ChatColor.GREEN + "Successfully registered " + Runes.size() + " runes!");
        }, 40L);
    }

    public void registerListeners() {
        dispatcher.register(new Curser());
    }

    @SuppressWarnings({"unchecked"})
    public void registerRunes() {
        List<String> offense = Arrays.asList("Sword", "Crit_Rate", "Crit_Dmg", "Bow", "Damage", "Enchantment_Breaker", "Fire", "Ice", "LifeSteal", "Penetration");
        List<String> defense = Arrays.asList("Armor", "Heal", "Health", "Regeneration", "Tenacity", "Thorns", "Protect", "Curse");
        List<String> miscellaneous = Arrays.asList();
        List<String> skill = Arrays.asList();

        List<String> tiers = Arrays.asList("1", "2", "3", "4", "5");

        for (String rune : offense) {
            for (String tier : tiers) {
                try {
                    Class<? extends Rune> clazz = (Class<? extends Rune>) Class.forName("org.twightlight.talents.runes.categories.offense.tier_" + tier + "." + rune + "_" + Utility.toRoman(Integer.parseInt(tier)));
                    Constructor<? extends Rune> constructor = clazz.getConstructor(int.class);
                    Rune runeInstance = constructor.newInstance(Integer.parseInt(tier));
                    register(runeInstance);
                } catch (NoSuchMethodException | InvocationTargetException |
                         InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException ignored) {}
            }
        }
        for (String rune : defense) {
            for (String tier : tiers) {
                try {
                    Class<? extends Rune> clazz = (Class<? extends Rune>) Class.forName("org.twightlight.talents.runes.categories.defense.tier_" + tier + "." + rune + "_" + Utility.toRoman(Integer.parseInt(tier)));
                    Constructor<? extends Rune> constructor = clazz.getConstructor(int.class);
                    Rune runeInstance = constructor.newInstance(Integer.parseInt(tier));
                    register(runeInstance);
                } catch (NoSuchMethodException | InvocationTargetException |
                         InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException ignored) {}
            }
        }
        for (String rune : miscellaneous) {
            for (String tier : tiers) {
                try {
                    Class<? extends Rune> clazz = (Class<? extends Rune>) Class.forName("org.twightlight.talents.runes.categories.miscellaneous.tier_" + tier + "." + rune + "_" + Utility.toRoman(Integer.parseInt(tier)));
                    Constructor<? extends Rune> constructor = clazz.getConstructor(int.class);
                    Rune runeInstance = constructor.newInstance(Integer.parseInt(tier));
                    register(runeInstance);
                } catch (NoSuchMethodException | InvocationTargetException |
                         InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException ignored) {}
            }
        }
        for (String rune : skill) {
            for (String tier : tiers) {
                try {
                    Class<? extends Rune> clazz = (Class<? extends Rune>) Class.forName("org.twightlight.talents.runes.categories.skill.tier_" + tier + "." + rune + "_" + Utility.toRoman(Integer.parseInt(tier)));
                    Constructor<? extends Rune> constructor = clazz.getConstructor(int.class);
                    Rune runeInstance = constructor.newInstance(Integer.parseInt(tier));
                    register(runeInstance);
                } catch (NoSuchMethodException | InvocationTargetException |
                         InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException ignored) {}
            }
        }

        register(new Blow());
        register(new Electrifying());
        register(new LifeDrain());
        register(new Strike());
        register(new VoidShot());

        register(new Dodge());
        register(new Endurance());
        register(new Strength());
        register(new Weakness());

    }

    public Set<String> getRegisteredRunes() {
        return Runes.keySet();
    }

    public List<String> getCategories() {
        return categories;
    }

    public int getRuneAmount(Player p, String prefix, String id) {
        User user = User.getUserFromBukkitPlayer(p);
        if (user == null) return 0;
        if (!runeExist(id)) return 0;
        Rune rune = getRuneByID(id);
        List<String> selecting = user.getSelectingRunes(prefix).get(rune.getCategory());
        return Math.toIntExact(selecting.stream()
                .filter(item -> item.contains(id))
                .count());

    }

    public int getRuneAmount(Player p, String prefix, String category, String id) {
        User user = User.getUserFromBukkitPlayer(p);
        if (user == null) return 0;
        List<String> selecting = user.getSelectingRunes(prefix).get(category);
        return Math.toIntExact(selecting.stream()
                .filter(item -> item.contains(id))
                .count());

    }

    public List<String> getRuneByCategory(String prefix, String cat) {
        return Runes.entrySet().stream()
                .filter(e -> cat.equals(e.getValue().category))
                .filter(e -> {
                    int tier = e.getValue().getTier();
                    return SPECIAL_PREFIX.equals(prefix)
                            ? tier == 64
                            : NORMAL_PREFIX.equals(prefix) && tier != 64;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    public List<String> sort(List<String> input) {
        Comparator<String> comparator = Comparator
                .comparingInt((String s) -> {
                    if (!runeExist(s)) return 0;
                    return getRuneByID(s).getTier();
                })
                .reversed()
                .thenComparing(s -> {
                    if (!runeExist(s)) return "";
                    return getRuneByID(s).getRuneId();
                });

        input.sort(comparator);
        return input;
    }

    public void equip(Player p, String category, String runeID, String prefix, int slot) {

        if (User.getUserFromBukkitPlayer(p) == null) return;
        User user = User.getUserFromBukkitPlayer(p);

        Map<String, List<String>> map = user.getSelectingRunes(prefix);

        if (!map.containsKey(category)) return;

        List<String> equipmentData = map.get(category);
        String oldRune = equipmentData.get(slot);

        if (runeID.isEmpty()) {
            if (runeExist(oldRune)) {
                addToStorage(p, oldRune, 1);
            }

            equipmentData.set(slot, runeID);

            Talents.getInstance().getDb().update(p, map, "runes", prefix + "_selecting");

            p.sendMessage(ChatColor.GREEN + "Successfully cleared your rune slot!");
            return;
        }

        if (!runeExist(oldRune)) {
            if (getStorageAmount(p, runeID) < 1) return;

            addToStorage(p, runeID, -1);
            equipmentData.set(slot, runeID);

            Talents.getInstance().getDb().update(p, map, "runes", prefix + "_selecting");

            p.sendMessage(ChatColor.GREEN + "Successfully set your " + category + " rune slot " + (slot + 1) + " to " + runeID);
            return;
        }

        if (!runeExist(runeID) || getStorageAmount(p, runeID) < 1) return;

        addToStorage(p, oldRune, 1);
        addToStorage(p, runeID, -1);

        equipmentData.set(slot, runeID);

        Talents.getInstance().getDb().update(p, map, "runes", prefix + "_selecting");

        p.sendMessage(ChatColor.GREEN + "Successfully set your " + category + " rune slot " + (slot + 1) + " to " + runeID);
    }


    public void addToStorage(Player p, String runeID, int amount) {
        if (!runeExist(runeID)) {
            p.sendMessage(ChatColor.RED + "Rune cannot be found!");
            return;
        }

        if (User.getUserFromBukkitPlayer(p) == null) return;
        User user = User.getUserFromBukkitPlayer(p);

        Map<String, Integer> storage = user.getRunesData();

        storage.compute(runeID, (k, current) -> Math.max(0, current + amount));

        Talents.getInstance().getDb().update(p, storage, "runes", "storage");
    }

    public int getStorageAmount(Player p, String runeID) {
        if (User.getUserFromBukkitPlayer(p) == null) return 0;

        User user = User.getUserFromBukkitPlayer(p);

        if (!user.getRunesData().containsKey(runeID)) return 0;
        return user.getRunesData().get(runeID);
    }

    public EventDispatcher getDispatcher() {
        return dispatcher;
    }
}
