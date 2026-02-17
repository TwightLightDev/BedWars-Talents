package org.twightlight.talents.runes.categories.offense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleFirework;
import jdk.jshell.execution.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.nmsbridge.abstracts.objects.BoundingBox;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.offense.OffenseRune;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

public class Electrifying extends OffenseRune {
    private static String hitsmetadatavalue = "rune.electrifying.hits";
    private static ParticleFirework firework = new ParticleFirework(0D, 0D, 0D, 2);

    static {
        firework.setVelocity(new Vector(0, 0, 0));
    }

    public Electrifying() {
        super(64);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer("THUNDER").add(amount * 10);
            map.getStatContainer(BaseStats.INCREASE_MELEE_DAMAGE.name()).add(amount * 6);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOW)
    public void onMeleeAttack(MeleeDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "Sword");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;

        User user = User.getUserFromBukkitPlayer(e.getDamagePacket().getAttacker());
        if (user == null) return;


        if (!user.hasMetadata(hitsmetadatavalue) || !(user.getMetadataValue(hitsmetadatavalue) instanceof Integer)) {
            user.setMetadata(hitsmetadatavalue, 0);
        }
        int current_hits = (Integer) user.getMetadataValue(hitsmetadatavalue);

        current_hits ++;

        user.setMetadata(hitsmetadatavalue, current_hits);

        if (current_hits >= 4) {
            MeleeDamagePacket packet = e.getDamagePacket();
            current_hits -= 4;
            user.setMetadata(hitsmetadatavalue, current_hits);

            if (packet.containsMetadata("thunder-activated")) {
                for (Entity entity : packet.getVictim().getWorld().getNearbyEntities(packet.getVictim().getLocation(), 3, 3, 3)) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) entity;

                        EntityDamageEvent lightningEvent = new EntityDamageEvent(livingEntity, EntityDamageEvent.DamageCause.LIGHTNING, 0.0D);
                        Bukkit.getPluginManager().callEvent(lightningEvent);
                        if (!lightningEvent.isCancelled()) {
                            LivingEntity attacker = packet.getAttacker();

                            CombatUtils.dealTrueDamage(2, attacker, livingEntity);

                            if (attacker.getHealth() > 0.0D) {
                                attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 1.0D));
                            }

                            for(int i = 0; i < 6; ++i) {

                                firework.display(packet.getVictim().getLocation().add(0.0D, (double)i * 0.2D * (double)(6 - i) + 1.5D, 0.0D));
                            }

                            BoundingBox boundingBox1 = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getBoundingBox(e.getDamagePacket().getVictim());
                            BoundingBox boundingBox2 = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getBoundingBox(livingEntity);


                            Location loc1 = e.getDamagePacket().getVictim().getLocation().clone().add(0, boundingBox1.getHeight() * 0.6, 0);

                            Location loc2 = entity.getLocation().clone().add(0, boundingBox2.getHeight() * 0.6, 0);
                            Vector step = loc2.toVector().subtract(loc1.toVector()).normalize().multiply(0.5);

                            double distance = Utility.distance(loc1, loc2);

                            int steps = (int) (distance / 0.5);

                            for (int i = 0; i <= steps; i++) {
                                firework.display(loc1);
                                loc1.add(step);
                            }
                        }
                    }
                }
            } else {
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
    }

}
