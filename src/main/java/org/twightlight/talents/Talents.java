package org.twightlight.talents;

import com.andrei1058.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.talents.commands.MainCommands;
import org.twightlight.talents.database.SQLite;
import org.twightlight.talents.listeners.game.*;
import org.twightlight.talents.listeners.general.*;
import org.twightlight.talents.menus.TalentsMenu;
import org.twightlight.talents.talents.TalentsManagerService;
import org.twightlight.talents.talents.built_in_talents.intersection.*;
import org.twightlight.talents.talents.built_in_talents.melee.directly.*;
import org.twightlight.talents.talents.built_in_talents.melee.skills.FireAspect;
import org.twightlight.talents.talents.built_in_talents.melee.skills.MeleeAttackSpeed;
import org.twightlight.talents.talents.built_in_talents.melee.skills.MeleeSlowing;
import org.twightlight.talents.talents.built_in_talents.melee.skills.Thunder;
import org.twightlight.talents.talents.built_in_talents.miscellaneous.*;
import org.twightlight.talents.talents.built_in_talents.protective.directly.*;
import org.twightlight.talents.talents.built_in_talents.protective.skills.*;
import org.twightlight.talents.talents.built_in_talents.ranged.directly.ArrowAttackDamage;
import org.twightlight.talents.talents.built_in_talents.ranged.directly.CriticalArrow;
import org.twightlight.talents.talents.built_in_talents.ranged.skills.*;
import org.twightlight.talents.talents.built_in_talents.special.KingOfBedWars;
import org.twightlight.talents.talents.built_in_talents.supportive.*;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.Utility;

import java.util.Arrays;
import java.util.List;

public final class Talents extends JavaPlugin {
    public static boolean debug = false;
    private SQLite database;
    private TalentsManagerService talentsManagerService;
    private List<Integer> normalCost = Arrays.asList(50, 50, 50, 50, 50, 80, 80, 80, 80, 80
    , 120, 120, 120, 120, 120, 150, 150, 150, 150, 150);
    private List<Integer> specialCost = Arrays.asList(75, 75, 75, 75, 75, 120, 120, 120, 120, 120
            , 180, 180, 180, 180, 180, 225, 225, 225, 225, 225);
    private List<Integer> highestCost = Arrays.asList(125, 125, 125, 125, 125, 200, 200, 200, 200, 200
            , 300, 300, 300, 300, 300, 375, 375, 375, 375, 375);
    private BedWars api;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        Utility.info("Talents is starting...");
        talentsManagerService = new TalentsManagerService();
        database = new SQLite(this);
        loadDependencies();
        registerTalents();
        loadListeners();
        getCommand("talents").setExecutor(new MainCommands());

        TalentsMenu.load();
    }

    private void registerTalents() {
        //Melee
        talentsManagerService.register("IMD", new IncreaseMultiDamage(TalentsCategory.Melee, normalCost));
        talentsManagerService.register("MLS", new MultiLifeSteal(TalentsCategory.Melee, specialCost));
        talentsManagerService.register("FA", new FireAspect(TalentsCategory.Melee, normalCost));
        talentsManagerService.register("MSL", new MeleeSlowing(TalentsCategory.Melee, specialCost));
        talentsManagerService.register("AMA", new AdditionalMeleeAttack(TalentsCategory.Melee, specialCost));
        talentsManagerService.register("AMAD", new AdditionalMeleeAttackDamage(TalentsCategory.Melee, normalCost));
        talentsManagerService.register("CH", new CriticalHit(TalentsCategory.Melee, specialCost));
        talentsManagerService.register("CD", new CriticalDamage(TalentsCategory.Melee, normalCost));
        talentsManagerService.register("IHE", new IncreaseHealingEffect(TalentsCategory.Melee, specialCost));
        talentsManagerService.register("SLS", new SubLifeSteal(TalentsCategory.Melee, normalCost));
        talentsManagerService.register("TH", new Thunder(TalentsCategory.Melee, specialCost));
        talentsManagerService.register("MAD", new MeleeAttackDamage(TalentsCategory.Melee, normalCost));
        talentsManagerService.register("AP", new ArmorPenetration(TalentsCategory.Melee, normalCost));
        talentsManagerService.register("MAS", new MeleeAttackSpeed(TalentsCategory.Melee, normalCost));
        //Protective
        talentsManagerService.register("DR", new DamageReduction(TalentsCategory.Protective, specialCost));
        talentsManagerService.register("ABS", new Absorption(TalentsCategory.Protective, normalCost));
        talentsManagerService.register("BL", new Blocking(TalentsCategory.Protective, specialCost));
        talentsManagerService.register("BLC", new BlockingChance(TalentsCategory.Protective, normalCost));
        talentsManagerService.register("CDR", new CriticalDamageReduction(TalentsCategory.Protective, normalCost));
        talentsManagerService.register("FDR", new FallDamageReduction(TalentsCategory.Protective, normalCost));
        talentsManagerService.register("THR", new Thorns(TalentsCategory.Protective, specialCost));
        talentsManagerService.register("IHE", new IncreaseHealingEffect(TalentsCategory.Protective, specialCost));
        talentsManagerService.register("MHP", new MaxHealth(TalentsCategory.Protective, normalCost));
        talentsManagerService.register("RFL", new Reflection(TalentsCategory.Protective, specialCost));
        talentsManagerService.register("YH", new YellowHearts(TalentsCategory.Protective, normalCost));
        talentsManagerService.register("GNT", new Giant(TalentsCategory.Protective, specialCost));
        talentsManagerService.register("SRG", new SelfRegen(TalentsCategory.Protective, normalCost));

        //Ranged
        talentsManagerService.register("IMD", new IncreaseMultiDamage(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("AAD", new ArrowAttackDamage(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("SA", new SpeedAspect(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("FRZ", new Frozen(TalentsCategory.Ranged, specialCost));
        talentsManagerService.register("MLS", new MultiLifeSteal(TalentsCategory.Ranged, specialCost));
        talentsManagerService.register("AKB", new ArrowKnockback(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("SLS", new SubLifeSteal(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("AP", new ArmorPenetration(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("CA", new CriticalArrow(TalentsCategory.Ranged, specialCost));
        talentsManagerService.register("CD", new CriticalDamage(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("SM", new Summoner(TalentsCategory.Ranged, specialCost));
        talentsManagerService.register("DLC", new Duplicate(TalentsCategory.Ranged, normalCost));
        talentsManagerService.register("TWIN", new Twins(TalentsCategory.Ranged, specialCost));

        //Miscellaneous
        talentsManagerService.register("RSSB", new ResourcesBonus(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("MCH", new Merchant(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("EMS", new ExplosionMaster(TalentsCategory.Miscellaneous, specialCost));
        talentsManagerService.register("MSS", new MoreSoulStones(TalentsCategory.Miscellaneous, specialCost));
        talentsManagerService.register("IW", new InitialWool(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("IWD", new InitialWood(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("IES", new InitialEndStone(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("GGF", new GoldGift(TalentsCategory.Miscellaneous, specialCost));
        talentsManagerService.register("BGA", new BetterGoldenApple(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("APR", new AdditionalProps(TalentsCategory.Miscellaneous, specialCost));
        talentsManagerService.register("AGF", new AngelGift(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("IRS", new InstantRespawn(TalentsCategory.Miscellaneous, specialCost));
        talentsManagerService.register("WT", new WoolTomb(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("WDT", new WoodTomb(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("EST", new EndStoneTomb(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("FBD", new FireballDamage(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("TNTD", new TNTDamage(TalentsCategory.Miscellaneous, normalCost));
        talentsManagerService.register("IRSS", new InitialResources(TalentsCategory.Miscellaneous, normalCost));

        //Supportive
        talentsManagerService.register("SHR", new Shura(TalentsCategory.Supportive, specialCost));
        talentsManagerService.register("BTH", new BountyHunter(TalentsCategory.Supportive, normalCost));
        talentsManagerService.register("ARS", new Ares(TalentsCategory.Supportive, specialCost));
        talentsManagerService.register("WR", new Warrior(TalentsCategory.Supportive, normalCost));
        talentsManagerService.register("ASA", new Assassin(TalentsCategory.Supportive, normalCost));
        talentsManagerService.register("SLD", new Shield(TalentsCategory.Supportive, normalCost));
        //special
        talentsManagerService.register("KOBW", new KingOfBedWars(TalentsCategory.Special, highestCost));

    }

    private void loadDependencies() {
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") != null) {
            api = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
        } else {
            Utility.error("BedWars plugin not found");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new GeneralPlayerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralMeleeAttack(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralArrowAttack(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralIgniteCheck(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralFallCheck(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralInventoryClickEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralInventoryOpenEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralMoveEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralConsumeEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralQuitEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralShotBowEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralFireballExplosionEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralTNTExplosionEvent(), this);

        Bukkit.getPluginManager().registerEvents(new GameEndEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GameKillEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GamePlayerCollectGeneratorEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GameReSpawnEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GameRejoinEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GameShopBuyEvent(), this);
        Bukkit.getPluginManager().registerEvents(new GameTeamAssign(), this);
    }

    @Override
    public void onDisable() {
    }

    public static Talents getInstance() {
        return getPlugin(Talents.class);
    }

    public TalentsManagerService getTalentsManagerService() {
        return talentsManagerService;
    }
    public SQLite getDatabase() {
        return database;
    }
    public BedWars getApi() {
        return api;
    }
}
