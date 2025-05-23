package org.centipedegame.centipedgame.managers;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.centipedegame.centipedgame.CentipedeGame;
import org.centipedegame.centipedgame.model.BlockInfo;
import org.centipedegame.centipedgame.model.Team;
import org.centipedegame.centipedgame.utils.MessageUtils;

import java.util.*;

public class GameManager {
    private final CentipedeGame plugin;
    private final TeamManager teamManager;
    private final TimerManager timerManager;
    private boolean isFirstGameActive = false;
    private boolean isFirstGamePass = false;
    private boolean isSecondGameActive = false;
    private boolean isSecondGamePass = false;
    private boolean isThirdGameActive = false;
    private boolean isThirdGamePass = false;
    private boolean firstTarget = false;
    private boolean secondTarget = false;
    private boolean thirdTarget = false;
    
    private final Random random = new Random();
    private final HashMap<Player, Integer> rounds = new HashMap<>();
    private final HashMap<Player, Boolean> waiting = new HashMap<>();
    private int delayOfThirdGame = 15;
    private Location locationOfRedBlock = null;
    private final List<BlockInfo> blockInfos = new ArrayList<>();
    public GameManager(CentipedeGame plugin, TeamManager teamManager, TimerManager timerManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.timerManager = timerManager;
        this.timerManager.setGameManager(this);
    }
    public void setRedBlock(Player player) {
        locationOfRedBlock = player.getLocation().getBlock().getLocation();
        player.sendMessage(ChatColor.GREEN + "Позиция для красного блока установлена!");
    }
    public void placeRedBlock() {
        if (locationOfRedBlock != null) {
            locationOfRedBlock.getBlock().setType(Material.RED_CONCRETE);
        }
    }
    public void startEvent(Player player) {
        Team activeTeam = teamManager.getActiveTeam();
        if (activeTeam == null) {
            player.sendMessage("Сначала выберите активную команду");
            return;
        }
        if (teamManager.getLocationOfOrganizator() == null) {
            player.sendMessage("Сначала поставьте локацию для активной команды");
            return;
        }
        if (isAnyGameActive()) {
            player.sendMessage("Игра уже запущена, сначала завершите игру!");
            return;
        }
        if (!isFirstGamePass) {
            startFirstGame(activeTeam);
            return;
        }
        if (!isSecondGamePass) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    startSecondGame(activeTeam);
                }
            }.runTaskLater(plugin, 60);
            return;
        }
        if (!isThirdGamePass) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    startThirdGame();
                }
            }.runTaskLater(plugin, 60);
        }
    }
    private void startFirstGame(Team team) {
        MessageUtils.sendTeamTitle(team, ChatColor.GREEN + "3", "");
        new BukkitRunnable() {
            @Override
            public void run() {
                MessageUtils.sendTeamTitle(team, ChatColor.YELLOW + "2", "");
            }
        }.runTaskLater(plugin, 50);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                MessageUtils.sendTeamTitle(team, ChatColor.RED + "1", "");
            }
        }.runTaskLater(plugin, 100);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                MessageUtils.sendTeamTitle(team, ChatColor.YELLOW + "GO", "");
                timerManager.addPlayer(team.getPlayer1());
                timerManager.addPlayer(team.getPlayer2());
                timerManager.addPlayer(team.getPlayer3());
                timerManager.addPlayer(team.getOrganizator());
                teamManager.setStopMoving(false);
                timerManager.startTimer();
                
                Player player2 = team.getPlayer2();
                Location loc = player2.getLocation().add(0, 2, 0);
                Firework firework = player2.getWorld().spawn(loc, Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withColor(Color.RED, Color.YELLOW)
                        .withFade(Color.ORANGE)
                        .withFlicker()
                        .build());
                meta.setPower(1);
                firework.setFireworkMeta(meta);
                player2.playSound(player2.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
            }
        }.runTaskLater(plugin, 150);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                handleFirstGame();
            }
        }.runTaskLater(plugin, 230);
    }

    public void continueEvent() {
        Team activeTeam = teamManager.getActiveTeam();
        if (activeTeam == null || teamManager.getLocationOfOrganizator() == null || isAnyGameActive()) {
            return;
        }
        if (!isFirstGamePass) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    handleFirstGame();
                }
            }.runTaskLater(plugin, 60);
            return;
        }
        if (!isSecondGamePass) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    startSecondGame(activeTeam);
                }
            }.runTaskLater(plugin, 60);
            return;
        }
        if (!isThirdGamePass) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    startThirdGame();
                }
            }.runTaskLater(plugin, 60);
        }
    }
    public void stopGame(Player player) {
        resetGameState();
        timerManager.stopTimer();
        teamManager.handleDisbandTeam(player);
        teamManager.setStopMoving(true);
    }
    public void stopAllGames() {
        resetGameState();
        timerManager.stopTimer();
        teamManager.handleDisbandTeam();
        teamManager.setStopMoving(true);
    }
    private void resetGameState() {
        isSecondGameActive = false;
        isFirstGameActive = false;
        isThirdGameActive = false;
        isFirstGamePass = false;
        isSecondGamePass = false;
        isThirdGamePass = false;
        firstTarget = false;
        secondTarget = false;
        thirdTarget = false;
        rounds.clear();
        waiting.clear();
    }
    // Первая
    private void handleFirstGame() {
        Team team = teamManager.getActiveTeam();
        if (team == null) return;
        
        ItemStack blueTtakji = new ItemStack(Material.BLUE_CONCRETE);
        ItemMeta meta = blueTtakji.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.BLUE + "СИНИЙ ТТАКДЖИ");
        blueTtakji.setItemMeta(meta);
        team.getPlayer1().getInventory().addItem(blueTtakji);
        MessageUtils.sendTeamMessage(team, "§aИгрок 1 должен кинуть синий ттакджи на красный!");
        isFirstGameActive = true;
        placeRedBlock();
    }
    public void handleFirstGameInteract(Player player, Location clickLocation, ItemStack item) {
        Team team = teamManager.getActiveTeam();
        if (team == null || !isFirstGameActive || !team.getPlayer1().equals(player)) return;
        
        item.setAmount(item.getAmount() - 1);
        
        player.getWorld().spawnParticle(Particle.REDSTONE, clickLocation.add(0.5, 0.5, 0.5), 80, 0.3, 0.3, 0.3, 1,
                new Particle.DustOptions(Color.fromRGB(255, 50, 50), 3.5f));
        player.getWorld().spawnParticle(Particle.FLAME, clickLocation, 15, 0.2, 0.2, 0.2, 0.05);
        
        Material clickedBlockType = clickLocation.getBlock().getType();
        
        if (Math.random() < 0.33 && clickedBlockType == Material.RED_CONCRETE) {
            clickLocation.getBlock().setType(Material.RED_CONCRETE_POWDER);
            MessageUtils.sendTeamTitle(team, "§aУСПЕХ!", "Ттакджи перевернута");
            isFirstGameActive = false;
            isFirstGamePass = true;
            continueEvent();
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, clickLocation, 50, 0.5, 0.5, 0.5, 0.3);
        } else {
            int firstGameDelay = 5;
            if (clickedBlockType == Material.RED_CONCRETE) {
                MessageUtils.sendTeamActionBar(team, "§cТтакджи не перевернулась! Новый блок через " + (firstGameDelay) + " секунд");
            } else {
                MessageUtils.sendTeamActionBar(team, "§cВы кинули ттакджи не туда! Новый блок через " + (firstGameDelay) + " секунд");
            }
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isFirstGameActive && team.getPlayer1().isOnline()) {
                        ItemStack blueTtakji = new ItemStack(Material.BLUE_CONCRETE);
                        ItemMeta meta = blueTtakji.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.BLUE + "СИНИЙ ТТАКДЖИ");
                        blueTtakji.setItemMeta(meta);
                        team.getPlayer1().getInventory().addItem(blueTtakji);
                    }
                }
            }.runTaskLater(plugin, firstGameDelay * 20L);
        }
    }
    
    // Вторая
    private void startSecondGame(Team team) {
        isSecondGameActive = true;
        Player shooter = team.getPlayer2();
        shooter.getInventory().addItem(new ItemStack(Material.SNOWBALL, 1));
        MessageUtils.sendTeamMessage(team, "§aКидай снежки в голову зомби!");
    }
    
    public void handleProjectileHit(Player shooter, Location hitLocation, Material hitBlockType) {
        Team team = teamManager.getActiveTeam();
        if (team == null || !isSecondGameActive) return;

        if (hitBlockType == Material.ZOMBIE_HEAD) {
            blockInfos.add(new BlockInfo(hitLocation, hitBlockType));
            
            MessageUtils.sendTeamTitle(team, "§eОтлично!", "§eМишень поражена!");
            if (!firstTarget) {
                firstTarget = true;
                hitLocation.getBlock().setType(Material.AIR);
                giveSnowball(shooter, team);
            } else if (!secondTarget) {
                secondTarget = true;
                hitLocation.getBlock().setType(Material.AIR);
                giveSnowball(shooter, team);
            } else if (!thirdTarget) {
                thirdTarget = true;
                hitLocation.getBlock().setType(Material.AIR);
                giveSnowball(shooter, team);
            }
            
            if (secondTarget && thirdTarget) {
                MessageUtils.sendTeamTitle(team, "§aОтлично!", "§aВсе 3 мишени поражены!");
                firstTarget = false;
                secondTarget = false;
                thirdTarget = false;
                isSecondGameActive = false;
                isSecondGamePass = true;
                shooter.getInventory().clear();
                restoreBlocks();
                continueEvent();
            }
        } else {
            handleMiss(shooter, team);
        }
    }
    
    private void giveSnowball(Player shooter, Team team) {
        if (team == null || !isSecondGameActive) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isSecondGameActive && shooter.isOnline()) {
                    shooter.getInventory().addItem(new ItemStack(Material.SNOWBALL));
                }
            }
        }.runTaskLater(plugin, 40);
    }
    
    private void restoreBlocks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (BlockInfo blockInfo : blockInfos) {
                    Location loc = blockInfo.getLocation();
                    Material type = blockInfo.getMaterial();
                    loc.getBlock().setType(type);
                }
                blockInfos.clear();
            }
        }.runTaskLater(plugin, 100L);
    }
    
    private void handleMiss(Player shooter, Team team) {
        if (team == null || !isSecondGameActive) return;
        MessageUtils.sendTeamActionBar(team, "§cПромах! Новый снежок через 5 секунд");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isSecondGameActive && shooter.isOnline()) {
                    shooter.getInventory().addItem(new ItemStack(Material.SNOWBALL));
                }
            }
        }.runTaskLater(plugin, 100);
    }
    
    // Третья
    private void startThirdGame() {
        Team activeTeam = teamManager.getActiveTeam();
        if (activeTeam == null) return;
        
        Player player = activeTeam.getPlayer3();
        isThirdGameActive = true;
        rounds.put(player, 1);
        waiting.put(player, false);
        scheduleJumpMessage(player);
    }
    
    private void scheduleJumpMessage(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Team activeTeam = teamManager.getActiveTeam();
                if (activeTeam == null) return;
                
                int delay = random.nextInt(200) + 1;
                int round = rounds.getOrDefault(player, 1);
                MessageUtils.sendTeamMessage(activeTeam, "§7§lРаунд " + round + " начинается!");
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isThirdGameActive && player.isOnline()) {
                            player.sendTitle("§6НАЖМИ ПРОБЕЛ", "", 0, 40, 10);
                            waiting.put(player, true);
                            startReactionTimer(player);
                        }
                    }
                }.runTaskLater(plugin, delay);
            }
        }.runTaskLater(plugin, 40);
    }
    
    private void startReactionTimer(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Team activeTeam = teamManager.getActiveTeam();
                if (activeTeam == null || !waiting.getOrDefault(player, false)) return;
                
                if (waiting.get(player)) {
                    player.sendMessage("§cНе успел! Все начинается заново.");
                    startThirdGame();
                }
            }
        }.runTaskLater(plugin, delayOfThirdGame);
    }
    
    public void handleJumpSuccess(Player player) {
        Team activeTeam = teamManager.getActiveTeam();
        if (activeTeam == null) return;
        
        waiting.put(player, false);
        int currentRound = rounds.getOrDefault(player, 1);

        if (currentRound >= 5) {
            MessageUtils.sendTeamTitle(activeTeam, "§aПОБЕДА!", "Все раунды пройдены!");
            rounds.remove(player);
            isThirdGameActive = false;
            isThirdGamePass = true;
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    stopGaming();
                }
            }.runTaskLater(plugin, 70);
            return;
        }

        MessageUtils.sendTeamMessage(activeTeam, "§aУспех! Раунд " + (currentRound + 1) + "/5");
        rounds.put(player, currentRound + 1);
        scheduleJumpMessage(player);
    }
    
    private void stopGaming() {
        if (isFirstGamePass && isSecondGamePass && isThirdGamePass) {
            Team activeTeam = teamManager.getActiveTeam();
            if (activeTeam != null) {
                MessageUtils.sendTeamTitle(activeTeam, "§a§lПоздравляю!", "§a§lВы проходите дальше.");
            }
            stopAllGames();
        }
    }
    
    public void setThirdGameDelay(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Введите значение в тиках!");
            return;
        }
        try {
            delayOfThirdGame = Integer.parseInt(args[1]);
            player.sendMessage(ChatColor.GREEN + "Значение задержки для 3 игры установлено на " + delayOfThirdGame + " тиков!");
        } catch (NumberFormatException numberFormatException) {
            player.sendMessage(ChatColor.RED + "Введите корректное значение!");
        }
    }
    
    public void onTimeExceeded() {
        Team activeTeam = teamManager.getActiveTeam();
        if (activeTeam == null) return;
        
        MessageUtils.sendTeamTitle(activeTeam, ChatColor.RED + "Время вышло!", "");
        Bukkit.broadcastMessage(ChatColor.RED + "Игрок " + activeTeam.getPlayer1().getName() + " выбыл!");
        Bukkit.broadcastMessage(ChatColor.RED + "Игрок " + activeTeam.getPlayer2().getName() + " выбыл!");
        Bukkit.broadcastMessage(ChatColor.RED + "Игрок " + activeTeam.getPlayer3().getName() + " выбыл!");
        
        activeTeam.getPlayer1().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, true));
        activeTeam.getPlayer2().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, true));
        activeTeam.getPlayer3().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, true));
        
        stopAllGames();
    }
    
    // Getters and setters
    public boolean isFirstGameActive() {
        return isFirstGameActive;
    }
    
    public boolean isSecondGameActive() {
        return isSecondGameActive;
    }
    
    public boolean isThirdGameActive() {
        return isThirdGameActive;
    }
    
    public boolean isAnyGameActive() {
        return isFirstGameActive || isSecondGameActive || isThirdGameActive;
    }
    
    public boolean isWaiting(Player player) {
        return waiting.getOrDefault(player, false);
    }
    
    public Location getLocationOfRedBlock() {
        return locationOfRedBlock;
    }
    
    public void setLocationOfRedBlock(Location locationOfRedBlock) {
        this.locationOfRedBlock = locationOfRedBlock;
    }
} 