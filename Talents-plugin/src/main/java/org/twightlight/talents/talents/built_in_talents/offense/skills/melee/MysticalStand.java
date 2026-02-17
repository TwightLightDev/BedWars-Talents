package org.twightlight.talents.talents.built_in_talents.offense.skills.melee;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.interfaces.Metadatable;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MysticalStand extends Talent {
    public MysticalStand(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("ADDITIONAL_ATTACK_RATE").add(level * 1.5);
            if (level > 0)
                map.getStatContainer("ADDITIONAL_ATTACK_DAMAGE").add(10 + level * 0.5);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();
        if (isExtraAttack(e.getDamagePacket())) return;
        StatsMap astatsMap = packet.getAttackerStatsMap();

        double chance = astatsMap.getStatContainer("ADDITIONAL_ATTACK_RATE").get();
        double ratio = astatsMap.getStatContainer("ADDITIONAL_ATTACK_DAMAGE").get()/100;

        if (!Utility.rollChance(chance)) return;
        double damage = e.getDamagePacket().getDamageProperty().getBaseDamage();
        Player attacker = e.getDamagePacket().getAttacker();
        LivingEntity victim = e.getDamagePacket().getVictim();
        packet.putMetadata("additional-attack-activated", true);

        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            CombatUtils.dealMeleeDamage(attacker, victim, damage * ratio, Map.of("additional-attack", true), Set.of("damageLayer1"));
            Sound sound = XSound.ENTITY_PLAYER_ATTACK_SWEEP.parseSound();

            attacker.getWorld().playSound(attacker.getLocation(), sound, 10, 5);
        }, 10L);
    }


    public static boolean isExtraAttack(Metadatable metadatable) {
        return metadatable.containsMetadata("additional-attack");
    }
}
