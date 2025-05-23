package org.centipedegame.centipedgame.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.centipedegame.centipedgame.CentipedeGame;
import org.centipedegame.centipedgame.managers.GameManager;
import org.centipedegame.centipedgame.managers.TeamManager;
import org.centipedegame.centipedgame.model.Team;

import java.util.Objects;

public class MovementListener implements Listener {
    private final CentipedeGame plugin;
    private final TeamManager teamManager;
    private final GameManager gameManager;
    
    public MovementListener(CentipedeGame plugin, TeamManager teamManager, GameManager gameManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.gameManager = gameManager;
        new BukkitRunnable() {
            @Override
            public void run() {
                updateTeamPositions();
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    @EventHandler
    public void onLeaderMove(PlayerMoveEvent playerMoveEvent) {
        Team activeTeam = teamManager.getActiveTeam();
        if (activeTeam != null && !gameManager.isAnyGameActive() && 
            playerMoveEvent.getPlayer() == activeTeam.getPlayer2() && teamManager.isStopMoving()) {
            playerMoveEvent.setCancelled(true);
            playerMoveEvent.setTo(playerMoveEvent.getFrom());
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Team team = teamManager.getActiveTeam();
        if (team == null) return;
        if ((player.equals(team.getPlayer1()) || player.equals(team.getPlayer3())) && !gameManager.isThirdGameActive()) {
            if (teamManager.hasMoved(event.getFrom(), Objects.requireNonNull(event.getTo()), gameManager.isThirdGameActive())) {
                Player leader = team.getPlayer2();
                Location lastLeaderLoc = teamManager.getLastLeaderLocations().get(leader);

                if (lastLeaderLoc != null && !teamManager.hasMoved(lastLeaderLoc, leader.getLocation(), gameManager.isThirdGameActive())) {
                    event.setCancelled(true);
                    player.teleport(event.getFrom());
                }
            }
        }
    }
    
    @EventHandler
    public void onJumpInThirdGame(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!gameManager.isWaiting(player) || !gameManager.isThirdGameActive()) return;
        
        // Проверяем, прыгнул ли игрок
        if (event.getFrom().getY() < Objects.requireNonNull(event.getTo()).getY()) {
            gameManager.handleJumpSuccess(player);
        }
    }
    
    private void updateTeamPositions() {
        Team activeTeam = teamManager.getActiveTeam();
        int taskId = teamManager.getTaskId();
        if (activeTeam == null) {
            if (taskId != -1) {
                teamManager.setTaskId(-1);
            }
            return;
        }
        Player leader = activeTeam.getPlayer2();
        Location currentLoc = leader.getLocation().clone();
        Location lastLoc = teamManager.getLastLeaderLocations().get(leader);
        
        if (lastLoc != null && !teamManager.hasMoved(lastLoc, currentLoc, gameManager.isThirdGameActive())) {
            return;
        }
        teamManager.updatePlayerPositions(currentLoc);
        teamManager.getLastLeaderLocations().put(leader, currentLoc.clone());
    }
} 