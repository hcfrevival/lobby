package net.hcfrevival.lobby.util;

import gg.hcfactions.libs.bukkit.scoreboard.AresScoreboard;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.services.impl.sync.SyncService;
import gg.hcfactions.libs.bukkit.services.impl.sync.impl.SyncServer;
import joptsimple.internal.Strings;
import net.hcfrevival.lobby.LobbyPlugin;
import net.hcfrevival.lobby.queue.model.impl.ServerQueue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class ScoreboardUtil {
    public static void applyScoreboardTemplate(LobbyPlugin plugin, AresScoreboard scoreboard) {
        scoreboard.setLine(0, ChatColor.RESET + "" + ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat('-', 24));
        scoreboard.setLine(2, ChatColor.RESET + " ");
        scoreboard.setLine(1, plugin.getConfiguration().getScoreboardFooter());
        scoreboard.setLine(63, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat('-', 24));
    }

    public static void sendLobbyScoreboard(LobbyPlugin plugin, Player player) {
        final ServerQueue queueData = plugin.getQueueManager().getCurrentQueue(player);
        final SyncService syncService = (SyncService) plugin.getService(SyncService.class);
        final RankService rankService = (RankService) plugin.getService(RankService.class);

        plugin.getPlayerManager().getPlayer(p -> p.getUniqueId().equals(player.getUniqueId())).ifPresent(lobbyPlayer -> {
            applyScoreboardTemplate(plugin, lobbyPlayer.getScoreboard());

            if (rankService != null) {
                final AresRank highestRank = rankService.getHighestRank(player);
                lobbyPlayer.getScoreboard().setLine(11, ChatColor.GOLD + "Rank" + ChatColor.YELLOW + ":");
                lobbyPlayer.getScoreboard().setLine(10, (highestRank != null)
                        ? net.md_5.bungee.api.ChatColor.of(highestRank.getColorCode()) + highestRank.getDisplayName()
                        : ChatColor.RESET + "Default");
            }

            if (syncService != null) {
                if (rankService != null) {
                    lobbyPlayer.getScoreboard().setLine(9, ChatColor.RESET + " ");
                }

                int totalOnline = syncService.getThisServer().getOnlineUsernames().size();

                for (SyncServer server : syncService.getServerRepository()) {
                    totalOnline += server.getOnlineUsernames().size();
                }

                lobbyPlayer.getScoreboard().setLine(8, ChatColor.GOLD + "Online" + ChatColor.YELLOW + ":");
                lobbyPlayer.getScoreboard().setLine(7, totalOnline + "");
            }

            if (queueData != null) {
                if (syncService != null) {
                    lobbyPlayer.getScoreboard().setLine(6, ChatColor.RESET + " ");
                }

                final String serverName = queueData.getServer().getType().getDisplayName();
                final int pos = queueData.getPosition(player.getUniqueId());
                final int queueSize = queueData.getQueue().size();

                lobbyPlayer.getScoreboard().setLine(5, ChatColor.GOLD + "Queue" + ChatColor.YELLOW + ":");
                lobbyPlayer.getScoreboard().setLine(4, serverName);
                lobbyPlayer.getScoreboard().setLine(3, ChatColor.ITALIC + "" + pos + " of " + queueSize);
            } else {
                lobbyPlayer.getScoreboard().removeLine(5);
                lobbyPlayer.getScoreboard().removeLine(4);
                lobbyPlayer.getScoreboard().removeLine(3);
            }

            if (player.getScoreboard() != lobbyPlayer.getScoreboard().getInternal()) {
                player.setScoreboard(lobbyPlayer.getScoreboard().getInternal());
            }
        });
    }
}
