package org.twightlight.talents.runes.categories.offense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.particles.ParticleFlame;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.nmsbridge.abstracts.objects.BoundingBox;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.offense.OffenseRune;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

public class VoidShot extends OffenseRune {

    private static ParticleFlame flame = new ParticleFlame(0D, 0D, 0D, 2);
    private static ParticleExplosion explosion = new ParticleExplosion(0D, 0D, 0D, 1);


    public VoidShot() {
        super(64);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.INCREASE_RANGED_DAMAGE.name()).add(15 * amount);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "Bow");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        if (Utility.rollChance(25)) {
            RangedDamagePacket packet = e.getDamagePacket();

            packet.putMetadata("void-activated", true);
            EntityDamageEvent lightningEvent = new EntityDamageEvent(packet.getVictim(), EntityDamageEvent.DamageCause.VOID, 0.0D);
            Bukkit.getPluginManager().callEvent(lightningEvent);
            if (!lightningEvent.isCancelled()) {
                LivingEntity entity = packet.getVictim();
                LivingEntity attacker = packet.getAttacker();

                CombatUtils.dealTrueDamage(2.5, attacker, entity);

                entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1), false);

                if (attacker.getHealth() > 0.0D) {
                    attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 1.0D));
                }

                BoundingBox box = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getBoundingBox(packet.getVictim());

                explosion.display(packet.getVictim().getLocation().add(0.0D, box.getHeight()/2, 0.0D));

                for(int i = 0; i < 6; ++i) {
                    flame.display(packet.getVictim().getLocation().add(0.0D, (double)i * 0.2D * (double)(6 - i) + 1.5D, 0.0D));
                }

                Sound sound = XSound.BLOCK_GLASS_BREAK.parseSound();

                attacker.getWorld().playSound(attacker.getLocation(), sound, 10, 5);
            }
        }
    }
}
