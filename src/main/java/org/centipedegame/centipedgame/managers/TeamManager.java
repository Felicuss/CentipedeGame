package org.centipedegame.centipedgame.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.centipedegame.centipedgame.model.Team;

import java.util.*;

public class TeamManager {
    private final List<Team> teams = new ArrayList<>();
    private Team activeTeam = null;
    private Location locationOfOrganizator = null;
    private final Map<Player, Location> lastLeaderLocations = new HashMap<>();
    private int taskId = -1;
    private boolean stopMoving = true;

    public TeamManager() {
    }

    public void setLocation(Player player) {
        Location lastLocation = locationOfOrganizator;
        locationOfOrganizator = player.getLocation();
        if (lastLocation == null) {
            player.sendMessage(ChatColor.GREEN + "Местоположение сохранено!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Местоположение обновлено!");
        }
    }

    public int findTeam(Player player, String name) {
        for (int i = 0; i < teams.size(); i++) {
            if (Objects.equals(teams.get(i).getNameTeam(), name)) {
                return i;
            }
        }
        player.sendMessage(ChatColor.RED + "Команды не найдено!");
        return -1;
    }

    public void selectTeam(Player player, String[] args) {
        if (locationOfOrganizator == null) {
            player.sendMessage(ChatColor.RED + "Сначала через команду /centipede setlocation выберете позицию!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Вы не ввели название команды!");
            return;
        }
        if (taskId != -1) {
            player.sendMessage(ChatColor.RED + "Команда уже выбрана, сначала распустите прошлую команду!");
            return;
        }
        int indexOfTeam = findTeam(player, args[1]);
        if (indexOfTeam != -1) {
            activeTeam = teams.get(indexOfTeam);
            Player player1 = activeTeam.getPlayer1();
            Player player2 = activeTeam.getPlayer2();
            Player player3 = activeTeam.getPlayer3();
            player1.teleport(locationOfOrganizator);
            player2.teleport(locationOfOrganizator);
            player3.teleport(locationOfOrganizator);
            
            player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 4));
            player2.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 4));
            player3.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 4));
        }
    }

    public void listOfTeams(Player player) {
        if (teams.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Команд не найдено!");
            return;
        }
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            String teamName = team.getNameTeam();

            String player1Name = (team.getPlayer1() != null) ? team.getPlayer1().getName() : "Неизвестно";
            String player2Name = (team.getPlayer2() != null) ? team.getPlayer2().getName() : "Неизвестно";
            String player3Name = (team.getPlayer3() != null) ? team.getPlayer3().getName() : "Неизвестно";

            String message = ChatColor.YELLOW + String.valueOf(i + 1) + ". " + ChatColor.GREEN + teamName +
                    ChatColor.WHITE + " (Игроки: " + ChatColor.AQUA + player1Name + ", " +
                    player2Name + ", " + player3Name + ChatColor.WHITE + ")";
            player.sendMessage(message);
        }
    }

    public void deleteTeam(Player player, String[] args) {
        if (args.length < 2) {
            return;
        }
        for (int i = 0; i < teams.size(); i++) {
            if (Objects.equals(teams.get(i).getNameTeam(), args[1])) {
                player.sendMessage(ChatColor.YELLOW + "Команда " + teams.get(i).getNameTeam() + " успешно удалена!");
                teams.remove(i);
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "Такой команды не найдено!");
    }

    public void createTeam(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "Вы пропустили какой-то параметр!");
            return;
        }
        teams.add(new Team(Bukkit.getPlayer(args[1]), Bukkit.getPlayer(args[2]), Bukkit.getPlayer(args[3]), args[4], player));
        player.sendMessage(ChatColor.GREEN + "Команда " + args[4] + " создана");
    }

    public void handleDisbandTeam(Player player) {
        Team team = activeTeam;
        if (team != null) {
            team.getPlayer1().removePotionEffect(PotionEffectType.SLOW);
            team.getPlayer2().removePotionEffect(PotionEffectType.SLOW);
            team.getPlayer3().removePotionEffect(PotionEffectType.SLOW);
            lastLeaderLocations.remove(team.getPlayer2());
            activeTeam = null;
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            } else {
                player.sendMessage("§cВы не можете распустить команду!");
            }
            player.sendMessage("§cКоманда распущена!");

        } else {
            player.sendMessage("§cАктивной команды сейчас нет!");
        }
    }

    public void handleDisbandTeam() {
        Team team = activeTeam;
        if (team != null) {
            team.getPlayer1().getActivePotionEffects().forEach(effect -> team.getPlayer1().removePotionEffect(effect.getType()));
            team.getPlayer2().getActivePotionEffects().forEach(effect -> team.getPlayer2().removePotionEffect(effect.getType()));
            team.getPlayer3().getActivePotionEffects().forEach(effect -> team.getPlayer3().removePotionEffect(effect.getType()));
            lastLeaderLocations.remove(team.getPlayer2());
            activeTeam = null;
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
        }
    }

    public boolean hasMoved(Location oldLoc, Location newLoc, boolean isThirdGameActive) {
        if (isThirdGameActive) {
            return oldLoc.getX() != newLoc.getX() ||
                    oldLoc.getZ() != newLoc.getZ();
        }
        return oldLoc.getX() != newLoc.getX() ||
                oldLoc.getY() != newLoc.getY() ||
                oldLoc.getZ() != newLoc.getZ();
    }

    public void updatePlayerPositions(Location leaderLoc) {
        if (activeTeam == null) return;
        
        double yawRad = Math.toRadians(leaderLoc.getYaw());
        final double OFFSET = 1.2;

        double leftX = -Math.sin(yawRad - Math.PI / 2) * OFFSET;
        double leftZ = Math.cos(yawRad - Math.PI / 2) * OFFSET;
        double rightX = -Math.sin(yawRad + Math.PI / 2) * OFFSET;
        double rightZ = Math.cos(yawRad + Math.PI / 2) * OFFSET;

        teleportPlayer(activeTeam.getPlayer1(), leaderLoc, leftX, leftZ);
        teleportPlayer(activeTeam.getPlayer3(), leaderLoc, rightX, rightZ);
    }

    private void teleportPlayer(Player p, Location leaderLoc, double dx, double dz) {
        Location targetLoc = leaderLoc.clone().add(dx, 0, dz);
        Location currentLoc = p.getLocation().clone();
        targetLoc.setYaw(currentLoc.getYaw());
        targetLoc.setPitch(currentLoc.getPitch());
        if (currentLoc.distanceSquared(targetLoc) > 0.01) { // 0.1^2 = 0.01
            p.teleport(targetLoc);
        }
    }

    public Team getActiveTeam() {
        return activeTeam;
    }

    public void setActiveTeam(Team activeTeam) {
        this.activeTeam = activeTeam;
    }

    public Location getLocationOfOrganizator() {
        return locationOfOrganizator;
    }

    public void setLocationOfOrganizator(Location locationOfOrganizator) {
        this.locationOfOrganizator = locationOfOrganizator;
    }

    public Map<Player, Location> getLastLeaderLocations() {
        return lastLeaderLocations;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public boolean isStopMoving() {
        return stopMoving;
    }

    public void setStopMoving(boolean stopMoving) {
        this.stopMoving = stopMoving;
    }

    public List<Team> getTeams() {
        return teams;
    }
} 