package org.twightlight.talents.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.talents.Talents;
import org.twightlight.talents.database.SQLite;
import org.twightlight.talents.skills.Skill;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class SkillsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) {
            Utility.info("This command must be executed by a player!");
            return true;
        }

        if (!(sender instanceof Player)) {
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equalsIgnoreCase("list")) {
            if (!p.hasPermission("skills.admin")) {
                p.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            sender.sendMessage(ChatColor.GRAY + "---------------------Skills------------------");
            for (String skill : Talents.getInstance().getSkillsManager().getRegisteredSkills()) {
                sender.sendMessage(" - " + ChatColor.YELLOW + skill);
            }
            return true;
        }

        if (sub.equalsIgnoreCase("select")) {

            if (args.length < 3) {
                p.sendMessage("Lack of args!");
                return true;
            }

            String skill = args[1];
            if (!Talents.getInstance().getSkillsManager().skillExist(skill)) {
                p.sendMessage("Skill not found!");
                return true;
            }
            int slot;

            try {
                slot = Integer.parseInt(args[2]) - 1;
            } catch (NumberFormatException e) {
                slot = 0;
            }

            if (slot < 0 || slot > 3) {
                p.sendMessage("Invalid slot!");
                return true;
            }

            Talents.getInstance().getSkillsManager().selectSkill(p, slot, skill);
            p.sendMessage(ChatColor.GREEN + "Successfully set your skill at slot " + (slot + 1));
            return true;
        }

        if (sub.equalsIgnoreCase("deselect")) {

            int slot;

            try {
                slot = Integer.parseInt(args[1]) - 1;
            } catch (Exception e) {
                slot = 0;
            }

            if (slot < 0 || slot > 3) {
                p.sendMessage("Invalid slot!");
                return true;

            }

            Talents.getInstance().getSkillsManager().deselectSkill(p, slot);
            return true;
        }

        if (sub.equalsIgnoreCase("setlevel")) {
            String skill = args[1];
            if (!Talents.getInstance().getSkillsManager().skillExist(skill)) {
                p.sendMessage("Skill not found!");
                return true;

            }
            int level;

            try {
                level = Integer.parseInt(args[2]);
            } catch (Exception e) {
                level = 0;
            }

            if (level < 0 || level > 20) {
                p.sendMessage("Invalid level!");
                return true;

            }

            if (Talents.getInstance().getSkillsManager().setLevelSkill(level, skill, p)) {
                p.sendMessage(ChatColor.GREEN + "Successfully set your skill level to " + level);
            } else {
                p.sendMessage("Couldn't set your skill level!");
            }
        }

        if (sub.equalsIgnoreCase("reset")) {
            Talents.getInstance().getSkillsManager().resetActivatingSkill(p);
            return true;
        }

        if (sub.equalsIgnoreCase("view")) {

            if (User.getUserFromBukkitPlayer(p) != null) {
                User user = User.getUserFromBukkitPlayer(p);

                p.sendMessage("Selected skills:");

                for (String skill : user.getActivatingSkills()) {
                    if (skill != null) {
                        p.sendMessage("Skill: " + skill);
                    }
                }

            }
            return true;
        }

        if (sub.equalsIgnoreCase("magicalspirits")) {

            if (!p.hasPermission("skills.admin")) {
                p.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            if (args.length < 2) {
                p.sendMessage("Lack of args!");
                return true;
            }

            String action = args[1].toLowerCase();

            if (action.equals("add")) {

                if (args.length < 4) {
                    p.sendMessage("Usage: /skills magicalspirits add <player> <amount>");
                    return true;
                }

                if (!Utility.isInteger(args[3])) {
                    p.sendMessage("Invalid number!");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    p.sendMessage("Player not found!");
                    return true;
                }

                int amount = Integer.parseInt(args[3]);

                SQLite db = Talents.getInstance().getDb();
                int current = db.getMagicalSpirits(target);
                db.setMagicalSpirits(target, current + amount);

                p.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        "&aSuccessfully added " + amount + " magicalspirit(s) to " + target.getName()
                ));
            }

            return true;
        }

        return true;
    }
}

