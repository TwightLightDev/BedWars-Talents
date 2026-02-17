package org.twightlight.talents.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.twightlight.talents.Talents;
import org.twightlight.talents.listeners.chat.ChatListener;
import org.twightlight.talents.listeners.combat.DamageCheck;
import org.twightlight.talents.listeners.combat.IgniteCheck;
import org.twightlight.talents.listeners.dispatcher.EventFire;
import org.twightlight.talents.listeners.game.GameInGameDataEvent;
import org.twightlight.talents.listeners.game.GameReSpawnEvent;
import org.twightlight.talents.listeners.game.GameStateChangeEvent;
import org.twightlight.talents.listeners.general.GeneralCombatCheck;
import org.twightlight.talents.listeners.general.GeneralIgniteCheck;
import org.twightlight.talents.listeners.general.GeneralJoinEvent;
import org.twightlight.talents.listeners.general.GeneralMoveEvent;
import org.twightlight.talents.listeners.menu.InventoryClickEvent;
import org.twightlight.talents.listeners.menu.InventoryOpenEvent;
import org.twightlight.talents.listeners.reset.GameLeaveEvent;
import org.twightlight.talents.listeners.reset.QuitEvent;

public class EventsManager {
    private static final PluginManager manager = Bukkit.getPluginManager();

    public static void load() {
        register(new QuitEvent());
        register(new GameLeaveEvent());
        register(new InventoryClickEvent());
        register(new InventoryOpenEvent());
        register(new GeneralJoinEvent());
        register(new GameStateChangeEvent());
        register(new IgniteCheck());
        register(new ChatListener());
        register(new DamageCheck());
        register(new ChatListener());
        register(new EventFire());
        register(new GameReSpawnEvent());
        register(new GeneralMoveEvent());
        register(new GeneralIgniteCheck());
        register(new GameInGameDataEvent());
        register(new GeneralCombatCheck());
    }

    private static void register(Listener listener) {
        manager.registerEvents(listener, Talents.getInstance());
    }
}
