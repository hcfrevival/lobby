package net.hcfrevival.lobby.queue.impl;

import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.services.impl.sync.EServerStatus;
import gg.hcfactions.libs.bukkit.services.impl.sync.impl.SyncServer;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPermissions;
import net.hcfrevival.lobby.item.LeaveQueueItem;
import net.hcfrevival.lobby.item.ServerSelectorItem;
import net.hcfrevival.lobby.queue.IQueueExecutor;
import net.hcfrevival.lobby.queue.QueueManager;
import net.hcfrevival.lobby.queue.menu.ServerSelectorMenu;
import net.hcfrevival.lobby.queue.model.impl.QueuedPlayer;
import net.hcfrevival.lobby.queue.model.impl.ServerQueue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public record QueueExecutor(@Getter QueueManager manager) implements IQueueExecutor {
    @Override
    public void enterQueue(Player player, ServerQueue queue) {
        final CustomItemService cis = (CustomItemService) manager.getPlugin().getService(CustomItemService.class);
        final RankService rankService = (RankService) manager.getPlugin().getService(RankService.class);

        if (cis == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain custom item service");
            return;
        }

        if (rankService == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain rank service");
            return;
        }

        final AresRank rank =  rankService.getHighestRank(player);
        final ServerQueue currentQueue = manager.getCurrentQueue(player);

        if (currentQueue != null) {
            currentQueue.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You have been removed from your current queue");
        }

        queue.add(player.getUniqueId(), rank);
        cis.getItem(LeaveQueueItem.class).ifPresent(item -> player.getInventory().setItem(4, item.getItem()));
        player.sendMessage(ChatColor.RESET + "Adding you to the " + queue.getServer().getName() + ChatColor.RESET + " queue...");
        player.sendMessage(ChatColor.AQUA + "You are now " + ChatColor.YELLOW + "#" + queue.getPosition(player.getUniqueId()) + ChatColor.AQUA + " in queue to join " + queue.getServer().getName());
    }

    @Override
    public void exitQueue(Player player) {
        final CustomItemService cis = (CustomItemService) manager.getPlugin().getService(CustomItemService.class);
        final ServerQueue currentQueue = manager.getCurrentQueue(player);

        if (cis == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain custom item service");
            return;
        }

        if (currentQueue == null) {
            player.sendMessage(ChatColor.RED + "You are not in an active queue");
            return;
        }

        currentQueue.remove(player.getUniqueId());
        cis.getItem(ServerSelectorItem.class).ifPresent(item -> player.getInventory().setItem(4, item.getItem()));
        player.sendMessage(ChatColor.YELLOW + "You are no longer in queue for " + ChatColor.RESET + currentQueue.getServer().getName());
    }

    @Override
    public void processQueues() {
        for (SyncServer server : manager.getQueueSnapshot().keySet()) {
            final ServerQueue queue = manager.getServerQueues().get(server.getServerId());

            if (queue == null) {
                continue;
            }

            if (queue.getQueue().isEmpty()) {
                continue;
            }

            final QueuedPlayer qp = (QueuedPlayer) queue.getSortedQueue().get(0);
            if (qp == null) {
                continue;
            }

            final Player player = Bukkit.getPlayer(qp.getUniqueId());
            if (player == null) {
                queue.remove(qp.getUniqueId());
                continue;
            }

            if (server.getStatus().equals(EServerStatus.OFFLINE)) {
                continue;
            }

            if (server.getStatus().equals(EServerStatus.WHITELISTED) && !player.hasPermission(LobbyPermissions.ARES_LOBBY_MOD)) {
                continue;
            }

            if (server.getOnlineUsernames().size() >= server.getMaxPlayers()) {
                continue;
            }

            if (server.isPremiumRequired() && !player.hasPermission(LobbyPermissions.ARES_LOBBY_PREMIUM)) {
                continue;
            }

            queue.remove(qp.getUniqueId());
            server.send(player);
        }
    }

    @Override
    public void openServerSelector(Player player) {
        final ServerSelectorMenu menu = new ServerSelectorMenu(manager.getPlugin(), player);
        menu.open();
    }
}
