package org.twightlight.talents.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.pvpmanager.utils.Logger;
import org.twightlight.talents.Talents;

public class CombatUtils {
    public static void dealTrueDamage(double baseTrueDamage, LivingEntity attacker, LivingEntity victim) {
        StatsMap attackerSM = attacker == null ? null : PVPManager.getInstance().getStatsMapHandler().getStatMap(attacker.getUniqueId());
        StatsMap victimSM = PVPManager.getInstance().getStatsMapHandler().getStatMap(victim.getUniqueId());
        double trueDamage = baseTrueDamage;
        if (attackerSM != null) {
            trueDamage = baseTrueDamage + (double)attackerSM.getStatContainer(BaseStats.TRUE_DAMAGE.name()).get();
        }

        if (victimSM != null) {
            trueDamage -= victimSM.getStatContainer(BaseStats.TRUE_DEFENSE.name()).get();
        }

        trueDamage = Math.max(0.0F, trueDamage);
        victim.setHealth(Math.max(0.5F, victim.getHealth() - trueDamage));
    }

    public static void dealMeleeDamage(Player damager, LivingEntity victim, double damage, Map<String, Object> initMetadata) {
        dealMeleeDamage(damager, victim, damage, initMetadata, Collections.emptySet());
    }

    public static void dealMeleeDamage(Player damager, LivingEntity victim, double damage, Map<String, Object> initMetadata, Set<String> set) {
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, victim, DamageCause.ENTITY_ATTACK, damage);
        MeleeDamageEvent e = new MeleeDamageEvent(event, initMetadata, set);
        Bukkit.getPluginManager().callEvent(e);
        int c2 = victim.getNoDamageTicks();
        victim.damage(0.01);
        if (!e.isCancelled()) {
            float absorbtion = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getAbsorptionHearts(victim);
            double fdamage = e.getDamagePacket().getDamageProperty().getDamage(PVPManager.getInstance().getMainConfig().getList("layersOrder.MeleeDamageEvent"));
            float left = (float) (absorbtion - fdamage);

            Talents.getInstance().getNMSBridge().getLivingEntityHelper().setAbsorptionHearts(victim, Math.max(0, left));
            victim.setHealth(Math.max(0.5F, Math.min(victim.getHealth(), victim.getHealth() + left)));
            e.getDamagePacket().getFinalConsumers().forEach((c) -> c.accept(e));
            Logger.debug("Total damage: " + e.getDamagePacket().getDamageProperty().getDamage(PVPManager.getInstance().getMainConfig().getList("layersOrder.MeleeDamageEvent")));
            Logger.debug("Applied Layers: " + e.getDamagePacket().getDamageProperty().getLayers());

        }

        victim.setNoDamageTicks(c2);
    }

    public static void dealRangedDamage(Projectile damager, LivingEntity victim, double damage, Map<String, Object> initMetadata) {
        dealRangedDamage(damager, victim, damage, initMetadata, Collections.emptySet());
    }

    public static void dealRangedDamage(Projectile damager, LivingEntity victim, double damage, Map<String, Object> initMetadata, Set<String> set) {
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, victim, DamageCause.ENTITY_ATTACK, damage);
        RangedDamageEvent e = new RangedDamageEvent(event, initMetadata, set);
        Bukkit.getPluginManager().callEvent(e);
        int c2 = victim.getNoDamageTicks();
        victim.damage(0.01);
        if (!e.isCancelled()) {

            float absorbtion = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getAbsorptionHearts(victim);
            double fdamage = e.getDamagePacket().getDamageProperty().getDamage(PVPManager.getInstance().getMainConfig().getList("layersOrder.RangedDamageEvent"));
            float left = (float) (absorbtion - fdamage);

            Talents.getInstance().getNMSBridge().getLivingEntityHelper().setAbsorptionHearts(victim, Math.max(0, left));
            victim.setHealth(Math.max(0.5F, Math.min(victim.getHealth(), victim.getHealth() + left)));

            e.getDamagePacket().getFinalConsumers().forEach((c) -> c.accept(e));
        }

        victim.setNoDamageTicks(c2);
    }

    public static void dealUndefinedDamage(Player victim, double damage, EntityDamageEvent.DamageCause cause, Map<String, Object> initMetadata) {
        dealUndefinedDamage(victim, damage, cause, initMetadata, Collections.emptySet());
    }

    public static void dealUndefinedDamage(Player victim, double damage, EntityDamageEvent.DamageCause cause, Map<String, Object> initMetadata, Set<String> set) {
        UndefinedDamageEvent e = new UndefinedDamageEvent(victim, damage, cause, initMetadata, set);
        Bukkit.getPluginManager().callEvent(e);
        int c2 = victim.getNoDamageTicks();
        victim.damage(0.01);
        if (!e.isCancelled()) {
            List<String> list = Collections.emptyList();

            float absorbtion = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getAbsorptionHearts(victim);
            double fdamage = e.getDamagePacket().getDamageProperty().getDamage(list);
            float left = (float) (absorbtion - fdamage);

            Talents.getInstance().getNMSBridge().getLivingEntityHelper().setAbsorptionHearts(victim, Math.max(0, left));
            victim.setHealth(Math.max(0.5F, Math.min(victim.getHealth(), victim.getHealth() + left)));


            e.getDamagePacket().getFinalConsumers().forEach((c) -> c.accept(e));
        }

        victim.setNoDamageTicks(c2);
    }
}

