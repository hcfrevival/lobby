package net.hcfrevival.lobby.player.model;

import gg.hcfactions.libs.bukkit.scoreboard.AresScoreboard;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import lombok.Getter;
import lombok.Setter;
import net.hcfrevival.lobby.LobbyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public final class LobbyPlayer {
    @Getter public final LobbyPlugin plugin;
    @Getter public UUID uniqueId;
    @Getter public String username;
    @Getter @Setter public AresScoreboard scoreboard;

    public LobbyPlayer(LobbyPlugin plugin, Player player) {
        this.plugin = plugin;
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.scoreboard = new AresScoreboard(plugin, player, plugin.getConfiguration().getScoreboardTitle());

        initScoreboard();
    }

    public void addToScoreboard(Player player) {
        final RankService rankService = (RankService) plugin.getService(RankService.class);

        if (rankService == null) {
            plugin.getAresLogger().error("Rank Service is null, failed to initialize scoreboard for " + username);
            return;
        }

        final AresRank highestRank = rankService.getHighestRank(player);

        if (highestRank == null) {
            return;
        }

        final Team team = scoreboard.getInternal().getTeam(highestRank.getName());

        if (team != null) {
            team.addEntry(player.getName());
        }
    }

    public void removeFromScoreboard(Player player) {
        scoreboard.getInternal().getTeams().forEach(team -> team.removeEntry(player.getName()));
    }

    private void initScoreboard() {
        final RankService rankService = (RankService) plugin.getService(RankService.class);

        if (rankService == null) {
            plugin.getAresLogger().error("Rank Service is null, failed to initialize scoreboard for " + username);
            return;
        }

        final Scoreboard internal = scoreboard.getInternal();

        rankService.getRankRepository().forEach(rank -> {
            final Team team = internal.registerNewTeam(rank.getName());
            team.setPrefix(rank.getPrefix());
        });

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            final AresRank highestRank = rankService.getHighestRank(onlinePlayer);

            if (highestRank != null) {
                final Team team = internal.getTeam(highestRank.getName());

                if (team != null) {
                    team.addEntry(onlinePlayer.getName());
                }
            }
        });
    }
}
