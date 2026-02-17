package org.twightlight.talents.talents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.menus.talents.TalentItem;
import org.twightlight.talents.menus.talents.TalentsMenu;
import org.twightlight.talents.talents.built_in_talents.defense.skills.*;
import org.twightlight.talents.talents.built_in_talents.defense.stats.*;
import org.twightlight.talents.talents.built_in_talents.miscellaneous.*;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.*;
import org.twightlight.talents.talents.built_in_talents.offense.skills.ranged.*;
import org.twightlight.talents.talents.built_in_talents.offense.stats.*;
import org.twightlight.talents.talents.built_in_talents.offense.stats.melee.*;
import org.twightlight.talents.talents.built_in_talents.offense.stats.ranged.CharmingProjectiles;
import org.twightlight.talents.talents.built_in_talents.offense.stats.ranged.DiamondProjectiles;
import org.twightlight.talents.talents.built_in_talents.offense.stats.ranged.Momentum;
import org.twightlight.talents.talents.built_in_talents.offense.stats.ranged.PreciseAiming;
import org.twightlight.talents.talents.built_in_talents.special.KingOfBedWars;
import org.twightlight.talents.talents.built_in_talents.supportive.*;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Debug;
import org.twightlight.talents.utils.Utility;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class TalentsManager {

    public Map<String, Talent> Talents;
    public Map<String, TalentItem> TalentItems;

    private List<Integer> normalCost = Arrays.asList(50, 50, 50, 50, 50, 80, 80, 80, 80, 80, 120, 120, 120, 120, 120, 150, 150, 150, 150, 150);
    private List<Integer> specialCost = Arrays.asList(75, 75, 75, 75, 75, 120, 120, 120, 120, 120, 180, 180, 180, 180, 180, 225, 225, 225, 225, 225);
    private List<Integer> highestCost = Arrays.asList(125, 125, 125, 125, 125, 200, 200, 200, 200, 200, 300, 300, 300, 300, 300, 375, 375, 375, 375, 375);
    private EventDispatcher dispatcher = new EventDispatcher();

    public void register(String id, Talent talent) {
        this.register(id, talent, false);
    }

    public void register(String id, Talent talent, boolean override) {
        File file = new File("plugins/Talents/talents/" + id + ".yml");
        if (!file.exists()) {
            String path = "talents/" + id + ".yml";

            InputStream in = org.twightlight.talents.Talents.getInstance().getResource(path);
            if (in == null) {
                Debug.debugMsg("File " + id + ".yml not found in jar, cancel the registration!");
                return;
            }

            org.twightlight.talents.Talents.getInstance().saveResource(path, false);
            Debug.debugMsg("File " + id + ".yml not found, load default file!");
            file = new File("plugins/Talents/talents/" + id + ".yml");
        }
        if (override) {
            this.Talents.put(id, talent);
            return;
        }
        this.Talents.putIfAbsent(id, talent);
        TalentItem talentItem = new TalentItem(id, YamlConfiguration.loadConfiguration(file), talent);
        TalentItems.putIfAbsent(id, talentItem);
        TalentsMenu.setItem(talentItem.getX(), talentItem.getY(), talentItem.getButton());
        Debug.debugMsg("Registered talent with id: " + id);

    }

    public Talent getTalentByID(String id) {
        return Talents.getOrDefault(id, null);
    }

    public TalentItem getTalentItemByID(String id) {
        return TalentItems.getOrDefault(id, null);
    }

    public boolean talentExist(String id) {
        return Talents.containsKey(id);
    }

    public TalentsManager() {
        Talents = new HashMap<>();
        TalentItems = new HashMap<>();

        Bukkit.getScheduler().runTaskLater(org.twightlight.talents.Talents.getInstance(), () -> {
            registerTalents();

            Talents.values().forEach((talent -> {
                dispatcher.register(talent);
            }));

            Debug.debugMsg(ChatColor.GREEN + "Successfully registered " + Talents.size() + " talents!");
        }, 40L);
    }



    private void registerTalents() {
        //Offense
        register("Improvement", new Improvement("Improvement", normalCost));
        register("CurseMaster", new CurseMaster("CurseMaster", normalCost));
        register("LifeSteal", new LifeSteal("LifeSteal", specialCost));
        register("PhysicalEnhancement", new PhysicalEnhancement("PhysicalEnhancement", normalCost));
        register("StrongBoom", new StrongBoom("StrongBoom", specialCost));

        //melee
        register("AttackWeaknesses", new AttackWeaknesses("AttackWeaknesses", normalCost));
        register("BloodyBlade", new BloodyBlade("BloodyBlade", normalCost));
        register("EnchantedSoul", new EnchantedSoul("EnchantedSoul", normalCost));
        register("Sharpness", new Sharpness("Sharpness", normalCost));
        register("Thump", new Thump("Thump", normalCost));
        register("Blazing", new Blazing("Blazing", normalCost));
        register("Breezing", new Breezing("Breezing", normalCost));
        register("KingOfBedWars", new KingOfBedWars("KingOfBedWars", highestCost));
        register("MysticalStand", new MysticalStand("MysticalStand", specialCost));
        register("Thunder", new Thunder("Thunder", specialCost));
        register("SoulStealing", new SoulStealing("SoulStealing", normalCost));

        //ranged
        register("CharmingProjectiles", new CharmingProjectiles("CharmingProjectiles", normalCost));
        register("DiamondProjectiles", new DiamondProjectiles("DiamondProjectiles", normalCost));
        register("Momentum", new Momentum("Momentum", normalCost));
        register("PreciseAiming", new PreciseAiming("PreciseAiming", normalCost));
        register("BlackMagic", new BlackMagic("BlackMagic", normalCost));
        register("Blow", new Blow("Blow", normalCost));
        register("Flame", new Flame("Flame", normalCost));
        register("Frozen", new Frozen("Frozen", normalCost));
        register("Scouter", new Scouter("Scouter", normalCost));
        register("Gemini", new Gemini("Gemini", specialCost));
        register("Summoner", new Summoner("Summoner", specialCost));


        //Defense
        register("Endurance", new Endurance("Endurance", normalCost));
        register("Absorption", new Absorption("Absorption", normalCost));
        register("BandAid", new BandAid("BandAid", specialCost));
        register("Heal", new Heal("Heal", normalCost));
        register("Parry", new Parry("Parry", normalCost));
        register("Tenacity", new Tenacity("Tenacity", normalCost));
        register("Feather", new Feather("Feather", normalCost));

        register("Thorns", new Thorns("Thorns", specialCost));
        register("Toughness", new Toughness("Toughness", specialCost));

        register("GoldenBlessing", new GoldenBlessing("GoldenBlessing", normalCost));

        register("Block", new Block("Block", specialCost));
        register("Giant", new Giant("Giant", specialCost));
        register("Reflection", new Reflection("Reflection", specialCost));
        register("SurvivalInstinct", new SurvivalInstinct("SurvivalInstinct", specialCost));
        register("Armor", new Armor("Armor", normalCost));

        //Supportive
        register("Shura", new Shura("Shura", specialCost));
        register("Ares", new Ares("Ares", specialCost));

        register("Assassin", new Assassin("Assassin", normalCost));
        register("BountyHunter", new BountyHunter("BountyHunter", normalCost));
        register("Shield", new Shield("Shield", normalCost));
        register("Warrior", new Warrior("Warrior", normalCost));
        register("Thrust", new Thrust("Thrust", normalCost));

        //Miscellaneous
        register("Arsonist", new Arsonist("Arsonist", normalCost));
        register("AngelGift", new AngelGift("AngelGift", normalCost));
        register("Blasting", new Blasting("Blasting", normalCost));
        register("ChampionsBonus", new ChampionsBonus("ChampionsBonus", specialCost));

        register("EndStoneSupply", new EndStoneSupply("EndStoneSupply", normalCost));
        register("EndStoneTomb", new EndStoneTomb("EndStoneTomb", normalCost));
        register("EnhancedGapple", new EnhancedGapple("ChampionsBonus", specialCost));
        register("Explode", new Explode("Explode", normalCost));
        register("Furnace", new Furnace("Furnace", normalCost));
        register("ItemMerchant", new ItemMerchant("ItemMerchant", specialCost));
        register("Metallurgy", new Metallurgy("Metallurgy", specialCost));
        register("Reincarnation", new Reincarnation("Reincarnation", specialCost));
        register("ResourcesSupply", new ResourcesSupply("ResourcesSupply", normalCost));
        register("Shepherd", new Shepherd("Shepherd", normalCost));
        register("WoodSupply", new WoodSupply("WoodSupply", normalCost));
        register("WoodTomb", new WoodTomb("WoodTomb", normalCost));
        register("WoolSupply", new WoolSupply("WoolSupply", normalCost));
        register("WoolTomb", new WoolTomb("WoolTomb", normalCost));

    }

    public boolean upgradeTalent(int amount, String talentid, Player p) {
        Talent talent = org.twightlight.talents.Talents.getInstance().getTalentsManager().getTalentByID(talentid);
        User user = User.getUserFromUUID(p.getUniqueId());
        if (talent == null || user == null) return false;

        int costListSize = talent.getCostList().size();
        Map<String, Integer> map = user.getTalentsData();
        if (map == null || !map.containsKey(talentid)) {
            return false;
        }
        int c_level = map.get(talentid);
        int newLevel = c_level + amount;
        if (newLevel < 0 || newLevel > costListSize) {
            return false;
        }
        map.put(talentid, newLevel);
        return org.twightlight.talents.Talents.getInstance().getDb().update(p, map, "talentsData");
    }

    public int resetTalent(Player p) {
        User user = User.getUserFromUUID(p.getUniqueId());
        if (user == null) return 0;
        int totalRefund = 0;
        Map<String, Integer> map = user.getTalentsData();
        Set<String> talents = map.keySet();
        for (String talentId : talents) {
            int level = map.getOrDefault(talentId, 0);
            if (level > 0) {
                Talent talentInstance = getTalentByID(talentId);
                Debug.debugMsg("Refunding talent with id " + talentId);
                int refund = Utility.totalCost(talentInstance.getCostList(), 0, level - 1);
                if (setLevelTalent(0, talentId, p)) {
                    totalRefund += refund;
                }
            }
        }
        return totalRefund;
    }

    public boolean setLevelTalent(int amount, String talentid, Player p) {
        Talent talent = getTalentByID(talentid);
        User user = User.getUserFromUUID(p.getUniqueId());
        if (talent == null || user == null) return false;

        int costListSize = talent.getCostList().size();
        Map<String, Integer> map = user.getTalentsData();
        if (map == null || !map.containsKey(talentid)) {
            return false;
        }
        if (amount < 0 || amount > costListSize) {
            return false;
        }
        map.put(talentid, amount);
        return org.twightlight.talents.Talents.getInstance().getDb().update(p, map, "talentsData");
    }

    public Set<String> getRegisteredTalents() {
        return Talents.keySet();
    }

    public EventDispatcher getDispatcher() {
        return dispatcher;
    }
}
