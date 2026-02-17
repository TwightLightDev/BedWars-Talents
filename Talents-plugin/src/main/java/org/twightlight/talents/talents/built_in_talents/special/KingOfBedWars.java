package org.twightlight.talents.talents.built_in_talents.special;

import com.cryptomorin.xseries.XMaterial;
import hm.zelha.particlesfx.particles.ParticleFlame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.events.RangedDamageEvent;
import org.twightlight.pvpmanager.api.packets.MeleeDamagePacket;
import org.twightlight.pvpmanager.api.packets.RangedDamagePacket;
import org.twightlight.pvpmanager.stats.StatsMap;
import org.twightlight.pvpmanager.utils.BaseStats;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.talents.Talent;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class KingOfBedWars extends Talent {

    private final FixedMetadataValue metadataValue = new FixedMetadataValue(Talents.getInstance(), true);

    public KingOfBedWars(String talentId, List<Integer> costList) {
        super(talentId, costList);
    }


    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onMeleeAttack(MeleeDamageEvent e) {
        MeleeDamagePacket packet = e.getDamagePacket();

        User attacker = User.getUserFromUUID(packet.getAttacker().getUniqueId());

        Integer level = attacker.getTalentLevel(getTalentId());

        if (Utility.rollChance(level * 0.25)) {
            packet.getDamageProperty().addLayer("KOBW_Attack", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
            packet.getDamageProperty().addValueToLayer("KOBW_Attack", 1 + (double) level / 100);
            packet.putMetadata("KOBW_Attack", true);
        }

        if (!packet.victimIsPlayer()) return;

        User victim = User.getUserFromUUID(packet.getVictimAsPlayer().getUniqueId());

        level = victim.getTalentLevel(getTalentId());

        if (Utility.rollChance(level * 0.25)) {
            packet.getDamageProperty().addLayer("KOBW_Defense", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
            packet.getDamageProperty().addValueToLayer("KOBW_Defense", 1 - (double) level * 0.8 / 100);
            packet.putMetadata("KOBW_Defense", true);
        }

        Player p = packet.getVictimAsPlayer();
        StatsMap vstatsMap = packet.getVictimStatsMap();

        if (vstatsMap == null) return;

        double percentage;
        percentage = (1.0D - p.getHealth() / p.getMaxHealth()) / 0.1D;
        long interval = (long)level * 3L;
        if (percentage >= 5.0D && !p.hasMetadata("DaoTanBu") && !p.hasMetadata("HoiChieuDaoTanBu")) {
            p.setMetadata("DaoTanBu", this.metadataValue);

            vstatsMap.getStatContainer(BaseStats.CRITICAL_RATE.name()).add(12);
            vstatsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).add(25);
            vstatsMap.getStatContainer(BaseStats.PENETRATION_POINTS.name()).add(2);

            this.playDTBAura(p, (int)interval);

            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                p.removeMetadata("DaoTanBu", Talents.getInstance());
                vstatsMap.getStatContainer(BaseStats.CRITICAL_RATE.name()).subtract(12);
                vstatsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).subtract(25);
                vstatsMap.getStatContainer(BaseStats.PENETRATION_POINTS.name()).subtract(2);

                p.setMetadata("HoiChieuDaoTanBu", this.metadataValue);
                Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    p.removeMetadata("HoiChieuDaoTanBu", Talents.getInstance());
                }, 120L);
            }, interval);
        }
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.LOWEST)
    public void onRangedAttack(RangedDamageEvent e) {
        RangedDamagePacket packet = e.getDamagePacket();

        User attacker = User.getUserFromUUID(packet.getAttacker().getUniqueId());

        Integer level = attacker.getTalentLevel(getTalentId());

        if (Utility.rollChance(level * 0.25)) {
            packet.getDamageProperty().addLayer("KOBW_Attack", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
            packet.getDamageProperty().addValueToLayer("KOBW_Attack", 1 + (double) level / 100);
            packet.putMetadata("KOBW_Attack", true);
        }

        if (!packet.victimIsPlayer()) return;

        User victim = User.getUserFromUUID(packet.getVictimAsPlayer().getUniqueId());

        level = victim.getTalentLevel(getTalentId());

        if (Utility.rollChance(level * 0.25)) {
            packet.getDamageProperty().addLayer("KOBW_Defense", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 0.2, 1);
            packet.getDamageProperty().addValueToLayer("KOBW_Defense", 1 - (double) level * 0.8 / 100);
            packet.putMetadata("KOBW_Defense", true);
        }

        Player p = packet.getVictimAsPlayer();
        StatsMap vstatsMap = packet.getVictimStatsMap();

        if (vstatsMap == null) return;

        double percentage;
        percentage = (1.0D - p.getHealth() / p.getMaxHealth()) / 0.1D;
        long interval = (long)level * 3L;
        if (percentage >= 5.0D && !p.hasMetadata("DaoTanBu") && !p.hasMetadata("HoiChieuDaoTanBu")) {
            p.setMetadata("DaoTanBu", this.metadataValue);

            vstatsMap.getStatContainer(BaseStats.CRITICAL_RATE.name()).add(12);
            vstatsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).add(25);
            vstatsMap.getStatContainer(BaseStats.PENETRATION_POINTS.name()).add(2);


            this.playDTBAura(p, (int)interval);

            Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                p.removeMetadata("DaoTanBu", Talents.getInstance());
                vstatsMap.getStatContainer(BaseStats.CRITICAL_RATE.name()).subtract(12);
                vstatsMap.getStatContainer(BaseStats.CRITICAL_DAMAGE.name()).subtract(25);
                vstatsMap.getStatContainer(BaseStats.PENETRATION_POINTS.name()).subtract(2);
                p.setMetadata("HoiChieuDaoTanBu", this.metadataValue);
                Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    p.removeMetadata("HoiChieuDaoTanBu", Talents.getInstance());
                }, 120L);
            }, interval);
        }
    }

    private void playDTBAura(final Player player, final int duration) {
        double radius = 1.2D;
        final ParticleFlame particle = new ParticleFlame(new Vector(0, 0, 0), 0, 0, 0, 8);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick++ < duration && player.isOnline()) {
                    double angle = Math.toRadians((this.tick * 12 % 360));

                    for(int i = 0; i < 8; ++i) {
                        double offset = Math.toRadians((i * 45));
                        double x = Math.cos(angle + offset) * radius;
                        double z = Math.sin(angle + offset) * radius;
                        particle.display(player.getLocation().clone().add(x, 1.2D, z));
                    }

                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(Talents.getInstance(), 0L, 1L);
    }


}
