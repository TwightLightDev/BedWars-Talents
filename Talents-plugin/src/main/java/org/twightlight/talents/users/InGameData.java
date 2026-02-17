package org.twightlight.talents.users;

import com.cryptomorin.xseries.XMaterial;

import java.util.HashMap;
import java.util.Map;

public class InGameData {
    public static String CURRENT_KILLS = "kills";
    public static String CURRENT_DEATH = "death";

    private Map<String, Integer> data;

    public InGameData() {
        data = new HashMap<>();
        data.put(CURRENT_DEATH, 0);
        data.put(CURRENT_KILLS, 0);
    }

    public Integer get(String a) {
        return data.getOrDefault(a, 0);
    }

    public void add(String a) {
        add(a, 1);
    }

    public void add(String a, int v) {
        int i = get(a);
        data.put(a, i+v);
    }
}
