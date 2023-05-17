package net.hcfrevival.lobby.queue;

import net.hcfrevival.lobby.queue.model.impl.ServerQueue;
import org.bukkit.entity.Player;

public interface IQueueExecutor {
    QueueManager getManager();
    void enterQueue(Player player, ServerQueue queue);
    void exitQueue(Player player);
    void processQueues();
    void openServerSelector(Player player);
}
