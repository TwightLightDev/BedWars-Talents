package org.twightlight.talents.users;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.twightlight.talents.Talents;
import org.twightlight.talents.runes.RunesManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private static final Map<UUID, User> playersInstance = new ConcurrentHashMap<>();
    private final UUID uuid;
    private boolean vulnerable;

    private Map<String, Integer> talentsData;

    private Map<String, Integer> skillsData;
    private List<String> activatingSkills;

    private Map<String, Integer> runesData;
    private Map<String, List<String>> n_selecting;
    private Map<String, List<String>> s_selecting;

    private Map<String, Object> metadata;

    public User(Player player) {
        playersInstance.put(player.getUniqueId(), this);
        this.uuid = player.getUniqueId();
        this.vulnerable = true;
        this.metadata = new HashMap<>();
        talentsData = Talents.getInstance().getDb().getTalentsMap(player);
        for (String talent : Talents.getInstance().getTalentsManager().getRegisteredTalents()) {
            if (!talentsData.containsKey(talent)) {
                talentsData.put(talent, 0);
            }
        }

        talentsData.keySet().removeIf(
                id -> !Talents.getInstance().getTalentsManager().talentExist(id)
        );

        skillsData = Talents.getInstance().getDb().getSkillsMap(player);
        for (String skill : Talents.getInstance().getSkillsManager().getRegisteredSkills()) {
            if (!skillsData.containsKey(skill)) {
                skillsData.put(skill, 0);
            }
        }
        skillsData.keySet().removeIf(
                id -> !Talents.getInstance().getSkillsManager().skillExist(id)
        );



        runesData = Talents.getInstance().getDb().getRunesStorage(player);
        for (String skill : Talents.getInstance().getRunesManager().getRegisteredRunes()) {
            if (!runesData.containsKey(skill)) {
                runesData.put(skill, 0);
            }
        }

        runesData.keySet().removeIf(
                id -> !Talents.getInstance().getRunesManager().runeExist(id)
        );


        n_selecting = Talents.getInstance().getDb().getSelectingRunes(player, "n");
        s_selecting = Talents.getInstance().getDb().getSelectingRunes(player, "s");

        for (String cat : Talents.getInstance().getRunesManager().getCategories()) {
            List<String> selecting = n_selecting.computeIfAbsent(cat, (k) -> new ArrayList<>());

            while (selecting.size() < 7) {
                selecting.add("");
            }

            selecting = s_selecting.computeIfAbsent(cat, (k) -> new ArrayList<>());

            while (selecting.size() < 1) {
                selecting.add("");
            }
        }

        activatingSkills = Talents.getInstance().getDb().getActivatingSkills(player);

        save();
    }

    public boolean isPlaying() {
        return Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(Bukkit.getPlayer(uuid)) != null;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }

    public static User getUserFromUUID(UUID uuid) {
        return playersInstance.getOrDefault(uuid, null);
    }

    public static User getUserFromBukkitPlayer(Player p) {
        return playersInstance.getOrDefault(p.getUniqueId(), null);
    }

    public Map<String, Integer> getTalentsData() {
        return talentsData;
    }

    public Integer getTalentLevel(String id) {
        return talentsData.getOrDefault(id, 0);
    }

    public List<String> getActivatingSkills() {
        return activatingSkills;
    }

    public Map<String, Integer> getSkillsData() {
        return skillsData;
    }

    public Integer getSkillLevel(String id) {
        return skillsData.getOrDefault(id, 0);
    }

    public List<String> getActivatingSkills(List<String> activatingSkills) {
        return activatingSkills;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(String s, Object o) {
        metadata.put(s, o);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    public Object getMetadataValue(String v) {
        return metadata.getOrDefault(v, null);
    }

    public Map<String, Integer> getRunesData() {
        return runesData;
    }

    public Map<String, List<String>> getSelectingRunes(String prefix) {
        if (prefix.equalsIgnoreCase(RunesManager.NORMAL_PREFIX)) {
            return n_selecting;
        } else if (prefix.equalsIgnoreCase(RunesManager.SPECIAL_PREFIX)) {
            return s_selecting;
        }
        return n_selecting;
    }

    public static boolean isPlaying(Player p) {

        if (User.getUserFromBukkitPlayer(p) == null) return false;

        return Talents.getInstance().getAPI().getArenaUtil().isPlaying(p);
    }

    public void save() {
        Player p = Bukkit.getPlayer(uuid);

        Talents.getInstance().getDb().update(p, String.join(";", activatingSkills), "skills", "selecting");
        Talents.getInstance().getDb().update(p, skillsData, "skills", "skills");

        Talents.getInstance().getDb().update(p, talentsData, "talentsData");

        Talents.getInstance().getDb().update(p, runesData, "runes", "storage");
        Talents.getInstance().getDb().update(p, n_selecting, "runes", "n_selecting");
        Talents.getInstance().getDb().update(p, s_selecting, "runes", "s_selecting");
    }

    public void removeMetadata(String o) {
        metadata.remove(o);
    }
}
