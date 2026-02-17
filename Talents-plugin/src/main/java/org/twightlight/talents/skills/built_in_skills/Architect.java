package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import hm.zelha.particlesfx.particles.ParticleHappy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;

public class Architect extends Skill implements Listener {

    private String blocksPlacedMetadataValue = "skill.architect.blocksPlaced";
    private String lastUpdateMetadataValue = "skill.architect.lastUpdate";
    private String shieldActiveMetadataValue = "skill.architect.shieldActive";
    private String shieldCooldownMetadataValue = "skill.architect.shieldCooldown";

    private Map<UUID, BukkitTask> decayTasks = new HashMap<>();
    private Map<UUID, Integer> lastStatValues = new HashMap<>();

    private ParticleHappy buildParticle;
    private ParticleDustColored shieldParticle;

    // Block placement tracking
    private static final int BLOCKS_PER_TIER = 50; // 50 blocks = 1 tier
    private static final int MAX_TIERS = 4; // Maximum 10 tiers (500 blocks)
    private static final long DECAY_TIME = 200L; // 10 seconds without placing = start decay
    private static final long SHIELD_COOLDOWN = 600L; // 30 seconds cooldown

    public Architect(String id, List<Integer> costList) {
        super(id, costList);

        buildParticle = new ParticleHappy();
        buildParticle.setCount(3);
        buildParticle.setOffset(0.3, 0.3, 0.3);
        buildParticle.setSpeed(0);

        shieldParticle = new ParticleDustColored();
        shieldParticle.setColor(hm.zelha.particlesfx.util.Color.AQUA);
        shieldParticle.setCount(15);
        shieldParticle.setOffset(0.5, 1, 0.5);
        shieldParticle.setSpeed(0.5);

        Bukkit.getPluginManager().registerEvents(this, Talents.getInstance());
    }

    // ==================== BLOCK PLACEMENT TRACKING ====================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        User user = User.getUserFromUUID(p.getUniqueId());

        if (user == null || !user.getActivatingSkills().contains(getSkillId())) {
            return;
        }

        int level = user.getSkillLevel(getSkillId());
        if (level == 0) return;

        Block block = e.getBlockPlaced();

        // Only count solid building blocks (wool, wood, stone, etc.)
        if (!isValidBuildingBlock(block.getType())) {
            return;
        }

        // Initialize metadata
        if (!user.hasMetadata(blocksPlacedMetadataValue) ||
                !(user.getMetadataValue(blocksPlacedMetadataValue) instanceof Integer)) {
            user.setMetadata(blocksPlacedMetadataValue, 0);
        }

        int blocksPlaced = (Integer) user.getMetadataValue(blocksPlacedMetadataValue);
        int previousTier = blocksPlaced / BLOCKS_PER_TIER;

        // Increment blocks placed
        blocksPlaced++;
        p.playSound(p.getLocation(), XSound.BLOCK_WOOL_PLACE.parseSound(), 5.0F, 3.0F);
        int newTier = Math.min(blocksPlaced / BLOCKS_PER_TIER, MAX_TIERS);

        user.setMetadata(blocksPlacedMetadataValue, blocksPlaced);
        user.setMetadata(lastUpdateMetadataValue, System.currentTimeMillis());

        // Update stats
        updateArchitectStats(p, user, level, newTier);

        // Visual feedback on tier up
        if (newTier > previousTier) {
            buildParticle.display(block.getLocation().add(0.5, 0.5, 0.5));
            p.playSound(p.getLocation(), XSound.BLOCK_ANVIL_USE.parseSound(), 1.0F, 1.5F + (newTier * 0.1F));
            p.sendMessage("§6§l[Architect] §eTier " + newTier + " reached! §7(" + blocksPlaced + "/" + (MAX_TIERS * BLOCKS_PER_TIER) + " blocks)");
            p.playSound(p.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 5.0F, 3.0F);

        } else if (blocksPlaced % 10 == 0) {
            // Progress feedback every 10 blocks
            buildParticle.display(block.getLocation().add(0.5, 0.5, 0.5));
            p.playSound(p.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 0.5F, 2.0F);
        }

        // Cancel previous decay task
        if (decayTasks.containsKey(p.getUniqueId())) {
            decayTasks.get(p.getUniqueId()).cancel();
        }

        // Schedule new decay task
        BukkitTask decayTask = Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            startDecay(p, user, level);
        }, DECAY_TIME);

        decayTasks.put(p.getUniqueId(), decayTask);

        // Check for shield activation (Tier 5+ feature)
        if (newTier >= 3 && level >= 10) {
            checkShieldActivation(p, user, level);
        }
    }

    private boolean isValidBuildingBlock(Material material) {


        // Only count typical building blocks
        switch (material) {
            case WOOL:
            case STAINED_CLAY:
            case HARD_CLAY:
            case WOOD:
            case LOG:
            case STONE:
            case COBBLESTONE:
            case SANDSTONE:
            case GLASS:
            case OBSIDIAN:
            case ENDER_STONE:
                return true;
            default:
                return false;
        }
    }

    // ==================== STATS MANAGEMENT ====================

    private void updateArchitectStats(Player p, User user, int level, int tier) {
        IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p);
        if (arena == null) return;

        Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
        StatsMap statsMap = arena1.getStatsMapOfUUID(user.getUuid());
        if (statsMap == null) return;

        // Remove old stats if they exist
        Integer oldTier = lastStatValues.get(p.getUniqueId());
        if (oldTier != null && oldTier > 0) {
            removeArchitectStats(statsMap, level, oldTier);
        }

        // Apply new stats
        if (tier > 0) {
            applyArchitectStats(statsMap, level, tier);
            lastStatValues.put(p.getUniqueId(), tier);
        }
    }

    private void applyArchitectStats(StatsMap statsMap, int level, int tier) {
        // Stats per tier (gets stronger with level)
        double damageReduction = 0.2 * level * tier;
        double blockChance = 0.125 * level * tier;
        double blockPower = 0.125 * level * tier;

        statsMap.getStatContainer("DAMAGE_REDUCTION").add(damageReduction);
        statsMap.getStatContainer("BLOCK_CHANCE").add(blockChance);
        statsMap.getStatContainer("BLOCK_POWER").add(blockPower);
    }

    private void removeArchitectStats(StatsMap statsMap, int level, int tier) {
        double damageReduction = 1.0 * level * tier;
        double blockChance = 0.5 * level * tier;
        double blockPower = 0.1 * level * tier;
        double knockbackResistance = 2.0 * level * tier;

        statsMap.getStatContainer("DAMAGE_REDUCTION").subtract(damageReduction);
        statsMap.getStatContainer("BLOCK_CHANCE").subtract(blockChance);
        statsMap.getStatContainer("BLOCK_POWER").subtract(blockPower);
        statsMap.getStatContainer("KNOCKBACK_RESISTANCE").subtract(knockbackResistance);
    }

    // ==================== DECAY SYSTEM ====================

    private void startDecay(Player p, User user, int level) {
        if (!p.isOnline()) return;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Talents.getInstance(), new Runnable() {
            @Override
            public void run() {
                User currentUser = User.getUserFromUUID(p.getUniqueId());
                if (currentUser == null || !p.isOnline()) {
                    this.cancel();
                    return;
                }

                if (!currentUser.hasMetadata(blocksPlacedMetadataValue)) {
                    this.cancel();
                    return;
                }

                int blocksPlaced = (Integer) currentUser.getMetadataValue(blocksPlacedMetadataValue);
                long lastUpdate = currentUser.hasMetadata(lastUpdateMetadataValue) ?
                        (Long) currentUser.getMetadataValue(lastUpdateMetadataValue) : 0L;

                // Check if player has placed blocks recently
                if (System.currentTimeMillis() - lastUpdate < DECAY_TIME * 50) {
                    // Still building, cancel decay
                    this.cancel();
                    return;
                }

                if (blocksPlaced <= 0) {
                    this.cancel();
                    return;
                }

                // Decay: lose 5 blocks per second
                blocksPlaced = Math.max(0, blocksPlaced - 5);
                p.sendMessage("§6§l[Architect] §cBuilding stacks -5!");
                int newTier = blocksPlaced / BLOCKS_PER_TIER;

                currentUser.setMetadata(blocksPlacedMetadataValue, blocksPlaced);
                updateArchitectStats(p, currentUser, level, newTier);

                if (blocksPlaced == 0) {
                    p.sendMessage("§6§l[Architect] §cAll building stacks lost!");
                    p.playSound(p.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 0.5F);
                    this.cancel();
                }
            }

            private void cancel() {
                if (decayTasks.containsKey(p.getUniqueId())) {
                    decayTasks.get(p.getUniqueId()).cancel();
                    decayTasks.remove(p.getUniqueId());
                }
            }
        }, 20L, 20L);

        decayTasks.put(p.getUniqueId(), task);
    }

    // ==================== BUILDER'S SHIELD (Special Feature) ====================

    private void checkShieldActivation(Player p, User user, int level) {
        // Check cooldown
        if (user.hasMetadata(shieldCooldownMetadataValue)) {
            long cooldownEnd = (Long) user.getMetadataValue(shieldCooldownMetadataValue);
            if (System.currentTimeMillis() < cooldownEnd) {
                return; // Still on cooldown
            }
        }

        // Check if already active
        if (user.hasMetadata(shieldActiveMetadataValue) &&
                (Boolean) user.getMetadataValue(shieldActiveMetadataValue)) {
            return;
        }

        // Activate shield
        user.setMetadata(shieldActiveMetadataValue, true);

        IArena arena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p);
        Arena arena1 = Talents.getInstance().getArenaManager().getArenaFromIArena(arena);
        StatsMap statsMap = arena1.getStatsMapOfUUID(user.getUuid());

        if (statsMap != null) {
            // Temporary massive defensive buff
            double shieldDR = 1.6 * level; // 20% DR per level for 5 seconds
            statsMap.getStatContainer("DAMAGE_REDUCTION").add(shieldDR);

            p.sendMessage("§6§l[Architect] §b§lBuilder's Shield activated!");
            p.playSound(p.getLocation(), XSound.BLOCK_ENCHANTMENT_TABLE_USE.parseSound(), 1.0F, 2.0F);

            // Visual effect
            playShieldEffect(p);

            // Remove after 5 seconds
            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                statsMap.getStatContainer("DAMAGE_REDUCTION").subtract(shieldDR);
                user.setMetadata(shieldActiveMetadataValue, false);
                user.setMetadata(shieldCooldownMetadataValue, System.currentTimeMillis() + (SHIELD_COOLDOWN * 50));

                p.sendMessage("§6§l[Architect] §7Builder's Shield expired.");
            }, 100L);
        }
    }

    private void playShieldEffect(Player p) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!p.isOnline() || ticks++ >= 5) {
                    cancel();
                    return;
                }

                shieldParticle.display(p.getLocation().add(0, 1, 0));
            }
        }.runTaskTimer(Talents.getInstance(), 0L, 20L);
    }

    // ==================== DAMAGE EVENTS (for visual feedback) ====================

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.MONITOR)
    public void onMeleeDamage(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());

        if (user != null && user.getActivatingSkills().contains(getSkillId())) {
            if (user.hasMetadata(blocksPlacedMetadataValue)) {
                int blocks = (Integer) user.getMetadataValue(blocksPlacedMetadataValue);
                if (blocks > 0) {
                    buildParticle.display(victim.getLocation().add(0, 1, 0));
                }
            }
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.MONITOR)
    public void onRangedDamage(RangedDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamagePacket().getVictim() instanceof Player)) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());

        if (user != null && user.getActivatingSkills().contains(getSkillId())) {
            if (user.hasMetadata(blocksPlacedMetadataValue)) {
                int blocks = (Integer) user.getMetadataValue(blocksPlacedMetadataValue);
                if (blocks > 0) {
                    buildParticle.display(victim.getLocation().add(0, 1, 0));
                }
            }
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.MONITOR)
    public void onUndefinedDamage(UndefinedDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamagePacket().getVictim() == null) return;

        Player victim = e.getDamagePacket().getVictimAsPlayer();
        User user = User.getUserFromUUID(victim.getUniqueId());

        if (user != null && user.getActivatingSkills().contains(getSkillId())) {
            if (user.hasMetadata(blocksPlacedMetadataValue)) {
                int blocks = (Integer) user.getMetadataValue(blocksPlacedMetadataValue);
                if (blocks > 0) {
                    buildParticle.display(victim.getLocation().add(0, 1, 0));
                }
            }
        }
    }
}

