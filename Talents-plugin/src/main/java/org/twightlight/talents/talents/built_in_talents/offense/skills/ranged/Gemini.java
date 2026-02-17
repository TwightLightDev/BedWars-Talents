package org.twightlight.talents.talents.built_in_talents.offense.skills.ranged;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;

import java.util.List;

public class Gemini extends Talent {
    public Gemini(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }
    private FixedMetadataValue metadata = new FixedMetadataValue(Talents.getInstance(), true);


    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onProjectileShoot(EntityShootBowEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!User.isPlaying(p)) return;

        p.setNoDamageTicks(9);
        Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
            Arrow arrow = p.launchProjectile(Arrow.class);
            arrow.setShooter(p);
            arrow.setVelocity(p.getLocation().getDirection().normalize().multiply(e.getForce() * 3.0F));
            arrow.setCritical(((Arrow)e.getProjectile()).isCritical());
            arrow.setMetadata("gemini", this.metadata);
        }, 10L);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onProjectileDamage(RangedDamageEvent e) {
        Projectile projectile = e.getDamagePacket().getProjectile();

        User user = User.getUserFromBukkitPlayer(e.getDamagePacket().getAttacker());
        int level = user.getTalentLevel(getTalentId());

        if (projectile.hasMetadata("gemini")) {
            DamageProperty property = e.getDamagePacket().getDamageProperty();
            property.addLayer("gemini", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0, Integer.MAX_VALUE);
            property.addValueToLayer("gemini", level * 0.015);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onArrowLand(ProjectileHitEvent e) {
        if (e.getEntity().isOnGround() && e.getEntity().hasMetadata("gemini")) {
            e.getEntity().remove();
        }
    }

}
