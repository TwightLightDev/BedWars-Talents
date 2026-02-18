package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.XSound;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.particles.ParticleCloud;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.ItemBuilder;

public class Mirage extends Skill {

    private String cooldownMeta = "skill.mirage.cooldown";
    private String activeMeta = "skill.mirage.active";

    // Active clones: ownerUUID -> clone location (updated each tick)
    private Map<UUID, Location> activeClones = new HashMap<>();
    private Map<UUID, Integer> cloneEntityIds = new HashMap<>();

    private com.andrei1058.bedwars.api.BedWars.ArenaUtil util = Talents.getInstance().getAPI().getArenaUtil();
    private PlayerManager packetManager = Talents.getInstance().getPacketEventsAPI().getPlayerManager();

    // Particles
    private ParticleDustColored ghostTrail;
    private ParticleDustColored ghostGlow;
    private ParticleCloud smokeCloud;

    private static final long COOLDOWN_MS = 7000L;
    private static final double CLONE_SPEED = 0.35D;
    private static final double DETONATE_RADIUS = 2.5D;
    private static final double HIT_DETECTION_RADIUS = 1.8D;

    public Mirage(String id, List<Integer> costList) {
        super(id, costList);

        ghostTrail = new ParticleDustColored();
        ghostTrail.setColor(new hm.zelha.particlesfx.util.Color(200, 210, 230));
        ghostTrail.setCount(1);
        ghostTrail.setOffset(0, 0, 0);

        ghostGlow = new ParticleDustColored();
        ghostGlow.setColor(new hm.zelha.particlesfx.util.Color(170, 180, 220));
        ghostGlow.setCount(3);
        ghostGlow.setOffset(0.15, 0.4, 0.15);
        ghostGlow.setSpeed(0.01);

        smokeCloud = new ParticleCloud();
        smokeCloud.setCount(30);
        smokeCloud.setOffset(1.0, 0.8, 1.0);
        smokeCloud.setSpeed(0.1);
    }

    // ==================== ACTIVATION via PlayerInteractEvent ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!p.isSneaking()) return;

        // Check if holding a sword
        if (p.getItemInHand() == null) return;
        String itemName = p.getItemInHand().getType().name();
        if (!itemName.contains("SWORD")) return;

        User user = User.getUserFromUUID(p.getUniqueId());
        if (user == null) return;
        if (!user.getActivatingSkills().contains(getSkillId())) return;
        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        if (!User.isPlaying(p)) return;

        // Already active?
        if (activeClones.containsKey(p.getUniqueId())) return;

        // Cooldown
        if (user.hasMetadata(cooldownMeta)) {
            long cd = (Long) user.getMetadataValue(cooldownMeta);
            if (System.currentTimeMillis() < cd) {
                long rem = (cd - System.currentTimeMillis()) / 1000;
                p.sendMessage("§7§l[Mirage] §7Cooldown: §c" + rem + "s");
                return;
            }
        }

        e.setCancelled(true);

        deployClone(p, level, user);
    }

    private void deployClone(Player owner, int level, User user) {
        Location spawnLoc = owner.getLocation().clone();
        Vector direction = owner.getLocation().getDirection().clone();
        direction.setY(0).normalize();

        int entityId = SpigotReflectionUtil.generateEntityId();
        UUID fakeUUID = UUID.randomUUID();

        // Create the armor stand packet clone
        com.github.retrooper.packetevents.protocol.world.Location peLoc =
                new com.github.retrooper.packetevents.protocol.world.Location(
                        spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(),
                        spawnLoc.getYaw(), spawnLoc.getPitch());

        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId, fakeUUID, EntityTypes.ARMOR_STAND, peLoc, spawnLoc.getYaw(), 0, null);

        // Make invisible
        EntityData invisData = new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20);
        WrapperPlayServerEntityMetadata metaPacket = new WrapperPlayServerEntityMetadata(
                entityId, Collections.singletonList(invisData));

        // Copy owner's armor as equipment
        List<Equipment> equips = new ArrayList<>();
        if (owner.getInventory().getHelmet() != null) {
            equips.add(new Equipment(EquipmentSlot.HELMET,
                    SpigotConversionUtil.fromBukkitItemStack(owner.getInventory().getHelmet())));
        }
        if (owner.getInventory().getChestplate() != null) {
            equips.add(new Equipment(EquipmentSlot.CHEST_PLATE,
                    SpigotConversionUtil.fromBukkitItemStack(owner.getInventory().getChestplate())));
        }
        if (owner.getInventory().getLeggings() != null) {
            equips.add(new Equipment(EquipmentSlot.LEGGINGS,
                    SpigotConversionUtil.fromBukkitItemStack(owner.getInventory().getLeggings())));
        }
        if (owner.getInventory().getBoots() != null) {
            equips.add(new Equipment(EquipmentSlot.BOOTS,
                    SpigotConversionUtil.fromBukkitItemStack(owner.getInventory().getBoots())));
        }

        WrapperPlayServerEntityEquipment equipPacket = equips.isEmpty() ? null :
                new WrapperPlayServerEntityEquipment(entityId, equips);
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);

        // Send to all players in world
        List<Player> worldPlayers = spawnLoc.getWorld().getPlayers();
        for (Player p : worldPlayers) {
            packetManager.sendPacket(p, spawnPacket);
            packetManager.sendPacket(p, metaPacket);
            if (equipPacket != null) packetManager.sendPacket(p, equipPacket);
        }

        activeClones.put(owner.getUniqueId(), spawnLoc.clone());
        cloneEntityIds.put(owner.getUniqueId(), entityId);

        owner.playSound(owner.getLocation(), XSound.ENTITY_ILLUSIONER_MIRROR_MOVE.parseSound(), 1.5F, 1.2F);
        owner.sendMessage("§7§l[Mirage] §f✧ Decoy deployed!");

        IArena arena = util.getArenaByPlayer(owner);
        ITeam ownerTeam = arena != null ? arena.getTeam(owner) : null;

        int lifetimeTicks = 40 + level * 3; // 2s to 5s
        double detonateDmg = 2.5D + level * 0.125D; // 1.625 to 4.0
        int blindDuration = 20 + level; // 1-2 seconds

        new BukkitRunnable() {
            int tick = 0;
            Location currentLoc = spawnLoc.clone();
            boolean detonated = false;

            public void run() {
                if (tick >= lifetimeTicks || !owner.isOnline() || detonated) {
                    // Destroy clone
                    for (Player p : worldPlayers) {
                        if (p.isOnline()) packetManager.sendPacket(p, destroyPacket);
                    }

                    activeClones.remove(owner.getUniqueId());
                    cloneEntityIds.remove(owner.getUniqueId());

                    if (!detonated && owner.isOnline()) {
                        // Fade effect
                        ghostGlow.display(currentLoc.clone().add(0, 1.0, 0));
                        owner.sendMessage("§7§l[Mirage] §8Decoy faded.");
                    }

                    user.setMetadata(cooldownMeta, System.currentTimeMillis() + COOLDOWN_MS);
                    cancel();
                    return;
                }

                // Move clone forward
                currentLoc.add(direction.clone().multiply(CLONE_SPEED));
                activeClones.put(owner.getUniqueId(), currentLoc.clone());

                // Update entity position via teleport packet
                com.github.retrooper.packetevents.protocol.world.Location newPeLoc =
                        new com.github.retrooper.packetevents.protocol.world.Location(
                                currentLoc.getX(), currentLoc.getY(), currentLoc.getZ(),
                                spawnLoc.getYaw(), 0);
                WrapperPlayServerEntityTeleport teleportPacket =
                        new WrapperPlayServerEntityTeleport(entityId, newPeLoc, false);
                for (Player p : worldPlayers) {
                    if (p.isOnline()) packetManager.sendPacket(p, teleportPacket);
                }

                // Ghost trail
                ghostTrail.display(currentLoc.clone().add(0, 0.5, 0));
                if (tick % 3 == 0) {
                    ghostGlow.display(currentLoc.clone().add(0, 1.0, 0));
                }

                // Check for enemy hits (enemy player near clone)
                if (tick % 2 == 0) {
                    for (Entity entity : currentLoc.getWorld().getNearbyEntities(
                            currentLoc, HIT_DETECTION_RADIUS, 2.0, HIT_DETECTION_RADIUS)) {
                        if (!(entity instanceof Player)) continue;
                        if (entity.getUniqueId().equals(owner.getUniqueId())) continue;

                        Player target = (Player) entity;
                        User targetUser = User.getUserFromUUID(target.getUniqueId());
                        if (targetUser == null) continue;

                        IArena tArena = util.getArenaByPlayer(target);
                        if (tArena == null) continue;
                        if (ownerTeam != null && tArena.getTeam(target) == ownerTeam) continue;

                        // Check if enemy is swinging (attacking) near the clone
                        // We detect via velocity change / recent attack — simple proximity trigger
                        // Only trigger if enemy is within very close range and facing the clone
                        double dist = target.getLocation().distance(currentLoc);
                        if (dist > HIT_DETECTION_RADIUS) continue;

                        Vector toClone = currentLoc.toVector().subtract(target.getLocation().toVector()).normalize();
                        double dot = target.getLocation().getDirection().normalize().dot(toClone);
                        if (dot < 0.3) continue; // Must be roughly facing the clone

                        // DETONATE
                        detonated = true;
                        detonateClone(owner, currentLoc, detonateDmg, blindDuration, ownerTeam);
                        break;
                    }
                }

                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);
    }

    private void detonateClone(Player owner, Location loc, double damage, int blindDuration, ITeam ownerTeam) {
        // Visual explosion
        smokeCloud.display(loc.clone().add(0, 1.0, 0));

        // Purple flash ring
        ParticleDustColored flashPurple = new ParticleDustColored();
        flashPurple.setColor(new hm.zelha.particlesfx.util.Color(160, 50, 200));
        flashPurple.setCount(20);
        flashPurple.setOffset(1.5, 0.8, 1.5);
        flashPurple.setSpeed(0.5);
        flashPurple.display(loc.clone().add(0, 1.0, 0));

        // Expanding ring animation
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick >= 6) { cancel(); return; }
                double r = 0.5D + tick * 0.5D;
                for (int i = 0; i < 12; i++) {
                    double angle = Math.toRadians(30.0 * i + tick * 20);
                    ghostTrail.display(loc.clone().add(Math.cos(angle) * r, 0.3, Math.sin(angle) * r));
                }
                tick++;
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 1L);

        // Sound
        loc.getWorld().playSound(loc, XSound.ENTITY_GENERIC_EXPLODE.parseSound(), 1.5F, 1.5F);
        loc.getWorld().playSound(loc, XSound.ENTITY_ILLUSIONER_CAST_SPELL.parseSound(), 2.0F, 0.5F);

        owner.sendMessage("§7§l[Mirage] §d✦ Decoy detonated!");

        // Damage + blind enemies in radius
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, DETONATE_RADIUS, DETONATE_RADIUS, DETONATE_RADIUS)) {
            if (!(entity instanceof Player)) continue;
            if (entity.getUniqueId().equals(owner.getUniqueId())) continue;

            Player target = (Player) entity;
            IArena tArena = util.getArenaByPlayer(target);
            if (tArena == null) continue;
            if (ownerTeam != null && tArena.getTeam(target) == ownerTeam) continue;

            CombatUtils.dealUndefinedDamage(target, damage,
                    EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                    Map.of("mirage-detonate", true),
                    Set.of("armorLayer"));

            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.BLINDNESS, blindDuration, 0, false, false));

            target.sendMessage("§7§l[Mirage] §cYou hit a decoy! §8Blinded...");
        }
    }
}

