package net.hcfrevival.lobby.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.hcfrevival.lobby.LobbyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public record ServerSelectorItem(@Getter LobbyPlugin plugin) implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.COMPASS;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_AQUA + "Server Selector";
    }

    @Override
    public List<String> getLore() {
        final List<String> res = Lists.newArrayList();
        res.add(ChatColor.GRAY + "Right-click this item while holding it");
        res.add(ChatColor.GRAY + "to open the server selector menu.");
        return res;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
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
