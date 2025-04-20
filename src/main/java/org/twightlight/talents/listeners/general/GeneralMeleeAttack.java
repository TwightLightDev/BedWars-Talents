package org.twightlight.talents.listeners.general;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.DebugService;

import java.util.Arrays;
import java.util.List;

public class GeneralMeleeAttack implements Listener {
    private static List<String> DAMAGER_TALENTS_DAMAGE = Arrays.asList("MAD", "IMD", "FA", "MSL", "TH", "CH", "AP", "MAS");
    private static List<String> DAMAGER_TALENTS_AA = Arrays.asList("AMA");
    private static List<String> DAMAGER_TALENTS_LIFESTEAL = Arrays.asList("MLS");
    private static List<String> VICTIM_TALENTS_DEFEND = Arrays.asList("DR", "ABS", "BL", "RFL");
    private static List<String> VICTIM_TALENTS_LS_REDUCTION = Arrays.asList("THR");
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMeleeAttack(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getEntity() instanceof Player) {
            if (ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getEntity()) == null) {
                return;
            }
            org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getEntity());
            if (!player.isVulnerable()) {
                return;
            }
            if (!player.isPlaying()) {
                return;
            }
        }
        if (e.getDamager() instanceof Player) {
            long startTime = System.nanoTime();
            Player attacker = (Player) e.getDamager();
            if (attacker.getFallDistance() > 0 &&
                    !attacker.isOnGround() &&
                    !attacker.isInsideVehicle() &&
                    !attacker.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                    !attacker.getLocation().getBlock().isLiquid() &&
                    !attacker.getLocation().getBlock().getType().name().contains("LADDER")) {
                DebugService.debugMsg("Detected a critical hit, trying to disable. Old damage: " + e.getDamage());
                e.setDamage(e.getDamage() / 1.5);
                DebugService.debugMsg("Modified! New damage: " + e.getDamage());
            }
            double base_damage = e.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
            Entity victim = e.getEntity();
            double final_damage = calculateFinalDamage(e.getDamage(), e);
            if (victim instanceof Player) {
                if (((Player) victim).isBlocking()) {
                    DebugService.debugMsg("Detected a block, trying to disable. Old damage: " + final_damage);
                    final_damage = final_damage * 2;
                    DebugService.debugMsg("Modified! New damage: " + final_damage);
                }
                final_damage = calculateReduction(final_damage, e);
            }

            e.setDamage(final_damage);
            DebugService.debugMsg("Final damage: " + e.getFinalDamage());

            double final_lifesteal = calculateLifeSteal(e.getFinalDamage(), e);
            if (victim instanceof Player) {
                final_lifesteal = calculateLSReduction(final_lifesteal, e);
            }
            if (!e.isCancelled() && !attacker.isDead() && attacker.getHealth() > 0 && final_lifesteal > 0 && attacker.getHealth() + final_lifesteal <= attacker.getMaxHealth()) {
                attacker.setHealth(attacker.getHealth() + final_lifesteal);
                DebugService.debugMsg("Healed attacker by: " + final_lifesteal);
            }
            long endTime = System.nanoTime();
            attacker.sendMessage("Estimated time (ms): " + ((startTime - endTime) / 1_000_000));
            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () ->
            {
                if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    calculateAddtitionalAttack(base_damage, e);
                }
            }, 10);
        } else if (!(e.getDamager() instanceof Player) && (e.getEntity() instanceof Player) && !(e.getDamager() instanceof Arrow)) {
            Entity victim = e.getEntity();
            double final_damage = e.getDamage();
            if (((Player) victim).isBlocking()) {
                DebugService.debugMsg("Detected a block, trying to disable. Old damage: " + final_damage);
                final_damage = final_damage * 2;
                DebugService.debugMsg("Modified! New damage: " + final_damage);
            }
            final_damage = calculateReduction(final_damage, e);
            e.setDamage(final_damage);
        }
    }

    private double calculateFinalDamage(double base_value, EntityDamageByEntityEvent e) {
        DebugService.debugMsg("Started calculate damage, starting value: " + base_value);
        if (ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getDamager()) != null) {
            Double total_addition_value = ConversionUtil.getPlayerFromBukkitPlayer((Player) e.getDamager()).getAttribute(PlayerAttribute.MELEE_DAMAGE);
            base_value += total_addition_value;
            DebugService.debugMsg("Attribute modified, new value: " + base_value);
        }
        DebugService.debugMsg("Modify value to: " + base_value);
        for (String id : DAMAGER_TALENTS_DAMAGE) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Melee.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    (Player) e.getDamager(),
                                    TalentsCategory.Melee,
                                    id)
                            , Arrays.asList(base_value, e));
            if (returned instanceof Double) {
                base_value = (Double) returned;
            }
        }
        Object returned = Talents.getInstance().getTalentsManagerService().handle((Player) e.getDamager(), "KOBW", TalentsCategory.Special, Arrays.asList(base_value, 1));
        if (returned instanceof Double) {
            base_value = (Double) returned;
        }
        DebugService.debugMsg("Finished calculation, final value: " + base_value);
        return base_value;
    }

    private double calculateAddtitionalAttack(double base_value, EntityDamageByEntityEvent e) {
        DebugService.debugMsg("Started rolling for additional attack, starting value: " + base_value);
        for (String id : DAMAGER_TALENTS_AA) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Melee.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    (Player) e.getDamager(),
                                    TalentsCategory.Melee,
                                    id)
                            , Arrays.asList(base_value, e));
            if (returned instanceof Double) {
                base_value = (Double) returned;
            }
        }
        return base_value;
    }

    private double calculateLifeSteal(double base_value, EntityDamageByEntityEvent e) {
        DebugService.debugMsg("Started calculate lifesteal amount, starting value: " + base_value);
        for (String id : DAMAGER_TALENTS_LIFESTEAL) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Melee.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    (Player) e.getDamager(),
                                    TalentsCategory.Melee,
                                    id)
                            , Arrays.asList(base_value, e, null, TalentsCategory.Melee));
            if (returned instanceof Double) {
                base_value = (Double) returned;
            }
        }
        DebugService.debugMsg("Finished calculation, final value: " + base_value);
        return base_value;
    }

    private double calculateReduction(double base_value, EntityDamageByEntityEvent e) {
        DebugService.debugMsg("Started calculate reduction amount, starting value: " + base_value);
        for (String id : VICTIM_TALENTS_DEFEND) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Protective.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    (Player) e.getEntity(),
                                    TalentsCategory.Protective,
                                    id)
                            , Arrays.asList(base_value, e));
            if (returned instanceof Double) {
                base_value = (Double) returned;
            }
        }
        Object returned = Talents.getInstance().getTalentsManagerService().handle((Player) e.getEntity(), "KOBW", TalentsCategory.Special, Arrays.asList(base_value, 2));
        if (returned instanceof Double) {
            base_value = (Double) returned;
        }
        DebugService.debugMsg("Finished calculation, final value: " + base_value);
        return base_value;
    }

    private double calculateLSReduction(double base_value, EntityDamageByEntityEvent e) {
        DebugService.debugMsg("Started calculate reduction amount, starting value: " + base_value);
        for (String id : VICTIM_TALENTS_LS_REDUCTION) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Protective.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    (Player) e.getEntity(),
                                    TalentsCategory.Protective,
                                    id)
                            , Arrays.asList(base_value, e));
            if (returned instanceof Double) {
                base_value = (Double) returned;
            }
        }
        DebugService.debugMsg("Finished calculation, final value: " + base_value);
        return base_value;
    }
}