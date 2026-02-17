package org.twightlight.talents;

import com.andrei1058.bedwars.api.BedWars;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import hm.zelha.particlesfx.util.ParticleSFX;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.talents.commands.RunesCommand;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.handlers.ActionBarHandler;
import org.twightlight.talents.nmsbridge.NMSBridge;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.talents.arenas.ArenaManager;
import org.twightlight.talents.commands.SkillsCommand;
import org.twightlight.talents.commands.TalentsCommand;
import org.twightlight.talents.database.SQLite;
import org.twightlight.talents.handlers.StatsMapHandler;
import org.twightlight.talents.listeners.EventsManager;
import org.twightlight.talents.nmsbridge.v1_12_R1.v1_12_R1;
import org.twightlight.talents.nmsbridge.v1_8_R3.v1_8_R3;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.skills.SkillsManager;
import org.twightlight.talents.talents.TalentsManager;
import org.twightlight.talents.utils.Utility;

import java.io.File;

public final class Talents extends JavaPlugin {
    private BedWars api;
    private ArenaManager arenaManager;
    private TalentsManager talentsManager;
    private SkillsManager skillsManager;
    private RunesManager runesManager;
    private NMSBridge nmsBridge;
    private SQLite db;
    private static final String version = Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1];
    private static boolean debug = true;
    private PacketEventsAPI<?> packetEventsAPI;
    private ActionBarHandler actionBarHandler;

    public void onLoad() {
        if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
        } else {
            Utility.error("PacketEvents plugin not found");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        File talents = new File(getDataFolder().getPath() + "/talents");
        if (!talents.exists()) {
            talents.mkdirs();
        }
        loadDependencies();
        ParticleSFX.setPlugin(this);
        PVPManager.getInstance().registerStatsMapHandler(new StatsMapHandler());
        this.db = new SQLite(this);
        this.arenaManager = new ArenaManager();
        this.talentsManager = new TalentsManager();
        this.skillsManager = new SkillsManager();
        this.runesManager = new RunesManager();
        this.actionBarHandler = new ActionBarHandler();
        loadNMS();
        EventsManager.load();
        registerCommands();
    }

    private void loadNMS() {
        Utility.info("Detected NMS version " + version + ", trying to load...");
        switch (version) {
            case "8":
                nmsBridge = new v1_8_R3();
                break;
            case "12":
                nmsBridge = new v1_12_R1();
                break;
        }
    }
    @Override
    public void onDisable() {


    }

    public static Talents getInstance() {
        return getPlugin(Talents.class);
    }

    public BedWars getAPI() {
        return api;
    }

    public TalentsManager getTalentsManager() {
        return talentsManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public SQLite getDb() {
        return db;
    }

    public SkillsManager getSkillsManager() {
        return skillsManager;
    }

    private void registerCommands() {
        getCommand("talents").setExecutor(new TalentsCommand());
        getCommand("skills").setExecutor(new SkillsCommand());
        getCommand("runes").setExecutor(new RunesCommand());
    }

    public NMSBridge getNMSBridge() {
        return nmsBridge;
    }

    public static boolean isDebug() {
        return debug;
    }

    public PacketEventsAPI<?> getPacketEventsAPI() {
        return packetEventsAPI;
    }

    private void loadDependencies() {
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") != null) {
            this.api = (BedWars)Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
        } else {
            Utility.error("BedWars plugin not found");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        PacketEvents.getAPI().init();
        this.packetEventsAPI = PacketEvents.getAPI();
        if (Bukkit.getPluginManager().getPlugin("PVPManager") != null) {
        } else {
            Utility.error("PVPManager plugin not found");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public RunesManager getRunesManager() {
        return runesManager;
    }

    public ActionBarHandler getActionBarHandler() {
        return actionBarHandler;
    }
}
