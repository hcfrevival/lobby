package net.hcfrevival.lobby.player;

import com.google.common.collect.Sets;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPlugin;
import net.hcfrevival.lobby.player.model.LobbyPlayer;
import net.hcfrevival.lobby.util.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class PlayerManager {
    @Getter public final LobbyPlugin plugin;
    @Getter public final Set<LobbyPlayer> playerRepository;
    @Getter public BukkitTask scoreboardTask;

    public PlayerManager(LobbyPlugin plugin) {
        this.plugin = plugin;
        this.playerRepository = Sets.newConcurrentHashSet();
    }

    public void onEnable() {
        this.scoreboardTask = new Scheduler(plugin).async(() -> {
            playerRepository.forEach(lobbyPlayer -> {
                final Player bukkitPlayer = Bukkit.getPlayer(lobbyPlayer.getUniqueId());

                if (bukkitPlayer != null) {
                    ScoreboardUtil.sendLobbyScoreboard(plugin, bukkitPlayer);
                }
            });
        }).repeat(0L, 20L).run();
    }

    public void onDisable() {
        this.scoreboardTask.cancel();
        this.scoreboardTask = null;
    }

    public Optional<LobbyPlayer> getPlayer(Predicate<LobbyPlayer> pred) {
        return playerRepository.stream().filter(pred).findFirst();
    }
}
