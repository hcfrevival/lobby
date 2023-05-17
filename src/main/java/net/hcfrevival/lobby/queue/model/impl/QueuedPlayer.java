package net.hcfrevival.lobby.queue.model.impl;

import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import net.hcfrevival.lobby.queue.model.IQueuedPlayer;

import java.util.UUID;

public final class QueuedPlayer implements IQueuedPlayer {
    @Getter public final UUID uniqueId;
    @Getter public final long joinTime;
    @Getter public final int weight;

    public QueuedPlayer(UUID uniqueId, int weight) {
        this.uniqueId = uniqueId;
        this.joinTime = Time.now();
        this.weight = weight;
    }
}
