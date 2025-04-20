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
import org.twightlight.talents.internal.PlayerAttribute;
import org.twightlight.talents.menus.PlayerMenu;
import org.twightlight.talents.talents.interfaces.Talent;
import org.twightlight.talents.utils.ConversionUtil;
import org.twightlight.talents.utils.Utility;

import java.util.List;
import java.util.Set;

public class MainCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Utility.info("This command must be executed by a player!");
        }
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1) {
                return true;
            } else {
                switch (args[0].toLowerCase()) {
                    case "check":
                        if (args.length < 2) {
                            p.sendMessage("Lack of args!");
                        }
                        if (ConversionUtil.getPlayerFromBukkitPlayer(p) != null) {
                            org.twightlight.talents.internal.Player player = ConversionUtil.getPlayerFromBukkitPlayer(p);
                            PlayerAttribute attribute = PlayerAttribute.valueOf(args[1].toUpperCase());
                            Double value = player.getAttribute(attribute);
                            if (value != null) {
                                p.sendMessage("Value: " + value);
                            }
                        } else {
                            p.sendMessage("You are not in game!");
                        }

                    case "menu":
                        if (PlayerMenu.getInstance(p) == null) {
                            PlayerMenu menu = new PlayerMenu(p, 0, 0);
                            menu.getHolder().open();
                        } else {
                            PlayerMenu.getInstance(p).getHolder().open();
                        }
                        break;
                    case "soulstones":
                        if (p.hasPermission("talents.admin")) {
                            if (args.length < 2) {
                                p.sendMessage("Lack of args!");
                                return true;
                            } else {
                                switch (args[1].toLowerCase()) {
                                    case "add":
                                        if (args.length < 4) {
                                            p.sendMessage("Lack of args! Usage: /talents soulstones add [<player>] [<amount>]");
                                            return true;
                                        } else if (!Utility.isInteger(args[3])) {
                                            p.sendMessage("Invalid args!");
                                        }
                                        SQLite db = Talents.getInstance().getDatabase();
                                        Player target = Bukkit.getPlayer(args[2]);
                                        int ss = db.getSoulStones(target);
                                        db.setSoulStones(target, ss + Integer.parseInt(args[3]));
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                "Successfully added " + args[3] + " soulstone to " + args[2]));
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        }
        return true;

    }
}
