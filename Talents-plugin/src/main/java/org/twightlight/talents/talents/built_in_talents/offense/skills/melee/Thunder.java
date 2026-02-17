package org.twightlight.talents.talents.built_in_talents.offense.skills.melee;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleFirework;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Thunder extends Talent {

    private static ParticleFirework firework = new ParticleFirework(0D, 0D, 0D, 2);

    static {
        firework.setVelocity(new Vector(0, 0, 0));
    }

    public Thunder(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            User user = User.getUserFromUUID(p.getUniqueId());
            Integer level = user.getTalentLevel(getTalentId());
            map.getStatContainer("THUNDER").add(level * 1.5);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        StatsMap astatsMap = packet.getAttackerStatsMap();

        double chance = astatsMap.getStatContainer("THUNDER").get();

        if (!Utility.rollChance(chance)) return;

        packet.putMetadata("thunder-activated", true);
        EntityDamageEvent lightningEvent = new EntityDamageEvent(packet.getVictim(), EntityDamageEvent.DamageCause.LIGHTNING, 0.0D);
        Bukkit.getPluginManager().callEvent(lightningEvent);
        if (!lightningEvent.isCancelled()) {
            LivingEntity entity = packet.getVictim();
            LivingEntity attacker = packet.getAttacker();

            CombatUtils.dealTrueDamage(2, attacker, entity);

            if (attacker.getHealth() > 0.0D) {
                attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 1.0D));
            }

            for(int i = 0; i < 6; ++i) {
                firework.display(packet.getVictim().getLocation().add(0.0D, (double)i * 0.2D * (double)(6 - i) + 1.5D, 0.0D));
            }

            Sound sound = XSound.ENTITY_LIGHTNING_BOLT_THUNDER.parseSound();

            attacker.getWorld().playSound(attacker.getLocation(), sound, 10, 5);
        }
    }
}
