package org.twightlight.talents.talents;

import org.bukkit.entity.Player;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;

import java.util.*;

public class TalentsManagerService {
    public HashMap<String, HashMap<String, Talent<?>>> Talents = new HashMap<>();


    public void register(String id, Talent<?> talent) {
        register(id, talent, false);
    }

    public void register(String id, Talent<?> talent, boolean override) {
        Talents.putIfAbsent(talent.getCategory().getColumn(), new HashMap<>());
        if (Talents.get(talent.getCategory().getColumn()).containsKey(id)) {
            if (override) {
                Talents.get(talent.getCategory().getColumn()).put(id, talent);
            } else {}
        } else {
            Talents.get(talent.getCategory().getColumn()).put(id, talent);
        }
    }

    public Set<String> getInnerTalents(TalentsCategory category) {
        if (category == null) {
            return Collections.emptySet();
        }
        Map<String, ?> talentsMap = Talents.get(category.getColumn());
        return talentsMap != null ? talentsMap.keySet() : Collections.emptySet();
    }

    public Object handle(Player p, String id, TalentsCategory cat, List<Object> params) {
        return org.twightlight.talents.Talents.getInstance().getTalentsManagerService().Talents.get(cat.getColumn())
                .get(id).handle(
                        org.twightlight.talents.Talents.getInstance().getDatabase().getTalentLevel(
                                p,
                                cat,
                                id)
                        , params);
    }
}
