package org.twightlight.talents.skills.built_in_skills;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.pvpmanager.api.events.MeleeDamageEvent;
import org.twightlight.talents.Talents;
import org.twightlight.talents.arenas.Arena;
import org.twightlight.talents.dispatcher.EventDispatcher;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.InGameData;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.CombatUtils;
import org.twightlight.talents.utils.Utility;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JusticeJudgement extends Skill {

    private String hitsmetadatavalue = "skill.justiceJudgement.hits";
    private String taskmetadatavalue = "skill.justiceJudgement.task";
    private Map<String, Object> initMetadata = Map.of("judgement", true);
    private Set<String> set = Set.of("reductionLayer1", "blockLayer", "KOBW_Defense",
            "weaknessLayer", "ironWillLayer",
            "riftwalkerDefenseLayer");

    public JusticeJudgement(String id, List<Integer> costList) {
        super(id, costList);
    }

    @EventDispatcher.ListenerPriority(EventDispatcher.Priority.NORMAL)
    public void onMeleeAttack(MeleeDamageEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getDamagePacket().getAttacker();
        User user = User.getUserFromUUID(p.getUniqueId());
        IArena iArena = Talents.getInstance().getAPI().getArenaUtil().getArenaByPlayer(p);
        Arena arena = Talents.getInstance().getArenaManager().getArenaFromIArena(iArena);

        int level = user.getSkillLevel(getSkillId());
        if (!user.getActivatingSkills().contains(getSkillId())) {
            return;
        }
        if (level != 0) {
            if (!user.hasMetadata(hitsmetadatavalue) || !(user.getMetadataValue(hitsmetadatavalue) instanceof Integer)) {
                user.setMetadata(hitsmetadatavalue, 0);
            }
            if (user.getMetadataValue(taskmetadatavalue) instanceof BukkitTask) {
                ((BukkitTask) user.getMetadataValue(taskmetadatavalue)).cancel();
            }
            int current_hits = (Integer) user.getMetadataValue(hitsmetadatavalue);

            current_hits += level == 20 && Utility.rollChance(45.0D) ? 2 : 1;

            user.setMetadata(hitsmetadatavalue, current_hits);
            user.setMetadata(taskmetadatavalue, Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                user.setMetadata(hitsmetadatavalue, 0);
            }, 100L));


            if (current_hits >= 4) {
                ((BukkitTask) user.getMetadataValue(taskmetadatavalue)).cancel();
                user.setMetadata(hitsmetadatavalue, current_hits-4);
                user.setMetadata(taskmetadatavalue, Bukkit.getScheduler().runTaskLater(Talents.getInstance(), () -> {
                    user.setMetadata(hitsmetadatavalue, 0);
                }, 100L));
                double damage = 1.5D + (double) level * 0.075D;
                if (e.getDamagePacket().getVictim() instanceof Player) {
                    Player victim = (Player) e.getDamagePacket().getVictim();
                    if (User.getUserFromBukkitPlayer(victim) != null) {
                        int kills = arena.getInGameDataOfUUID(victim.getUniqueId()).get(InGameData.CURRENT_KILLS);
                        damage += (0.2D + 0.04D * (double) (level - (level == 20 ? 0 : 1))) * (double) kills;
                        CombatUtils.dealUndefinedDamage(victim, Math.min(damage, 12.0D), EntityDamageEvent.DamageCause.ENTITY_ATTACK, initMetadata, set);
                        p.sendMessage("§c§l[Justice Judgement] §6⚡ Strike! §7(+" +
                        String.format("%.0f", Math.min(damage, 12.0D)) + " direct damage)");
                    }
                    this.playJudgementAura(e.getDamagePacket().getVictim(), 10);
                    return;
                }

                if (e.getDamagePacket().getVictim() != null) {
                    p.getWorld().playSound(e.getDamagePacket().getVictim().getLocation().clone(), XSound.BLOCK_ANVIL_LAND.name(), 10.0F, 3.0F);
                    (e.getDamagePacket().getVictim()).setHealth(Math.max(1.0D, (e.getDamagePacket().getVictim()).getHealth() - damage));
                }

                this.playJudgementAura(e.getDamagePacket().getVictim(), 10);
            }
        }
    }

    private void playJudgementAura(final Entity entity, final int duration) {
        double radius = 0.4D;
        final ParticleDustColored particle = new ParticleDustColored();
        particle.setColor(hm.zelha.particlesfx.util.Color.YELLOW);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick++ >= duration || entity instanceof Player && !((Player)entity).isOnline()) {
                    this.cancel();
                } else {
                    for(int i = 0; i < 4; ++i) {
                        double offset = Math.toRadians((i * 90));
                        double x = Math.cos(offset) * radius;
                        double z = Math.sin(offset) * radius;
                        particle.setCount(8);
                        particle.setOffset(0, 0, 0);
                        particle.display(entity.getLocation().clone().add(x, (double)this.tick * 0.3D, z));
                    }

                }
            }
        }).runTaskTimer(Talents.getInstance(), 0L, 1L);
    }
}
