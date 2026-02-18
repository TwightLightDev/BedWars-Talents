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
import org.twightlight.talents.menus.runes.RuneItem;
import org.twightlight.talents.menus.runes.RuneMenu;
import org.twightlight.talents.menus.runes.RuneSelectMenu;
import org.twightlight.talents.menus.skills.SkillMenu;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.users.User;
import org.twightlight.talents.utils.Utility;

public class RunesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0 && sender instanceof Player) {
            RuneMenu.open((Player) sender, RunesManager.categories.get(0));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equalsIgnoreCase("list")) {
            if (!sender.hasPermission("runes.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            sender.sendMessage(ChatColor.GRAY + "---------------------Runes------------------");
            for (String skill : Talents.getInstance().getRunesManager().getRegisteredRunes()) {
                sender.sendMessage(" - " + ChatColor.YELLOW + skill);
            }
            return true;
        }

        if (sub.equalsIgnoreCase("give")) {
            if (!sender.hasPermission("runes.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Lack of args!");
            }

            Player p = Bukkit.getPlayerExact(args[1]);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + "Player cannot be found!");
                return true;
            }
            String id = args[2];

            if (!Utility.isInteger(args[3])) {
                sender.sendMessage(ChatColor.RED + "Invalid amount!");
            }
            int amount = Integer.parseInt(args[3]);

            Talents.getInstance().getRunesManager().addToStorage(p, id, amount);
            if (args.length >= 5 && args[4].equals("silent")) {
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + "Successfully gave "+ args[3] +" rune with id "+ args[2] +" to "+ args[1] + "!");
            return true;
        }

        if (sub.equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be executed by player!");
                return true;
            }

            Player p = (Player) sender;

            RuneMenu.open(p, RunesManager.categories.get(0));
            return true;
        }

        return true;
    }
}

