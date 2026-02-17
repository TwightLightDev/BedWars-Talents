package org.twightlight.talents.listeners.dispatcher;

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.users.User;

public class EventFire implements Listener {

    private void dispatch(Event event) {
        Talents.getInstance().getTalentsManager().getDispatcher().dispatch(event);
        Talents.getInstance().getSkillsManager().getDispatcher().dispatch(event);
        Talents.getInstance().getRunesManager().getDispatcher().dispatch(event);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(MeleeDamageEvent e) {
        if (!User.isPlaying(e.getDamagePacket().getAttacker())) return;
        if (User.getUserFromBukkitPlayer(e.getDamagePacket().getAttacker()) == null) return;

        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(RangedDamageEvent e) {
        if (!User.isPlaying(e.getDamagePacket().getAttacker())) return;
        if (User.getUserFromBukkitPlayer(e.getDamagePacket().getAttacker()) == null) return;

        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(UndefinedDamageEvent e) {
        if (!User.isPlaying(e.getDamagePacket().getVictim())) return;
        if (User.getUserFromBukkitPlayer(e.getDamagePacket().getVictim()) == null) return;

        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(GameStateChangeEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(EntityShootBowEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(PlayerGeneratorCollectEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(ShopBuyEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(GameEndEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(PlayerReSpawnEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(PlayerItemConsumeEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(EntityDamageByEntityEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(PlayerKillEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(ProjectileHitEvent e) {
        dispatch(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEvent(PlayerBedBreakEvent e) {
        dispatch(e);
    }
}
