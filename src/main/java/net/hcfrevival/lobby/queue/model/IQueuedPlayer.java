package net.hcfrevival.lobby.queue.model;

import java.util.UUID;

public interface IQueuedPlayer {
    UUID getUniqueId();
    long getJoinTime();
    int getWeight();
}
