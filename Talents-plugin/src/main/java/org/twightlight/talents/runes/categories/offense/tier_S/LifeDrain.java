package org.twightlight.talents.runes.categories.offense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleFirework;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.nmsbridge.NMSBridge;
import org.twightlight.talents.nmsbridge.abstracts.objects.BoundingBox;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.offense.OffenseRune;
import org.twightlight.talents.utils.Utility;

public class LifeDrain extends OffenseRune {

    private static ParticleDustColored dustColored = new ParticleDustColored(0D, 0D, 0D, 2);

    static {
        dustColored.setColor(hm.zelha.particlesfx.util.Color.RED);
    }


    public LifeDrain() {
        super(64);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.LIFESTEAL.name()).add(12 * amount);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "Crit_Rate");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        if (Utility.rollChance(30)) {
            LivingEntity victim = e.getDamagePacket().getVictim();
            victim.setHealth(Math.max(0.5, victim.getHealth() - 1));

            Player attacker = e.getDamagePacket().getAttacker();
            attacker.setHealth(Math.max(attacker.getMaxHealth(), attacker.getHealth() + 1));

            BoundingBox boundingBox1 = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getBoundingBox(attacker);
            BoundingBox boundingBox2 = Talents.getInstance().getNMSBridge().getLivingEntityHelper().getBoundingBox(victim);

            Location loc1 = attacker.getLocation().clone().add(0, boundingBox1.getHeight() * 0.6, 0);

            Location loc2 = victim.getLocation().clone().add(0, boundingBox2.getHeight() * 0.6, 0);
            Vector step = loc2.toVector().subtract(loc1.toVector()).normalize().multiply(0.5);

            double distance = Utility.distance(loc1, loc2);

            int steps = (int) (distance / 0.5);

            for (int i = 0; i <= steps; i++) {
                dustColored.display(loc1);
                loc1.add(step);
            }
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.NORMAL_PREFIX, getCategory(), "Damage");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getAttacker(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        if (Utility.rollChance(30)) {
            LivingEntity victim = e.getDamagePacket().getVictim();
            victim.setHealth(Math.max(0.5, victim.getHealth() - 1));

            Player attacker = e.getDamagePacket().getAttacker();
            attacker.setHealth(Math.max(attacker.getMaxHealth(), attacker.getHealth() + 1));
        }
    }
}
