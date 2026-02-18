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
import org.twightlight.talents.menus.runes.RuneMenu;
import org.twightlight.talents.menus.talents.PlayerMenu;
import org.twightlight.talents.menus.talents.TalentsMenu;
import org.twightlight.talents.runes.RunesManager;
import org.twightlight.talents.utils.Utility;

public class TalentsCommand implements CommandExecutor {

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
            if (PlayerMenu.getInstance(p) == null) {
                PlayerMenu menu = new PlayerMenu(p, 1, 0);
                menu.getHolder().open();
            } else {
                PlayerMenu.getInstance(p).getHolder().open();
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        /* ---------------- /talents check ---------------- */

        if (sub.equals("list")) {
            if (!p.hasPermission("talents.admin")) {
                p.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            sender.sendMessage(ChatColor.GRAY + "---------------------Talents------------------");
            for (String talent : Talents.getInstance().getTalentsManager().getRegisteredTalents()) {
                sender.sendMessage(" - " + ChatColor.YELLOW + talent);
            }
            return true;
        }

        if (sub.equals("menu")) {
            if (PlayerMenu.getInstance(p) == null) {
                PlayerMenu menu = new PlayerMenu(p, 1, 0);
                menu.getHolder().open();
            } else {
                PlayerMenu.getInstance(p).getHolder().open();
            }
            return true;
        }

        if (sub.equals("menuitems")) {
            if (!p.hasPermission("talents.admin")) {
                p.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            sender.sendMessage(TalentsMenu.menuItems.toString());

            return true;
        }

        if (sub.equals("soulstones")) {

            if (!p.hasPermission("talents.admin")) {
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
                    p.sendMessage("Usage: /talents soulstones add <player> <amount>");
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
                int current = db.getSoulStones(target);
                db.setSoulStones(target, current + amount);

                p.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        "&aSuccessfully added " + amount + " soulstone(s) to " + target.getName()
                ));
            }

            return true;
        }

        return true;
    }
}
