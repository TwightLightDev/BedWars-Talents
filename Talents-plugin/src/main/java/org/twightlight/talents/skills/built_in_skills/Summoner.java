package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.entity.Despawnable;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleFlame;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.nmsbridge.abstracts.objects.BoundingBox;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.ItemBuilder;
import org.twightlight.talents.utils.Utility;

public class Summoner extends Skill {

    private String soulMonstersMetadataValue = "skill.summoner.soulMonsters";
    private static List<Equipment> equipments;
    private PlayerManager manager = Talents.getInstance().getPacketEventsAPI().getPlayerManager();
    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();

    private ParticleFlame bullet;

    public Summoner(String id, List<Integer> costList) {
        super(id, costList);
        bullet = new ParticleFlame(2);
        bullet.setOffset(0, 0, 0);
        bullet.setSpeed(0);
        bullet.setVelocity(new Vector(0, 0, 0));
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
            if (!user.hasMetadata(soulMonstersMetadataValue) || !(user.getMetadataValue(soulMonstersMetadataValue) instanceof Integer)) {
                user.setMetadata(soulMonstersMetadataValue, 0);
            }

            int soulMonsters = (Integer) user.getMetadataValue(soulMonstersMetadataValue);

            if (Utility.rollChance(1.25 * level) && soulMonsters < 2) {
                p.getWorld().playSound(p.getLocation(), XSound.ENTITY_ZOMBIE_AMBIENT.parseSound(), 5.0F, 2.0F);
                summonSoulMonster(p, level, p.getLocation(), user);
                soulMonsters++;
                user.setMetadata(soulMonstersMetadataValue, soulMonsters);
            }
        }
    }


    public void summonSoulMonster(final Player owner, final int level, final Location location, final User ownerUser) {
        UUID uuid = UUID.randomUUID();
        int entityId = SpigotReflectionUtil.generateEntityId();
        com.github.retrooper.packetevents.protocol.world.Location location1 = new com.github.retrooper.packetevents.protocol.world.Location(
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        WrapperPlayServerSpawnEntity standPacket = new WrapperPlayServerSpawnEntity(
                entityId, uuid, EntityTypes.ARMOR_STAND, location1, location.getYaw(), 0, null);
        WrapperPlayServerEntityEquipment entityEquipment = new WrapperPlayServerEntityEquipment(entityId, equipments);
        EntityData entityData = new EntityData(0, EntityDataTypes.BYTE, (byte) 32);
        WrapperPlayServerEntityMetadata entityMetadata = new WrapperPlayServerEntityMetadata(
                entityId, Collections.singletonList(entityData));
        final WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(entityId);
        final List<Player> players = location.getWorld().getPlayers();

        for (Player player : players) {
            manager.sendPacket(player, standPacket);
            manager.sendPacket(player, entityMetadata);
            manager.sendPacket(player, entityEquipment);
        }

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick < 60 && owner.isOnline() && User.getUserFromUUID(owner.getUniqueId()) != null) {
                    double radius = 4.0D + (double) (level - (level == 20 ? 0 : 1)) * 0.1D;
                    ParticleDustColored ring1 = new ParticleDustColored(1);

                    ring1.setColor(hm.zelha.particlesfx.util.Color.YELLOW);
                    ring1.setOffset(0, 0, 0);

                    ParticleDustColored ring2 = new ParticleDustColored(1);

                    ring2.setColor(hm.zelha.particlesfx.util.Color.RED);
                    ring2.setOffset(0, 0, 0);

                    ParticleDustColored ring3 = new ParticleDustColored(20);

                    ring3.setColor(hm.zelha.particlesfx.util.Color.BLACK);
                    ring3.setOffset(0.5, 0.2, 0.5);

                    for (int i = 0; i < 36; i++) {
                        double angle = Math.toRadians(10 * i);
                        Location loc = location.clone().add(radius * Math.cos(angle), 0.0D, radius * Math.sin(angle));
                        Location loc1 = location.clone().add(radius * 0.35D * Math.cos(angle), 0.0D, radius * 0.35D * Math.sin(angle));
                        ring1.display(loc);
                        ring2.display(loc1);
                    }

                    ring3.display(location.clone().add(0.0D, 0.8D, 0.0D));

                    IArena arena = util.getArenaByPlayer(owner);
                    ITeam ownerTeam = arena.getTeam(owner);

                    for (Entity entity : location.getWorld().getNearbyEntities(location, radius, 3.0D, radius)) {
                        if (!(entity instanceof LivingEntity)) continue;
                        if (BedWars.nms.isDespawnable(entity) &&
                                !((Despawnable) BedWars.nms.getDespawnablesList().get(entity.getUniqueId())).getTeam().equals(ownerTeam)) {
                            if (location.distance(entity.getLocation()) < 3) {
                                CombatUtils.dealMeleeDamage(owner, (LivingEntity) entity, level * 0.25, Map.of("additional-attack", true), Set.of("damageLayer1"));
                            } else if (location.distance(entity.getLocation()) < radius) {
                                rangedAttack(owner, ((Despawnable) entity).getEntity(), level, location, radius);
                            }
                        } else if (entity instanceof Player && entity != owner) {
                            Player targetPlayer = (Player) entity;
                            User targetUser = User.getUserFromUUID(targetPlayer.getUniqueId());
                            if (targetUser != null && util.getArenaByPlayer(targetPlayer).getTeam(targetPlayer) != ownerTeam) {
                                if (location.distance(entity.getLocation()) < 3) {
                                    CombatUtils.dealMeleeDamage(owner, (LivingEntity) entity, level * 0.25, Map.of("additional-attack", true), Set.of("damageLayer1"));
                                } else if (location.distance(entity.getLocation()) < radius) {
                                    rangedAttack(owner, targetPlayer, level, location, radius);
                                    targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 1, false, false));
                                }
                            }
                        }
                    }

                    tick += 20;
                } else {
                    for (Player player : players) {
                        if (player.isOnline()) {
                            manager.sendPacket(player, destroyEntities);
                        }
                    }

                    int currentCount = (Integer) ownerUser.getMetadataValue(soulMonstersMetadataValue);
                    ownerUser.setMetadata(soulMonstersMetadataValue, currentCount - 1);
                    cancel();
                }
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 20L);
    }


    public void rangedAttack(final Player owner, final LivingEntity target, final int level,
                             final Location baseLoc, final double radius) {


        new BukkitRunnable() {
            Location lastLoc = baseLoc.clone().add(0.0D, 1.5D, 0.0D);

            public void run() {
                if (owner.isOnline() && Utility.distance(baseLoc, lastLoc) <= radius) {
                    BoundingBox boundingBox = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getBoundingBox(target);
                    Location targetLoc = target.getLocation().add(0.0D, boundingBox.getHeight() * 0.8D, 0.0D);
                    Vector vector = targetLoc.toVector().subtract(lastLoc.toVector()).normalize();
                    lastLoc.add(vector.multiply(0.5D));

                    for (Entity nearbyPlayer : baseLoc.getWorld().getNearbyEntities(lastLoc, 1.0D, 2.0D, 1.0D).stream().filter(entity -> entity instanceof LivingEntity).collect(Collectors.toList())) {
                        Arrow arrow = (Arrow) baseLoc.getWorld().spawn(baseLoc.clone().add(0.0D, 100.0D, 0.0D), Arrow.class);
                        arrow.setShooter(owner);
                        CombatUtils.dealRangedDamage(arrow, (LivingEntity) nearbyPlayer, level * 0.2, new HashMap<>(), Set.of("damageLayer1"));
                        arrow.remove();
                        target.getWorld().playSound(target.getLocation().clone(), XSound.ENTITY_ARROW_HIT_PLAYER.parseSound(), 10.0F, 3.0F);
                        cancel();
                        return;
                    }

                    bullet.display(lastLoc, lastLoc);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 2L);
    }

    static {
        equipments = Arrays.asList(
                new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(
                        new ItemBuilder(XMaterial.PLAYER_HEAD)
                                .setDurability((byte) 3)
                                .setSkullOwnerNMS(new ItemBuilder.SkullData(
                                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjExMDlkOWM2Nzg2Yzc0N2M4N2ZjMzZjOWEyNzFkNTFmZjIwMTYwM2MzOGQyNjE1YWM0ODAzOTA4NjgwZTk4OCJ9fX0=",
                                        ItemBuilder.SkullDataType.TEXTURE))
                                .toItemStack())),
                new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(
                        new ItemBuilder(XMaterial.LEATHER_CHESTPLATE)
                                .setLeatherArmorColor(Color.AQUA)
                                .toItemStack()))
        );
    }
}

