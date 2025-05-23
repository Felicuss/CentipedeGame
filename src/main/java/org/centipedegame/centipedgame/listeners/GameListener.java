package org.centipedegame.centipedgame.listeners;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.centipedegame.centipedgame.CentipedeGame;
import org.centipedegame.centipedgame.managers.GameManager;

public class GameListener implements Listener {
    private final CentipedeGame plugin;
    private final GameManager gameManager;
    public GameListener(CentipedeGame plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getItem() != null
                && event.getItem().getType() == org.bukkit.Material.BLUE_CONCRETE) {
            
            Player player = event.getPlayer();
            if (event.getClickedBlock() == null) return;
            
            if (gameManager.isFirstGameActive()) {
                event.setCancelled(true);
                gameManager.handleFirstGameInteract(player, event.getClickedBlock().getLocation(), event.getItem());
            }
        }
    }
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball && event.getEntity().getShooter() instanceof Player shooter) {
            if (!gameManager.isSecondGameActive()) return;
            
            if (event.getHitBlock() != null) {
                gameManager.handleProjectileHit(shooter, event.getHitBlock().getLocation(), event.getHitBlock().getType());
            }
        }
    }
} 