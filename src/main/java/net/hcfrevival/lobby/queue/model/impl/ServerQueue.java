package net.hcfrevival.lobby.queue.model.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.services.impl.sync.impl.SyncServer;
import lombok.Getter;
import net.hcfrevival.lobby.queue.model.IServerQueue;

import java.util.List;
import java.util.UUID;

public final class ServerQueue implements IServerQueue {
    @Getter public final SyncServer server;
    @Getter public List<QueuedPlayer> queue;

    public ServerQueue(SyncServer server) {
        this.server = server;
        this.queue = Lists.newArrayList();
    }

    @Override
    public void add(UUID uniqueId, AresRank rank) {
        final int weight = (rank != null) ? rank.getWeight() : 0;
        queue.add(new QueuedPlayer(uniqueId, weight));
    }
}
