package org.centipedegame.centipedgame.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.centipedegame.centipedgame.CentipedeGame;

public class TimerManager {
    private final CentipedeGame plugin;
    private int maxTime = 120;
    private int time = maxTime;
    private final BossBar bossBar;
    private BukkitRunnable task;
    private GameManager gameManager;

    public TimerManager(CentipedeGame plugin) {
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Оставшееся время: " + time, BarColor.GREEN, BarStyle.SOLID);
    }
    
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void startTimer() {
        if (task != null) {
            task.cancel();
        }

        bossBar.setVisible(true);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (time <= 0) {
                    stopTimer();
                    if (gameManager != null) {
                        gameManager.onTimeExceeded();
                    }
                    return;
                }
                bossBar.setTitle("Оставшееся время: " + time + " секунд");
                bossBar.setProgress((double) time / maxTime);
                time--;
            }
        };
        task.runTaskTimer(plugin, 0, 20);
    }

    public void setTimer(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Введите значение правильно, используя команду /centipede settimer [ЧИСЛО В СЕКУНДАХ]");
            return;
        }
        try {
            time = Integer.parseInt(args[1]);
            maxTime = time;
            player.sendMessage(ChatColor.GREEN + "Максимальное время изменено на " + time / 60 + " минут и " + (time - (time / 60) * 60) + " секунд!");
        } catch (NumberFormatException numberFormatException) {
            player.sendMessage(ChatColor.RED + "Введите корректное значение!");
        }
    }

    public void stopTimer() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        bossBar.removeAll();
        time = maxTime;
    }

    public void addPlayer(Player player) {
        bossBar.addPlayer(player);
    }
    
    public void resetTimer() {
        time = maxTime;
    }
    
    public int getMaxTime() {
        return maxTime;
    }
    
    public int getCurrentTime() {
        return time;
    }
} 