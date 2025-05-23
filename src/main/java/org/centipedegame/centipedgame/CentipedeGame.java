package org.centipedegame.centipedgame;

import org.bukkit.plugin.java.JavaPlugin;
import org.centipedegame.centipedgame.commands.CentipedeCommand;
import org.centipedegame.centipedgame.listeners.GameListener;
import org.centipedegame.centipedgame.listeners.MovementListener;
import org.centipedegame.centipedgame.managers.GameManager;
import org.centipedegame.centipedgame.managers.TeamManager;
import org.centipedegame.centipedgame.managers.TimerManager;

public class CentipedeGame extends JavaPlugin {
    private TeamManager teamManager;
    private TimerManager timerManager;
    private GameManager gameManager;
    @Override
    public void onEnable() {
        this.teamManager = new TeamManager();
        this.timerManager = new TimerManager(this);
        this.gameManager = new GameManager(this, teamManager, timerManager);
        getServer().getPluginManager().registerEvents(new GameListener(this, gameManager), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this, teamManager, gameManager), this);
        getCommand("centipede").setExecutor(new CentipedeCommand(this, teamManager, gameManager, timerManager));
    }
    @Override
    public void onDisable() {
        if (timerManager != null) {
            timerManager.stopTimer();
        }
        if (gameManager != null) {
            gameManager.stopAllGames();
        }
    }
}