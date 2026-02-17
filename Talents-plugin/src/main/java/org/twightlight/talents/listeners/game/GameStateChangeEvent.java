package org.twightlight.talents.listeners.game;

import com.andrei1058.bedwars.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.RegenEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.attributes.AttributesData;
import org.twightlight.talents.menus.AttributeMenu;
import org.twightlight.talents.users.InGameData;
import org.twightlight.talents.users.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStateChangeEvent implements Listener {

    private static Map<UUID, BukkitTask> healingTask = new HashMap<>();
    private static Map<UUID, BukkitTask> actionBarTask = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameStateChange(com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent e) {
        if (e.getNewState() == GameState.playing) {
            Arena arena = Talents.getInstance().getArenaManager().createNewArena(e.getArena());

            for (Player player : e.getArena().getPlayers()) {
                StatsMap newSM = new StatsMap(player);
                AttributesData attributesData = new AttributesData();
                InGameData igd = new InGameData();
                arena.getStatsMaps().put(player.getUniqueId(), newSM);
                arena.getAttributesDataMap().put(player.getUniqueId(), attributesData);
                arena.getInGameDataMap().put(player.getUniqueId(), igd);
                new AttributeMenu(player);


                Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    BukkitTask t = Bukkit.getScheduler().runTaskTimer(Talents.getInstance(), () -> {
                        if (User.isPlaying(player) && Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(player).equals(arena.getArena())) {
                            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(player.getUniqueId());
                            double heal = map.getStatContainer("REGENERATION_PER_FIVE_SECONDS").get();
                            EntityRegainHealthEvent event1 = new EntityRegainHealthEvent(player, heal, EntityRegainHealthEvent.RegainReason.SATIATED);
                            RegenEvent event = new RegenEvent(event1);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                double finalHeal = event.getRegenPacket().getHealProperty().getHealAmount(Collections.emptyList());

                                player.setHealth(Math.min(player.getHealth() + finalHeal, player.getMaxHealth()));
                                event.getRegenPacket().getFinalConsumers().forEach((c) -> c.accept(event));
                                event.getRegenPacket().selfDestroy();
                            }
                        }
                    }, 0L, 100L);

                    if (healingTask.containsKey(player.getUniqueId())) {
                        healingTask.get(player.getUniqueId()).cancel();
                    }
                    healingTask.put(player.getUniqueId(), t);


                }, 100L);

                BukkitTask t = Bukkit.getScheduler().runTaskTimer(Talents.getInstance(), () -> {
                    if (User.isPlaying(player) && Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(player).equals(arena.getArena())) {
                        StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(player.getUniqueId());

                        int armor = PVPManager.getInstance().getVersionHandler().getVanillaUtils().getTotalArmorPoints(player);
                        armor += Math.toIntExact(Math.round(map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_ARMOR.name()).get()));

                        Talents.getInstance().getActionBarHandler()
                                .sendActionBar(player, ChatColor.translateAlternateColorCodes('&',"&c{hp}/{max_hp}❤                 &a{armor}❈"
                                        .replace("{hp}", String.format("%.1f", player.getHealth()))
                                        .replace("{max_hp}", String.format("%.1f", player.getMaxHealth()))
                                        .replace("{armor}", armor + "")));
                    }
                }, 10L, 8L);

                if (actionBarTask.containsKey(player.getUniqueId())) {
                    actionBarTask.get(player.getUniqueId()).cancel();
                }
                actionBarTask.put(player.getUniqueId(), t);
            }
        }
    }

    public static Map<UUID, BukkitTask> getHealingTask() {
        return healingTask;
    }

    public static Map<UUID, BukkitTask> getActionBarTask() {
        return actionBarTask;
    }
}
