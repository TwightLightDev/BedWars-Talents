package org.twightlight.talents.talents.built_in_talents.ranged.skills;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.entity.Despawnable;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import net.minecraft.server.v1_8_R3.EntitySilverfish;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.talents.Talents;
import org.twightlight.talents.talents.enums.TalentsCategory;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.DebugService;
import org.twightlight.talents.utils.Utility;

import java.util.List;

public class Summoner implements Talent<Void> {
    private final TalentsCategory category;
    private final List<Integer> costList;
    public Summoner(TalentsCategory category, List<Integer> costList) {
        this.category = category;
        this.costList = costList;
    }

    public Void handle(int level, List<Object> params) {
        if (level <= 0) {
            return null;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) params.get(1);
        Player p = (Player) params.get(2);
        if (Talents.getInstance().getApi().getArenaUtil().getArenaByPlayer(p) == null) {
            return null;
        }
        ITeam iTeam = Talents.getInstance().getApi().getArenaUtil().getArenaByPlayer(p).getTeam(p);
        DebugService.debugMsg("Rolling chance for bedbug, current chance: " + level*3.5 + "%");
        if (Utility.rollChance(level*3.5)) {
            LivingEntity silverfish = (LivingEntity) e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.SILVERFISH);

            Boolean bool = (Boolean) Talents.getInstance().getTalentsManagerService().
                    handle(p, "DLC", TalentsCategory.Ranged, null);

            new Despawnable(silverfish, iTeam, (level + 20), "shop-utility-silverfish", PlayerKillEvent.PlayerKillCause.SILVERFISH, PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL);
            if (bool) {
                LivingEntity silverfish1 = (LivingEntity) e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.SILVERFISH);

                new Despawnable(silverfish1, iTeam, (level + 20), "shop-utility-silverfish", PlayerKillEvent.PlayerKillCause.SILVERFISH, PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL);
            }
            DebugService.debugMsg("Bingo, summoned a bedbug for: " + (10 + level) * 20 + " ticks!");
        }
        return null;
    }

    public TalentsCategory getCategory() {
        return category;
    }

    public List<Integer> getCostList() {
        return costList;
    }
}
