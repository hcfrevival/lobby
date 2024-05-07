package net.hcfrevival.lobby.queue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.sync.SyncService;
import gg.hcfactions.libs.bukkit.services.impl.sync.impl.SyncServer;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPlugin;
import net.hcfrevival.lobby.queue.impl.QueueExecutor;
import net.hcfrevival.lobby.queue.listener.QueueListener;
import net.hcfrevival.lobby.queue.model.impl.QueuedPlayer;
import net.hcfrevival.lobby.queue.model.impl.ServerQueue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public final class QueueManager {
    @Getter public final LobbyPlugin plugin;
    @Getter public final QueueExecutor executor;
    @Getter public Map<Integer, ServerQueue> serverQueues;
    @Getter public BukkitTask queueProcessor;
    @Getter public BukkitTask queueNotifier;

    public QueueManager(LobbyPlugin plugin) {
        this.plugin = plugin;
        this.executor = new QueueExecutor(this);
        this.serverQueues = Maps.newConcurrentMap();

        this.queueProcessor = new Scheduler(plugin).sync(executor::processQueues).repeat(0L, 20L).run();

        this.queueNotifier = new Scheduler(plugin).sync(() -> {
            serverQueues.values().forEach(queue -> queue.getQueue().forEach(qp -> {
                final Player player = Bukkit.getPlayer(qp.getUniqueId());

                if (player != null) {
                    player.sendMessage(ChatColor.AQUA + "You are currently " + ChatColor.YELLOW + "#" + queue.getPosition(player.getUniqueId()) + ChatColor.AQUA + " in queue to join " + queue.getServer().getName());
                }
            }));
        }).repeat(20L, 10 * 20L).run();

        plugin.registerListener(new QueueListener(this));
    }

    /**
     * Returns a snapshot of the queue with synced servers
     * @return Immutable Map of Sync servers and their respective queues
     */
    public ImmutableMap<SyncServer, ServerQueue> getQueueSnapshot() {
        final SyncService syncService = (SyncService) plugin.getService(SyncService.class);
        if (syncService == null) {
            plugin.getAresLogger().error("Attempted to grab a snapshot of the queue but the sync service was null");
            return ImmutableMap.of();
        }

        final Map<SyncServer, ServerQueue> res = Maps.newHashMap();
        serverQueues.forEach((serverId, serverQueue) -> syncService.getServerRepository()
                .stream()
                .filter(s -> s.getServerId() == serverId)
                .findFirst()
                .ifPresent(s -> res.put(s, serverQueue)));

        return ImmutableMap.copyOf(res);
    }

    /**
     * Return the current queue for the provided player
     * @param player Player
     * @return ServerQueue
     */
    public ServerQueue getCurrentQueue(Player player) {
        for (ServerQueue queue : serverQueues.values()) {
            for (QueuedPlayer qp : queue.getQueue()) {
                if (qp.getUniqueId().equals(player.getUniqueId())) {
                    return queue;
                }
            }
        }

        return null;
    }
}
