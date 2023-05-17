package net.hcfrevival.lobby.queue.listener;

import gg.hcfactions.libs.bukkit.services.impl.sync.event.ServerSyncEvent;
import lombok.Getter;
import net.hcfrevival.lobby.queue.QueueManager;
import net.hcfrevival.lobby.queue.model.impl.ServerQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public record QueueListener(@Getter QueueManager manager) implements Listener {
    @EventHandler /* Updates the server queues with current servers */
    public void onServerSync(ServerSyncEvent event) {
        // TODO: This is kinda open-ended, leaving room for issues with scaling. Refactor if we make it
        event.getServers().forEach(server -> manager.getServerQueues().putIfAbsent(server.getServerId(), new ServerQueue(server)));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ServerQueue queue = manager.getCurrentQueue(player);

        if (queue == null) {
            return;
        }

        queue.remove(player.getUniqueId());
    }
}
