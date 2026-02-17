package org.twightlight.talents.runes.categories.defense.tier_S;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.util.Color;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.pvpmanager.PVPManager;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.events.UndefinedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.api.packets.UndefinedDamagePacket;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.runes.categories.defense.DefenseRune;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class Weakness extends DefenseRune {

    private static String metadatatag = "rune.weakness.state";

    public Weakness() {
        super(64);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        e.getArena().getPlayers().forEach((p) -> {
            StatsMap map = PVPManager.getInstance().getStatsMapHandler().getStatMap(p.getUniqueId());
            int amount = Talents.getInstance().getRunesManager().getRuneAmount(p, RunesManager.SPECIAL_PREFIX, getRuneId());
            map.getStatContainer(BaseStats.GENERIC_ADDITIONAL_MAX_HEALTH.name()).add(1.5 * amount);
        });
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        if (!(packet.getVictim() instanceof Player)) return;

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.NORMAL_PREFIX, getCategory(), "Protect");
        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        Player victim = e.getDamagePacket().getVictimAsPlayer();
        Player attacker = e.getDamagePacket().getAttacker();

        User user = User.getUserFromBukkitPlayer(e.getDamagePacket().getAttacker());

        if (user == null) return;

        if (!user.hasMetadata(metadatatag)) {
            if (Utility.rollChance(30)) {
                victim.getWorld().playSound(attacker.getLocation(), XSound.ENTITY_PLAYER_SPLASH.parseSound(), 5.0F, 2.0F);
                user.setMetadata(metadatatag, true);
                this.playWeaknessAura(attacker, 40);
                DamageProperty property = e.getDamagePacket().getDamageProperty();

                property.addLayer("weaknessLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
                property.addValueToLayer("weaknessLayer", 0.8);
                Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    user.removeMetadata(metadatatag);
                }, 40L);
            }
        } else {
            DamageProperty property = e.getDamagePacket().getDamageProperty();

            property.addLayer("weaknessLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
            property.addValueToLayer("weaknessLayer", 0.8);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();

        if (!(packet.getVictim() instanceof Player)) return;

        int amount = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.NORMAL_PREFIX, getCategory(), "Protect");

        int amount1 = Talents.getInstance().getRunesManager().getRuneAmount(e.getDamagePacket().getVictimAsPlayer(), RunesManager.SPECIAL_PREFIX, getRuneId());

        if (amount < 3 || amount1 < 1) return;
        Player victim = e.getDamagePacket().getVictimAsPlayer();
        Player attacker = e.getDamagePacket().getAttacker();

        User user = User.getUserFromBukkitPlayer(e.getDamagePacket().getAttacker());

        if (user == null) return;

        if (!user.hasMetadata(metadatatag)) {
            if (Utility.rollChance(30)) {
                victim.getWorld().playSound(attacker.getLocation(), XSound.ENTITY_PLAYER_SPLASH.parseSound(), 5.0F, 2.0F);
                user.setMetadata(metadatatag, true);
                this.playWeaknessAura(attacker, 40);
                DamageProperty property = e.getDamagePacket().getDamageProperty();

                property.addLayer("weaknessLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
                property.addValueToLayer("weaknessLayer", 0.8);
                Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    user.removeMetadata(metadatatag);
                }, 40L);
            }
        } else {
            DamageProperty property = e.getDamagePacket().getDamageProperty();

            property.addLayer("weaknessLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
            property.addValueToLayer("weaknessLayer", 0.8);
        }
    }


    private void playWeaknessAura(LivingEntity entity, final int duration) {
        double radius = 1.2D;
        double radius1 = 0.6D;
        final ParticleDustColored particle = new ParticleDustColored(8);
        particle.setOffset(0, 0, 0);
        particle.setColor(Color.AQUA);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick++ >= duration || entity instanceof Player && !((Player)entity).isOnline()) {
                    this.cancel();
                } else {
                    double angle = Math.toRadians((this.tick * 12 % 360));

                    for(int i = 0; i < 8; ++i) {
                        double offset = Math.toRadians(((i * 45)));
                        double x = Math.cos(angle + offset) * radius;
                        double z = Math.sin(angle + offset) * radius;
                        double x1 = Math.cos(angle + offset) * radius1;
                        double z1 = Math.sin(angle + offset) * radius1;
                        particle.display(entity.getLocation().clone().add(x, 0.0D, z));
                        particle.display(entity.getLocation().clone().add(x1, 0.0D, z1));
                    }

                }
            }
        }).runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}
