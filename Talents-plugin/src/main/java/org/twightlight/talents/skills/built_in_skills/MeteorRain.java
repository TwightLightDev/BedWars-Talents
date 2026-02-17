package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.entity.Despawnable;
import com.cryptomorin.xseries.XSound;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class MeteorRain extends Skill {

    private String hitsmetadatavalue = "skill.meteorRain.hits";
    private String taskmetadatavalue = "skill.meteorRain.task";
    private String damagelayer = "meteorRainLayer";
    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    public MeteorRain(String id, List<Integer> costList) {
        super(id, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        int level = user.getSkillLevel(getSkillId());
        if (level != 0 && !MysticalStand.isExtraAttack(e.getDamagePacket())) {
            if (!user.hasMetadata(hitsmetadatavalue) || !(user.getMetadataValue(hitsmetadatavalue) instanceof Integer)) {
                user.setMetadata(hitsmetadatavalue, 0);
            }
            if (user.getMetadataValue(taskmetadatavalue) instanceof BukkitTask) {
                ((BukkitTask) user.getMetadataValue(taskmetadatavalue)).cancel();
            }
            int current_hits = (Integer) user.getMetadataValue(hitsmetadatavalue);

            current_hits++;

            user.setMetadata(hitsmetadatavalue, current_hits);
            user.setMetadata(taskmetadatavalue, Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                user.setMetadata(hitsmetadatavalue, 0);
            }, 80L));

            if (current_hits >= 3) {
                ((BukkitTask) user.getMetadataValue(taskmetadatavalue)).cancel();
                user.setMetadata(hitsmetadatavalue, current_hits-3);
                user.setMetadata(taskmetadatavalue, Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    user.setMetadata(hitsmetadatavalue, 0);
                }, 80L));
                playMeteorRain(p, e.getDamagePacket().getVictim());
                double ratio = (e.getDamagePacket().getAttackerStatsMap().getStatContainer("ADDITIONAL_ATTACK_DAMAGE").get() + level)/100;
                DamageProperty property = e.getDamagePacket().getDamageProperty();
                double multiple = 0.04D + 0.004D * (double) (level - (level == 20 ? 0 : 1));
                double damage = e.getDamagePacket().getDamageProperty().getBaseDamage() * ratio;
                for (Entity entity : e.getDamagePacket().getVictim().getWorld().getNearbyEntities(e.getDamagePacket().getVictim().getLocation(), 1.5D, 1.5D, 1.5D)) {
                    if (entity instanceof Player && User.getUserFromBukkitPlayer((Player) entity) != null) {
                        Player victim = (Player) entity;
                        if (util.getArenaByPlayer(victim).getTeam(victim) != util.getArenaByPlayer(p).getTeam(p)) {

                            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                                CombatUtils.dealMeleeDamage(p, victim, damage, Map.of("additional-attack", true), Set.of("damageLayer1"));
                                victim.setFireTicks(40);
                            }, 10L);

                            if (victim.getHealth()/victim.getMaxHealth() <= 0.1 + level * 0.015 && Utility.rollChance(30)) {
                                Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                                    org.twightlight.talents.utils.CombatUtils.dealMeleeDamage(p, victim, damage, Map.of("additional-attack", true), Set.of("damageLayer1"));
                                }, 30L);
                            }
                        }
                        if (!property.hasLayer(damagelayer)) {
                            property.addLayer(damagelayer, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                            property.addValueToLayer(damagelayer, 1);
                        }
                        property.addValueToLayer(damagelayer, multiple);
                    } else if (BedWars.nms.isDespawnable(entity) && entity instanceof LivingEntity && (BedWars.nms.getDespawnablesList().get(entity.getUniqueId())).getTeam() != util.getArenaByPlayer(p).getTeam(p)) {
                        entity.setFireTicks(60);
                        ((LivingEntity) entity).setHealth(Math.max(((LivingEntity) entity).getMaxHealth() - 8.0D, 1.0D));
                        if (!property.hasLayer(damagelayer)) {
                            property.addLayer(damagelayer, LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                            property.addValueToLayer(damagelayer, 1);
                        }
                        property.addValueToLayer(damagelayer, multiple);
                    }
                }
            }
        }
    }

    private void playMeteorRain(final Entity entity, Entity target) {
        double radius = 1.5D;
        double height = 3.0D;
        final Location locE = entity.getLocation();
        final Location locT = target.getLocation();
        final Vector dir = locT.toVector().subtract(locE.toVector()).normalize();
        final Vector up = new Vector(0, 1, 0);
        final Vector right = dir.clone().crossProduct(up).normalize();
        (new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (this.i > 6 || entity instanceof Player && !((Player) entity).isOnline()) {
                    this.cancel();
                } else {
                    double angle = Math.toRadians((this.i * 60));
                    Location sunCirclePoint = locE.clone().add(up.clone().multiply(Math.cos(angle) * radius)).add(right.clone().multiply(Math.sin(angle) * radius)).add(0.0D, height, 0.0D);
                    Location flatCirclePoint = locT.clone().add(dir.clone().multiply(Math.cos(3.141592653589793D - angle) * radius)).add(right.clone().multiply(Math.sin(3.141592653589793D - angle) * radius));
                    Vector meteor = flatCirclePoint.toVector().subtract(sunCirclePoint.toVector()).normalize();
                    sunCirclePoint.setDirection(meteor);
                    com.github.retrooper.packetevents.protocol.world.Location meteorLoc = new com.github.retrooper.packetevents.protocol.world.Location(sunCirclePoint.getX(), sunCirclePoint.getY(), sunCirclePoint.getZ(), sunCirclePoint.getYaw(), sunCirclePoint.getPitch());
                    Vector3d velocity = (new Vector3d(meteor.getX(), meteor.getY(), meteor.getZ())).multiply(3.0D);
                    UUID uuid = UUID.randomUUID();
                    int entityId = SpigotReflectionUtil.generateEntityId();
                    WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(entityId, uuid, EntityTypes.SMALL_FIREBALL, meteorLoc, meteorLoc.getYaw(), 0, velocity);
                    WrapperPlayServerEntityVelocity velPacket = new WrapperPlayServerEntityVelocity(entityId, velocity);
                    List<Player> players = entity.getWorld().getPlayers();
                    entity.getWorld().playSound(flatCirclePoint, XSound.BLOCK_FIRE_AMBIENT.parseSound(), 5.0F, 3.0F);

                    for (Player p : players) {
                        Talents.getInstance().getPacketEventsAPI().getPlayerManager().sendPacket(p, packet);
                        Talents.getInstance().getPacketEventsAPI().getPlayerManager().sendPacket(p, velPacket);
                    }

                    Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                        WrapperPlayServerDestroyEntities packet1 = new WrapperPlayServerDestroyEntities(entityId);

                        for (Player p : players) {
                            Talents.getInstance().getPacketEventsAPI().getPlayerManager().sendPacket(p, packet1);
                        }

                    }, 15L);
                    ++this.i;
                }
            }
        }).runTaskTimer(Talents.getInstance(), 0L, 2L);
    }
}
