package org.twightlight.talents.skills.built_in_skills;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import java.util.List;

import hm.zelha.particlesfx.particles.ParticleBlockBreak;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.pvpmanager.api.calculation.LayeredCalculator;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.pvpmanager.api.properties.DamageProperty;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.talents.built_in_talents.offense.skills.melee.MysticalStand;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class SoulCollector extends Skill {

    private String soulsMetadataValue = "skill.soulCollector.souls";
    private  ParticleBlockBreak blockBreak = new ParticleBlockBreak();

    public SoulCollector(String id, List<Integer> costList) {
        super(id, costList);
        blockBreak.setOffset(1, 1, 1);
        blockBreak.setSpeed(1);
        blockBreak.setCount(2);
        blockBreak.setMaterialData(new MaterialData(XMaterial.BLUE_CONCRETE.parseMaterial()));
    }


    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());

        int level = user.getSkillLevel(getSkillId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        if (level != 0) {

            if (!user.hasMetadata(soulsMetadataValue) || !(user.getMetadataValue(soulsMetadataValue) instanceof Integer)) {
                user.setMetadata(soulsMetadataValue, 0);
            }

            int souls = (int) user.getMetadataValue(soulsMetadataValue);

            souls += Utility.rollChance(level * (!MysticalStand.isExtraAttack(e.getDamagePacket()) ? 1.75D : 0.875D)) ? 2 : 1;

            user.setMetadata(soulsMetadataValue, souls);


            if (Utility.rollChance(25.0D) && !MysticalStand.isExtraAttack(e.getDamagePacket())) {
                user.setMetadata(soulsMetadataValue, 0);
                playSoulExtraction(e.getDamagePacket().getVictim(), 16);

                double multiplier = 1.0D + (souls * level) * 0.0065D;

                DamageProperty property = e.getDamagePacket().getDamageProperty();

                property.addLayer("soulCollectorLayer", LayeredCalculator.OP_ADD, LayeredCalculator.OP_MULTIPLY, 1, Integer.MAX_VALUE);
                property.addValueToLayer("soulCollectorLayer", multiplier);

                e.getDamagePacket().getVictim().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));

                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
                p.playSound(p.getLocation(), XSound.ENTITY_PLAYER_BURP.parseSound(), 5.0F, 3.0F);
                p.sendMessage("§c§l[Soul Collector] §d Soul Released! §7(+" +
                        String.format("%.0f", multiplier * 100) + "% damage damage)");
                if (souls >= 5) {
                    blockBreak.display(e.getDamagePacket().getVictim().getLocation().clone().add(0, 1, 0));
                }
            }
        }
    }


    private void playSoulExtraction(final Entity entity, final int duration) {
        final ParticleDustColored particle = new ParticleDustColored();
        final ParticleDustColored particle1 = new ParticleDustColored();
        particle.setColor(hm.zelha.particlesfx.util.Color.PURPLE);
        particle.setCount(8);
        particle.setOffsetX(1);
        particle.setOffsetY(1);
        particle.setOffsetZ(1);
        particle.setSpeed(1);
        particle1.setColor(hm.zelha.particlesfx.util.Color.GRAY);
        particle1.setCount(8);
        particle1.setOffsetX(1);
        particle1.setOffsetY(1);
        particle1.setOffsetZ(1);
        particle.setSpeed(1);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick++ < duration / 4 && (!(entity instanceof Player) || ((Player) entity).isOnline())) {
                    particle.display(entity.getLocation().clone().add(0.0D, 1.0D, 0.0D));
                    particle.display(entity.getLocation().clone().add(0.0D, 1.0D, 0.0D));

                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(Talents.getInstance(), 0L, 4L);
    }

}
