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

public record LeaveQueueItem(@Getter LobbyPlugin plugin) implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.BARRIER;
    }

    @Override
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "LeaveQueue");
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Leave Queue", NamedTextColor.RED);
    }

    @Override
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        res.add(Component.keybind("key.use").color(NamedTextColor.AQUA).appendSpace().append(Component.text("while holding this item to", NamedTextColor.GRAY)));
        res.add(Component.text("leave your current queue.", NamedTextColor.GRAY));
        return res;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> plugin.getQueueManager().getExecutor().exitQueue(who);
    }
}
