package org.twightlight.talents.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.menus.skills.SkillItem;
import org.twightlight.talents.menus.skills.SkillsMenuRegistry;
import org.twightlight.talents.skills.built_in_skills.*;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Debug;
import org.twightlight.talents.utils.Utility;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class SkillsManager {

    public Map<String, Skill> Skills;
    public Map<String, SkillItem> SkillItems;

    private List<Integer> normalCost = Arrays.asList(100, 100, 100, 100, 100, 250, 250, 250, 250, 250, 375, 375, 375, 375, 375, 475, 475, 475, 475, 475);
    private EventDispatcher dispatcher = new EventDispatcher();

    public void register(String id, Skill skill) {
        this.register(id, skill, false);
    }

    public void register(String id, Skill skill, boolean override) {
        // Load YML file for this skill
        File file = new File("plugins/Talents/skills/" + id + ".yml");
        if (!file.exists()) {
            String path = "skills/" + id + ".yml";
            InputStream in = Talents.getInstance().getResource(path);
            if (in == null) {
                Debug.debugMsg("File " + id + ".yml not found in jar for skill, registering without menu item!");
                if (override) {
                    this.Skills.put(id, skill);
                } else {
                    this.Skills.putIfAbsent(id, skill);
                }
                return;
            }
            Talents.getInstance().saveResource(path, false);
            Debug.debugMsg("File " + id + ".yml not found, load default file!");
            file = new File("plugins/Talents/skills/" + id + ".yml");
        }

        if (override) {
            this.Skills.put(id, skill);
        } else {
            this.Skills.putIfAbsent(id, skill);
        }

        // Create SkillItem and register it in the paginated menu
        SkillItem skillItem = new SkillItem(id, YamlConfiguration.loadConfiguration(file), skill);
        SkillItems.putIfAbsent(id, skillItem);
        SkillsMenuRegistry.setItem(skillItem.getPage(), skillItem.getSlot(), skillItem.getButton());
        SkillsMenuRegistry.registerSkillItem(skillItem.getPage(), skillItem);
        Debug.debugMsg("Registered skill with id: " + id + " at page " + skillItem.getPage() + " slot " + skillItem.getSlot());
    }

    public Skill getSkillByID(String id) {
        return Skills.getOrDefault(id, null);
    }

    public SkillItem getSkillItemByID(String id) {
        return SkillItems.getOrDefault(id, null);
    }

    public boolean skillExist(String id) {
        return Skills.containsKey(id);
    }

    public SkillsManager() {
        Skills = new HashMap<>();
        SkillItems = new HashMap<>();
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            registerSkills();

            Skills.values().forEach((skill -> {
                dispatcher.register(skill);
            }));

            Debug.debugMsg(ChatColor.GREEN + "Successfully registered " + Skills.size() + " skills!");
        }, 40L);
    }

    private void registerSkills() {
        register("JusticeJudgement", new JusticeJudgement("JusticeJudgement", normalCost));
        register("MeteorRain", new MeteorRain("MeteorRain", normalCost));
        register("SoulCollector", new SoulCollector("SoulCollector", normalCost));
        register("Architect", new Architect("Architect", normalCost));
        register("Berserk", new Berserk("Berserk", normalCost));
        register("BrutalRevenge", new BrutalRevenge("BrutalRevenge", normalCost));
        register("Combo", new Combo("Combo", normalCost));
        register("DefensiveAura", new DefensiveAura("DefensiveAura", normalCost));
        register("Reinforcement", new Reinforcement("Reinforcement", normalCost));
        register("Summoner", new Summoner("Summoner", normalCost));
        register("IronWill", new IronWill("IronWill", normalCost));
        register("Marksman", new Marksman("Marksman", normalCost));
        register("PhantomStrike", new PhantomStrike("PhantomStrike", normalCost));
        register("Resonance", new Resonance("Resonance", normalCost));
        register("RiftWalker", new RiftWalker("RiftWalker", normalCost));
    }

    public void selectSkill(Player p, int slot, String id) {
        if (slot < 0 || slot >= 4) return;
        User user = User.getUserFromUUID(p.getUniqueId());
        if (user == null) return;

        List<String> selects = user.getActivatingSkills();
        if (skillExist(id) && !Utility.contains(selects, id)) {
            selects.set(slot, id);
        }
        Talents.getInstance().getDb().update(p, String.join(";", selects), "skills", "selecting");
    }

    public void deselectSkill(Player p, int slot) {
        if (slot < 0 || slot >= 4) return;
        User user = User.getUserFromUUID(p.getUniqueId());
        if (user == null) return;
        List<String> selects = user.getActivatingSkills();
        selects.set(slot, "");
        Talents.getInstance().getDb().update(p, String.join(";", selects), "skills", "selecting");
    }

    public void resetActivatingSkill(Player p) {
        User user = User.getUserFromUUID(p.getUniqueId());
        if (user == null) return;
        Talents.getInstance().getDb().update(p, "", "skills", "selecting");
    }

    public boolean upgradeSkill(int amount, String skillId, Player p) {
        Skill skill = Talents.getInstance().getSkillsManager().getSkillByID(skillId);
        User user = User.getUserFromUUID(p.getUniqueId());
        if (skill == null || user == null) return false;

        int costListSize = skill.getCostList().size();
        Map<String, Integer> map = user.getSkillsData();
        if (map == null || !map.containsKey(skillId)) {
            return false;
        }
        int c_level = map.get(skillId);
        int newLevel = c_level + amount;
        if (newLevel < 0 || newLevel > costListSize) {
            return false;
        }
        map.put(skillId, newLevel);
        return Talents.getInstance().getDb().update(p, map, "skills", "skills");
    }

    public int resetSkills(Player p) {
        User user = User.getUserFromUUID(p.getUniqueId());
        if (user == null) return 0;
        int totalRefund = 0;
        Map<String, Integer> map = user.getSkillsData();
        Set<String> skills = map.keySet();
        for (String talentId : skills) {
            int level = map.getOrDefault(talentId, 0);
            if (level > 0) {
                Skill skillInstance = getSkillByID(talentId);
                Debug.debugMsg("Refunding skill with id " + talentId);
                int refund = Utility.totalCost(skillInstance.getCostList(), 0, level - 1);
                if (setLevelSkill(0, talentId, p)) {
                    totalRefund += refund;
                }
            }
        }
        return totalRefund;
    }

    public boolean setLevelSkill(int amount, String skillId, Player p) {
        Skill talent = getSkillByID(skillId);
        User user = User.getUserFromUUID(p.getUniqueId());
        if (talent == null || user == null) return false;

        int costListSize = talent.getCostList().size();
        Map<String, Integer> map = user.getSkillsData();
        if (map == null || !map.containsKey(skillId)) {
            return false;
        }
        if (amount < 0 || amount > costListSize) {
            return false;
        }
        map.put(skillId, amount);
        return Talents.getInstance().getDb().update(p, map, "skills", "skills");
    }

    public Set<String> getRegisteredSkills() {
        return Skills.keySet();
    }

    public EventDispatcher getDispatcher() {
        return dispatcher;
    }
}
