package net.hcfrevival.lobby;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.utils.Configs;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public final class LobbyConfig {
    @Getter public final LobbyPlugin plugin;

    @Getter @Setter public String mongoUri;
    @Getter @Setter public String mongoDatabaseName;
    @Getter @Setter public String redisUri;
    @Getter @Setter public Component scoreboardTitle;
    @Getter @Setter public String scoreboardFooter;
    @Getter @Setter public Component motd;
    @Getter @Setter public Location spawnLocation;
    @Getter @Setter public boolean alwaysStorming;

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

        motd = Component.empty();
        for (String entry : motdEntries) {
            final Component formatted = plugin.getMiniMessage().deserialize(entry);
            motd = motd.appendNewline().append(formatted);
        }

        scoreboardTitle = plugin.getMiniMessage().deserialize(Objects.requireNonNull(conf.getString("scoreboard.title")));
        scoreboardFooter = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(conf.getString("scoreboard.footer")));

        spawnLocation = Configs.parsePlayerLocation(conf, "spawn").getBukkitLocation();
        alwaysStorming = conf.getBoolean("weather.always-storming");
    }
}
