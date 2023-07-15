package net.hcfrevival.lobby.listener;

import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPermissions;
import net.hcfrevival.lobby.LobbyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public record PremiumListener(@Getter LobbyPlugin plugin) implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (!player.hasPermission(LobbyPermissions.ARES_LOBBY_PREMIUM)) {
            return;
        }

        final ItemStack elytra = new ItemBuilder()
                .setMaterial(Material.ELYTRA)
                .setName(ChatColor.GOLD + "Thank you for your support!")
                .addEnchant(Enchantment.DURABILITY, 3)
                .build();

        final ItemStack fireworks = new ItemBuilder()
                .setMaterial(Material.FIREWORK_ROCKET)
                .setName(ChatColor.GOLD + "Thank you for your support!")
                .setAmount(64)
                .build();

        player.getInventory().setChestplate(elytra);
        player.getInventory().setItem(0, fireworks);
    }
}
