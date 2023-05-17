package net.hcfrevival.lobby.queue.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.services.impl.sync.impl.SyncServer;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public interface IServerQueue {
    /**
     * @return Server this queue is attached to
     */
    SyncServer getServer();

    /**
     * @return Queue player data
     */
    List<? extends IQueuedPlayer> getQueue();

    /**
     * @param uniqueId UUID to add
     * @param rank Rank to determine position
     */
    void add(UUID uniqueId, AresRank rank);

    /**
     * @param uniqueId UUID to remove
     */
    default void remove(UUID uniqueId) {
        getQueue().removeIf(qp -> qp.getUniqueId().equals(uniqueId));
    }

    /**
     * Returns the position in the queue for the provided UUID
     * @param uniqueId UUID to query
     */
    default int getPosition(UUID uniqueId) {
        int pos = 1;

        for (IQueuedPlayer qp : getSortedQueue()) {
            if (qp.getUniqueId().equals(uniqueId)) {
                break;
            }

            pos += 1;
        }

        return pos;
    }

    /**
     * @param uniqueId UUID to query
     * @return True if the provided UUID is in this queue
     */
    default boolean isQueueing(UUID uniqueId) {
        return getQueue().stream().anyMatch(q -> q.getUniqueId().equals(uniqueId));
    }

    /**
     * @return Sorted queue by weight
     */
    default ImmutableList<? extends IQueuedPlayer> getSortedQueue() {
        final List<IQueuedPlayer> players = Lists.newArrayList(getQueue());

        players.sort((o1, o2) -> {
            if (o1.getWeight() > o2.getWeight()) {
                return -1;
            }

            if (o1.getWeight() < o2.getWeight()) {
                return 1;
            }

            // weight is the same, compare join time
            // join time is also the same? no fuarking way but okay
            if (o1.getJoinTime() == o2.getJoinTime()) {
                return 0;
            }

            // joined earlier
            if (o1.getJoinTime() <= o2.getJoinTime()) {
                return -1;
            }

            // joined later
            return 1;
        });

        return ImmutableList.copyOf(players);
    }
}
