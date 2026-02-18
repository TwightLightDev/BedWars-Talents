package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleFirework;
import hm.zelha.particlesfx.particles.ParticleCrit;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

public class IronWill extends Skill {

    private ParticleDustColored shieldOrange;
    private ParticleDustColored shieldGold;
    private ParticleDustColored shieldWhite;
    private ParticleFirework firework;
    private ParticleCrit critParticle;

    public IronWill(String id, List<Integer> costList) {
        super(id, costList);

        shieldOrange = new ParticleDustColored();
        shieldOrange.setColor(new hm.zelha.particlesfx.util.Color(255, 150, 30));
        shieldOrange.setCount(1);
        shieldOrange.setOffset(0, 0, 0);

        shieldGold = new ParticleDustColored();
        shieldGold.setColor(new hm.zelha.particlesfx.util.Color(255, 215, 0));
        shieldGold.setCount(1);
        shieldGold.setOffset(0, 0, 0);

        shieldWhite = new ParticleDustColored();
        shieldWhite.setColor(new hm.zelha.particlesfx.util.Color(255, 250, 230));
        shieldWhite.setCount(1);
        shieldWhite.setOffset(0, 0, 0);

        firework = new ParticleFirework();
        firework.setCount(2);
        firework.setOffset(0.1, 0.1, 0.1);
        firework.setSpeed(0.05);

        critParticle = new ParticleCrit();
        critParticle.setCount(5);
        critParticle.setOffset(0.3, 0.4, 0.3);
        critParticle.setSpeed(0.3);
    }


    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeDamage(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        if (Utility.rollChance(35)) {
            double thornsDamage = 0.2D * level;

            CombatUtils.dealUndefinedDamage(e.getDamagePacket().getAttacker(), thornsDamage,
                    EntityDamageEvent.DamageCause.THORNS,
                    Map.of("ironwill-reflect", true));

            e.getDamagePacket().getAttacker().getWorld().playSound(e.getDamagePacket().getAttacker().getLocation(),
                    XSound.BLOCK_ANVIL_LAND.parseSound(), 0.5F, 2.0F);

            victim.getWorld().playSound(victim.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(),
                    0.6F, 0.8F);
            double healPercent = 0.3 * level;
            victim.setHealth(Math.min(victim.getMaxHealth(), victim.getMaxHealth() * healPercent + victim.getHealth()));
            playThornsReflect(victim, e.getDamagePacket().getAttacker());
        }
    }


    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onRangedDamage(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        if (Utility.rollChance(35)) {
            double thornsDamage = 0.2D * level;

            CombatUtils.dealUndefinedDamage(e.getDamagePacket().getAttacker(), thornsDamage,
                    EntityDamageEvent.DamageCause.THORNS,
                    Map.of("ironwill-reflect", true));

            e.getDamagePacket().getAttacker().getWorld().playSound(e.getDamagePacket().getAttacker().getLocation(),
                    XSound.BLOCK_ANVIL_LAND.parseSound(), 0.5F, 2.0F);

            victim.getWorld().playSound(victim.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(),
                    0.6F, 0.8F);
            double healPercent = 0.3 * level;
            victim.setHealth(Math.min(victim.getMaxHealth(), victim.getMaxHealth() * healPercent + victim.getHealth()));
            playThornsReflect(victim, e.getDamagePacket().getAttacker());

        }

   }

    private void playThornsReflect(Player victim, Player attacker) {
        Location from = victim.getLocation().add(0, 1.0, 0);
        Location to = attacker.getLocation().add(0, 1.0, 0);

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 5) {
                    cancel();
                    return;
                }

                double progress = (double) (tick + 1) / 5;
                Location point = from.clone().add(
                        to.clone().subtract(from).toVector().multiply(progress));
                point.add(0, Math.sin(progress * Math.PI) * 0.8D, 0);

                shieldGold.display(point);

                critParticle.display(point);

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}
