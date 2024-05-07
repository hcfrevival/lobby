package net.hcfrevival.lobby.command;

import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPermissions;
import net.hcfrevival.lobby.LobbyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("spawn")
public final class SpawnCommand extends BaseCommand {
    @Getter public final LobbyPlugin plugin;

    @Default
    @Description("Return to spawn")
    public void onSpawn(Player player) {
        player.teleport(plugin.getConfiguration().getSpawnLocation());
        player.sendMessage(Component.text("Returned to Spawn", NamedTextColor.YELLOW));
    }

    @Subcommand("set")
    @CommandPermission(LobbyPermissions.ARES_LOBBY_ADMIN)
    @Description("Update the spawn location to your current location")
    public void onSetSpawn(Player player) {
        plugin.getConfiguration().saveSpawnLocation(player);
        plugin.getConfiguration().setSpawnLocation(player.getLocation());
        player.sendMessage(Component.text("Spawn has been updated", NamedTextColor.GREEN));
    }
}
