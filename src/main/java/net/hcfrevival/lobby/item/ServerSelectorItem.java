package net.hcfrevival.lobby.item;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public record ServerSelectorItem(@Getter LobbyPlugin plugin) implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.COMPASS;
    }

    @Override
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "ServerSelector");
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Server Selector", NamedTextColor.BLUE);
    }

    @Override
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        res.add(Component.keybind("key.use").color(NamedTextColor.AQUA).appendSpace().append(Component.text("this item while holding it", NamedTextColor.GRAY)));
        res.add(Component.text("to open the server selection menu.", NamedTextColor.GRAY));
        return res;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> plugin.getQueueManager().getExecutor().openServerSelector(who);
    }
}
