package net.hcfrevival.lobby.listener;

import com.google.common.base.Joiner;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.libs.bukkit.events.impl.PlayerBigMoveEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.utils.Players;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPermissions;
import net.hcfrevival.lobby.LobbyPlugin;
import net.hcfrevival.lobby.item.ServerSelectorItem;
import net.hcfrevival.lobby.player.model.LobbyPlayer;
import net.hcfrevival.lobby.util.ScoreboardUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public record PlayerListener(@Getter LobbyPlugin plugin) implements Listener {
    private void checkPermissions(Player player, Cancellable cancellable) {
        if (player.hasPermission(LobbyPermissions.ARES_LOBBY_ADMIN)) {
            return;
        }

        cancellable.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final LobbyPlayer lobbyPlayer = new LobbyPlayer(plugin, player);

        Players.resetHealth(player);
        Players.resetFlySpeed(player);
        Players.resetWalkSpeed(player);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setWalkSpeed(0.4f);
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(plugin.getConfiguration().getSpawnLocation());

        plugin.getPlayerManager().getPlayerRepository().add(lobbyPlayer);
        plugin.getPlayerManager().getPlayerRepository().forEach(p -> p.addToScoreboard(player));
        new Scheduler(plugin).sync(() -> ScoreboardUtil.sendLobbyScoreboard(plugin, player)).delay(5L).run();

        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);
        if (cis != null) {
            cis.getItem(ServerSelectorItem.class).ifPresent(item -> player.getInventory().setItem(4, item.getItem()));
        }

        final CXService cxService = (CXService) plugin.getService(CXService.class);
        final RankService rankService = (RankService) plugin.getService(RankService.class);
        AresRank rank = null;

        if (cxService != null && rankService != null) {
            rank = rankService.getHighestRank(player);

            if (rank != null) {
                player.setPlayerListName(rankService.getFormattedName(player));

                if (!cxService.getVanishManager().isVanished(player)) {
                    Bukkit.broadcastMessage(rankService.getFormattedName(player) + ChatColor.YELLOW + " has joined the lobby");
                }
            }
        }

        new Scheduler(plugin).sync(() ->
                player.sendMessage(
                        Joiner.on("\n").join(plugin.getConfiguration().getMotd()).replaceAll("%player%", player.getName())))
                .delay(1L)
                .run();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        plugin.getPlayerManager().getPlayerRepository().removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
        plugin.getPlayerManager().getPlayerRepository().forEach(p -> p.removeFromScoreboard(player));
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(PlayerDamagePlayerEvent event) {
        final Player damager = event.getDamager();
        final Player damaged = event.getDamaged();

        event.setCancelled(true);

        if (damaged.hasPermission(LobbyPermissions.ARES_LOBBY_MOD) || damaged.hasPermission(LobbyPermissions.ARES_LOBBY_ADMIN)) {
            return;
        }

        // Players.spawnParticle(damager, damaged.getLocation().add(0, 1.0, 0), Particle.BUBBLE_POP, 16);
        Players.playSound(damager, Sound.ENTITY_ITEM_PICKUP);

        damager.hidePlayer(plugin, damaged);
        damager.sendMessage(ChatColor.AQUA + "Pop!");
    }

    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final Block floorBlock = event.getTo().getBlock();

        if (!floorBlock.getType().name().contains("_PLATE")) {
            return;
        }

        final Vector velocity = player.getLocation().getDirection();

        if (floorBlock.getType().equals(Material.OAK_PRESSURE_PLATE)) {
            velocity.setY(velocity.getY() + 0.3);
            velocity.multiply(3.00);
            Players.playSound(player, Sound.ENTITY_ENDER_DRAGON_FLAP);
        }

        if (floorBlock.getType().equals(Material.STONE_PRESSURE_PLATE)) {
            velocity.setY(velocity.getY() + 0.3);
            velocity.multiply(4.00);
            Players.playSound(player, Sound.ENTITY_ENDER_DRAGON_FLAP);
        }

        if (floorBlock.getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
            velocity.setY(velocity.getY() + 0.2);
            velocity.multiply(5.00);
            Players.playSound(player, Sound.ENTITY_ENDER_DRAGON_FLAP);
        }

        if (floorBlock.getType().equals(Material.HEAVY_WEIGHTED_PRESSURE_PLATE)) {
            velocity.setY(velocity.getY() + 0.15);
            velocity.multiply(6.00);
            Players.playSound(player, Sound.ENTITY_ENDER_DRAGON_FLAP);
        }

        player.setVelocity(velocity);
    }

    @EventHandler
    public void onVoidFalling(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final Location to = event.getTo();

        if (to.getY() <= 0 || to.getY() > 300) {
            player.teleport(plugin.getConfiguration().getSpawnLocation());
            player.setFallDistance(0);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            player.teleport(plugin.getConfiguration().getSpawnLocation());
            player.setFallDistance(0);
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        event.setFoodLevel(10);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        checkPermissions(event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        checkPermissions(event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        checkPermissions(event.getPlayer(), event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        checkPermissions(player, event);
    }
}
