package net.hcfrevival.lobby;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.utils.Configs;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public final class LobbyConfig {
    @Getter public final LobbyPlugin plugin;

    @Getter @Setter public String mongoUri;
    @Getter @Setter public String mongoDatabaseName;
    @Getter @Setter public String redisUri;
    @Getter @Setter public List<String> motd;
    @Getter @Setter public Location spawnLocation;

    public LobbyConfig(LobbyPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveSpawnLocation(Player player) {
        final YamlConfiguration conf = plugin.loadConfiguration("config");
        Configs.writePlayerLocation(conf, "spawn", new PLocatable(player));
        plugin.saveConfiguration("config", conf);
    }

    public void load() {
        final YamlConfiguration conf = plugin.loadConfiguration("config");
        final List<String> motdEntries = conf.getStringList("motd");

        mongoUri = conf.getString("database.mongodb.uri");
        mongoDatabaseName = conf.getString("database.mongodb.database");

        redisUri = conf.getString("database.redis.uri");

        motd = Lists.newArrayList();
        motdEntries.forEach(entry -> motd.add(ChatColor.translateAlternateColorCodes('&', entry)));

        spawnLocation = Configs.parsePlayerLocation(conf, "spawn").getBukkitLocation();
    }
}
