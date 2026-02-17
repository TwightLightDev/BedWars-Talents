package org.twightlight.talents.talents.built_in_talents.melee.directly;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.*;

public class AdditionalMeleeAttack implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public static List<String> DAMAGER_TALENTS_DAMAGE = Arrays.asList("MAD", "IMD", "FA", "MSL", "TH", "CH", "AP", "MAS");
    public static List<String> DAMAGER_TALENTS_LIFESTEAL = Arrays.asList("MLS");
    public static List<String> VICTIM_TALENTS_LS_REDUCTION = Arrays.asList("THR");


    public AdditionalMeleeAttack(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        double past_value = (Double) params.get(0);
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);

        DebugService.debugMsg("Started rolling for additional attack, current chance: " + level * 2.5 + "%");
        if (!Utility.rollChance(level * 2.5) || e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return null;
        }
        DebugService.debugMsg("Bingo, AA activated!");
        float percentage = (float) (0.1 + ((float) level / 200));
        Player p = (Player) e.getDamager();
        Object returned = Talents.getInstance().getTalentsManagerService().Talents.
                get(TalentsCategory.Melee.getColumn()).
                get("AMAD").handle(
                        Talents.getInstance().getDatabase().getTalentLevel(
                                p,
                                TalentsCategory.Melee,
                                "AMAD")
                        , Arrays.asList(percentage, e));
        if (returned instanceof Float) {
            percentage = (Float) returned;
        }
        LivingEntity entity = ((LivingEntity) e.getEntity());
        int NDT = entity.getNoDamageTicks();
        entity.setNoDamageTicks(0);
        double damage = past_value * percentage;

        double final_damage = calculateFinalDamage(damage, e);

        if (entity instanceof Player) {
            if (((Player) entity).isBlocking()) {
                DebugService.debugMsg("Detected a block, trying to disable. Old damage: " + final_damage);
                final_damage = final_damage * 2;
                DebugService.debugMsg("Modified! New damage: " + final_damage);
            }
        }

        entity.damage(final_damage);
        DebugService.debugMsg("Final Damage of this AA before reduction is: " + final_damage);

        entity.setNoDamageTicks(NDT);

        if (entity instanceof Player) {
            final_damage = Utility.calculateDamageReduction((Player) entity, final_damage, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            DebugService.debugMsg("Final Damage of this AA after reduction is: " + final_damage);

        }

        double final_lifesteal = calculateLifeSteal(final_damage, e);
        if (entity instanceof Player) {
            final_lifesteal = calculateLSReduction(final_lifesteal, e);
        }
        if (!e.isCancelled() && !p.isDead() && p.getHealth() > 0 && final_lifesteal > 0 && p.getHealth() + final_lifesteal <= p.getMaxHealth() && !(((LivingEntity) entity).getNoDamageTicks() > 0)) {
            p.setHealth(p.getHealth() + final_lifesteal);
            DebugService.debugMsg("Healed attacker by: " + final_lifesteal);
        }

        return null;
    }

    private double calculateFinalDamage(double base_value, EntityDamageByEntityEvent e) {
        DebugService.debugMsg("Started calculate damage, starting value: " + base_value);
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

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
