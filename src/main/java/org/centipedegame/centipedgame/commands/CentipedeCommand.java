package org.centipedegame.centipedgame.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.centipedegame.centipedgame.CentipedeGame;
import org.centipedegame.centipedgame.managers.GameManager;
import org.centipedegame.centipedgame.managers.TeamManager;
import org.centipedegame.centipedgame.managers.TimerManager;
import org.centipedegame.centipedgame.model.Team;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CentipedeCommand implements CommandExecutor, TabCompleter {
    private final CentipedeGame plugin;
    private final TeamManager teamManager;
    private final GameManager gameManager;
    private final TimerManager timerManager;
    private final List<String> subCommands = Arrays.asList(
            "go", "setredblock", "settimer", "setlocation", 
            "createteam", "deleteteam", "list", "selectteam", 
            "disbandteam", "stop", "thirdgamedelay"
    );
    public CentipedeCommand(CentipedeGame plugin, TeamManager teamManager, GameManager gameManager, TimerManager timerManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.gameManager = gameManager;
        this.timerManager = timerManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        if (!player.isOp()) {
            player.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }
        if (args.length == 0) return false;
        switch (args[0].toLowerCase()) {
            case "settimer":
                timerManager.setTimer(player, args);
                break;
            case "createteam":
                teamManager.createTeam(player, args);
                break;
            case "deleteteam":
                teamManager.deleteTeam(player, args);
                break;
            case "list":
                teamManager.listOfTeams(player);
                break;
            case "selectteam":
                teamManager.selectTeam(player, args);
                break;
            case "setredblock":
                gameManager.setRedBlock(player);
                break;
            case "setlocation":
                teamManager.setLocation(player);
                break;
            case "go":
                gameManager.startEvent(player);
                break;
            case "stop":
                gameManager.stopGame(player);
                break;
            case "disbandteam":
                teamManager.handleDisbandTeam(player);
                break;
            case "thirdgamedelay":
                gameManager.setThirdGameDelay(player, args);
                break;
            default:
                player.sendMessage("§cНеверная подкоманда! Используйте /centipede <подкоманда>");
                break;
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("centipede")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                for (String subCommand : subCommands) {
                    if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(subCommand);
                    }
                }
                return completions;
            } else if ((args.length == 2 || args.length == 3 || args.length == 4) && args[0].equalsIgnoreCase("createteam")) {
                List<String> playerNames = new ArrayList<>();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    playerNames.add(onlinePlayer.getName());
                }
                return playerNames;
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("selectteam") || args[0].equalsIgnoreCase("deleteteam"))) {
                List<String> teamsNames = new ArrayList<>();
                for (Team team : teamManager.getTeams()) {
                    teamsNames.add(team.getNameTeam());
                }
                return teamsNames;
            }
        }
        return Collections.emptyList();
    }
} 