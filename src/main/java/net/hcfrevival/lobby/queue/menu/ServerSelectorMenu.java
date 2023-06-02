package net.hcfrevival.lobby.queue.menu;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import gg.hcfactions.libs.bukkit.services.impl.sync.EServerStatus;
import gg.hcfactions.libs.bukkit.services.impl.sync.EServerType;
import gg.hcfactions.libs.bukkit.services.impl.sync.SyncService;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPlugin;
import net.hcfrevival.lobby.queue.model.impl.ServerQueue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public final class ServerSelectorMenu extends GenericMenu {
    @Getter public final LobbyPlugin plugin;

    public ServerSelectorMenu(LobbyPlugin plugin, Player player) {
        super(plugin, player, "Pick Server", 1);
        this.plugin = plugin;
    }

    @Override
    public void open() {
        super.open();

        addUpdater(() -> {
            final SyncService syncService = (SyncService) plugin.getService(SyncService.class);
            if (syncService == null) {
                plugin.getAresLogger().error("failed to obtain sync service");
                return;
            }

            clear();

            syncService.getServerRepository().stream().filter(s -> !s.getType().equals(EServerType.LOBBY)).forEach(server -> {
                final ServerQueue queue = plugin.getQueueManager().getServerQueues().get(server.getServerId());

                final ItemBuilder icon = new ItemBuilder()
                        .setMaterial(server.getType().getIconMaterial())
                        .setName(server.getName())
                        .addFlag(ItemFlag.HIDE_ATTRIBUTES);

                final List<String> lore = Lists.newArrayList();
                for (String desc : server.getDescription()) {
                    lore.add(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', desc));
                }

                String serverStatus = null;
                switch (server.getStatus()) {
                    case ONLINE -> serverStatus = ChatColor.GREEN + "Online";
                    case WHITELISTED ->  serverStatus = ChatColor.GRAY + "Whitelisted";
                    case OFFLINE -> serverStatus = ChatColor.RED + "Offline";
                }

                lore.add(ChatColor.RESET + " ");
                lore.add(ChatColor.GOLD + "Status" + ChatColor.YELLOW + ": " + serverStatus);
                lore.add(ChatColor.GOLD + "Online" + ChatColor.YELLOW + ": " + (server.getStatus().equals(EServerStatus.ONLINE) ? server.getOnlineUsernames().size() : 0));

                if (queue != null) {
                    lore.add(ChatColor.GOLD + "Queue" + ChatColor.YELLOW + ": " + queue.getQueue().size());
                }

                lore.add(ChatColor.RESET + " ");
                lore.add(ChatColor.GREEN + "Click to join!");
                icon.addLore(lore);

                addItem(new Clickable(icon.build(), server.getType().getIconPosition(), click -> {
                    player.closeInventory();

                    if (queue == null) {
                        player.sendMessage(ChatColor.RED + "Queue is disabled");
                        return;
                    }

                    plugin.getQueueManager().getExecutor().enterQueue(player, queue);
                }));
            });
        }, 20L);
    }
}
