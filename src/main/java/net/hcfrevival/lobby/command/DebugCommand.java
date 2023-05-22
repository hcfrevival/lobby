package net.hcfrevival.lobby.command;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPermissions;
import net.hcfrevival.lobby.LobbyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("debug")
public final class DebugCommand extends BaseCommand {
    @Getter public final LobbyPlugin plugin;

    @SuppressWarnings("UnstableApiUsage")
    @Subcommand("send")
    @Syntax("<server>")
    @Description("Navigate to a specified server")
    @CommandPermission(LobbyPermissions.ARES_LOBBY_MOD)
    public void onSend(Player player, String serverName) {
        player.sendMessage(ChatColor.RESET + "Now sending you to " + ChatColor.AQUA + serverName + ChatColor.RESET + "...");

        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
