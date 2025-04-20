package org.twightlight.talents.listeners.general;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.DebugService;

import java.util.Arrays;
import java.util.List;

public class GeneralArrowAttack implements Listener {
    private static List<String> DAMAGER_TALENTS_DAMAGE = Arrays.asList("TWIN", "AAD", "IMD", "SA", "FRZ", "AKB", "CA", "AP", "SM");
    private static List<String> DAMAGER_TALENTS_LIFESTEAL = Arrays.asList("MLS");
    private static List<String> VICTIM_TALENTS_DEFEND = Arrays.asList("DR", "ABS", "BL", "RFL");
    private static List<String> VICTIM_TALENTS_LS_REDUCTION = Arrays.asList("THR");
    @EventHandler(priority = EventPriority.HIGH)
    public void onArrowAttack(EntityDamageByEntityEvent e) {
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
        if (e.getDamager() instanceof Arrow) {
            if (!(((Arrow) e.getDamager()).getShooter() instanceof Player)) {
                Entity victim = e.getEntity();
                double final_damage = e.getDamage();
                if (victim instanceof Player) {
                    if (((Player) victim).isBlocking()) {
                        DebugService.debugMsg("Detected a block, trying to disable. Old damage: " + final_damage);
                        final_damage = final_damage  * 2;
                        DebugService.debugMsg("Modified! New damage: " + final_damage);
                    }
                    final_damage = calculateReduction(final_damage, e);
                }
                e.setDamage(final_damage);
                return;
            }
            Player p = (Player) ((Arrow) e.getDamager()).getShooter();
            if (e.getEntity() instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) e.getEntity();
                if (living.getNoDamageTicks() > 0) return;
            }
            Player attacker = (Player) ((Arrow) e.getDamager()).getShooter();
            if (((Arrow) e.getDamager()).isCritical()) {
                DebugService.debugMsg("Detected a critical arrow, trying to disable.");
                ((Arrow) e.getDamager()).setCritical(false);
            }
            Entity victim = e.getEntity();
            double final_damage = calculateFinalDamage(e.getDamage(), e, p);
            if (victim instanceof Player) {
                if (((Player) victim).isBlocking()) {
                    DebugService.debugMsg("Detected a block, trying to disable. Old damage: " + final_damage);
                    final_damage = final_damage  * 2;
                    DebugService.debugMsg("Modified! New damage: " + final_damage);
                }
                final_damage = calculateReduction(final_damage, e);
            }

            e.setDamage(final_damage);
            DebugService.debugMsg("Final damage: " + e.getFinalDamage());

            double final_lifesteal = calculateLifeSteal(e.getFinalDamage(), e, p);
            if (victim instanceof Player) {
                final_lifesteal = calculateLSReduction(final_lifesteal, e);
            }
            if (!e.isCancelled() && !attacker.isDead() && attacker.getHealth() > 0 && final_lifesteal > 0 && attacker.getHealth() + final_lifesteal <= attacker.getMaxHealth() && !(((LivingEntity) victim).getNoDamageTicks() > 0)) {
                attacker.setHealth(attacker.getHealth() + final_lifesteal);
                DebugService.debugMsg("Healed attacker by: " + final_lifesteal);
            }

        }
    }

    private double calculateFinalDamage(double base_value, EntityDamageByEntityEvent e, Player p) {
        DebugService.debugMsg("Started calculate damage, starting value: " + base_value);
        if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
            Double total_addition_value = ConversionUtil.getPlayerFromBukkitPlayer(p).getAttribute(PlayerAttribute.ARROW_DAMAGE);
            base_value += total_addition_value;
            DebugService.debugMsg("Attribute modified, new value: " + base_value);
        }
        DebugService.debugMsg("Modify value to: " + base_value);
        for (String id : DAMAGER_TALENTS_DAMAGE) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Ranged.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    p,
                                    TalentsCategory.Ranged,
                                    id)
                            , Arrays.asList(base_value, e, p));
            if (returned instanceof Double) {
                base_value = (Double) returned;
            }
        }
        Object returned = Talents.getInstance().getTalentsManagerService().handle(p, "KOBW", TalentsCategory.Special, Arrays.asList(base_value, 1));
        if (returned instanceof Double) {
            base_value = (Double) returned;
        }
        DebugService.debugMsg("Finished calculation, final value: " + base_value);
        return base_value;
    }

    private double calculateLifeSteal(double base_value, EntityDamageByEntityEvent e, Player p) {
        DebugService.debugMsg("Started calculate lifesteal amount, starting value: " + base_value);
        for (String id : DAMAGER_TALENTS_LIFESTEAL) {
            Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                    get(TalentsCategory.Ranged.getColumn()).
                    get(id).handle(
                            Talents.getInstance().getDatabase().getTalentLevel(
                                    p,
                                    TalentsCategory.Ranged,
                                    id)
                            , Arrays.asList(base_value, e, p, TalentsCategory.Ranged));
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

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();

        if (arrow.hasMetadata("twin")) {
            arrow.remove();
        }
    }
}
