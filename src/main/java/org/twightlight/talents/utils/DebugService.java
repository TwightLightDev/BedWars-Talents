package org.twightlight.talents.utils;

import org.twightlight.talents.Talents;

public class DebugService {
    public static void debugMsg(String message) {
        if (Talents.debug) {
            System.out.println("[Debug] " + message);
        }
    }
}
