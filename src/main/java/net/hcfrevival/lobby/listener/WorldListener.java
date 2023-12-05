package net.hcfrevival.lobby.listener;

import lombok.Getter;
import net.hcfrevival.lobby.LobbyPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public record WorldListener(@Getter LobbyPlugin plugin) implements Listener {
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!event.toWeatherState() && plugin.getConfiguration().isAlwaysStorming()) {
            event.getWorld().setStorm(true);
            event.getWorld().setWeatherDuration(3600 * 1000);
        }
    }
}
