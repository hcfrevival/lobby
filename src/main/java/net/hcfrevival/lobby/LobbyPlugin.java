package net.hcfrevival.lobby;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.libs.acf.PaperCommandManager;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.connect.impl.redis.Redis;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.alts.AltService;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanConfig;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.punishments.PunishmentService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.reports.ReportService;
import gg.hcfactions.libs.bukkit.services.impl.reports.channel.payload.ReportPayload;
import gg.hcfactions.libs.bukkit.services.impl.reports.channel.payload.ReportPayloadTypeAdapter;
import gg.hcfactions.libs.bukkit.services.impl.sync.EServerType;
import gg.hcfactions.libs.bukkit.services.impl.sync.SyncService;
import lombok.Getter;
import net.hcfrevival.lobby.command.DebugCommand;
import net.hcfrevival.lobby.command.SpawnCommand;
import net.hcfrevival.lobby.item.LeaveQueueItem;
import net.hcfrevival.lobby.item.ServerSelectorItem;
import net.hcfrevival.lobby.listener.BlockListener;
import net.hcfrevival.lobby.listener.PlayerListener;
import net.hcfrevival.lobby.listener.PremiumListener;
import net.hcfrevival.lobby.listener.WorldListener;
import net.hcfrevival.lobby.player.PlayerManager;
import net.hcfrevival.lobby.queue.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.List;

@Getter
public final class LobbyPlugin extends AresPlugin {
    public final NamespacedKey namespacedKey = new NamespacedKey(this, "lobby");

    public QueueManager queueManager;
    public PlayerManager playerManager;
    public LobbyConfig configuration;

    @Override
    public void onLoad() {
        super.onLoad();
        registerPacketEvents();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        configuration = new LobbyConfig(this);
        configuration.load();

        if (configuration.isAlwaysStorming()) {
            Bukkit.getWorlds().forEach(world -> {
                world.setStorm(true);
                world.setWeatherDuration(1000*3600);
            });
        }

        // db init
        final Mongo mdb = new Mongo(configuration.getMongoUri(), getAresLogger());
        final Redis redis = new Redis(configuration.getRedisUri(), getAresLogger());

        mdb.openConnection();
        redis.openConnection();

        registerConnectable(mdb);
        registerConnectable(redis);

        // commands
        final PaperCommandManager cmdMng = new PaperCommandManager(this);
        cmdMng.enableUnstableAPI("help");
        registerCommandManager(cmdMng);
        registerCommand(new SpawnCommand(this));
        registerCommand(new DebugCommand(this));

        // gson
        registerGsonTypeAdapter(ReportPayload.class, new ReportPayloadTypeAdapter());

        // auto completions
        cmdMng.getCommandCompletions().registerAsyncCompletion("servers", ctx -> {
            final List<String> res = Lists.newArrayList();
            final SyncService syncService = (SyncService) getService(SyncService.class);

            if (syncService == null) {
                return res;
            }

            syncService.getServerRepository().stream().filter(s -> !s.getType().equals(EServerType.LOBBY)).forEach(nonLobbyServer -> res.add(nonLobbyServer.getProxyName()));
            return res;
        });

        final CustomItemService cis = new CustomItemService(this, namespacedKey);

        // services
        registerService(new CXService(this));
        registerService(new RankService(this));
        registerService(new AccountService(this, configuration.getMongoDatabaseName()));
        registerService(new SyncService(this, configuration.getMongoDatabaseName()));
        registerService(new PunishmentService(this, configuration.getMongoDatabaseName()));
        registerService(new ReportService(this));
        registerService(new AltService(this));
        registerService(cis);

        // TODO: Make configurable
        registerService(new DeathbanService(this, new DeathbanConfig(
                configuration.getMongoDatabaseName(),
                true,
                false,
                0,
                0,
                0,
                0,
                30,
                "https://shop.hcfrevival.net",
                Maps.newHashMap()
        )));

        startServices();

        // init gson
        registerGson();

        // custom items
        cis.registerNewItem(new ServerSelectorItem(this));
        cis.registerNewItem(new LeaveQueueItem(this));

        // listeners
        registerListener(new PlayerListener(this));
        registerListener(new BlockListener());
        registerListener(new PremiumListener(this));
        registerListener(new WorldListener(this));

        // lobby internals
        queueManager = new QueueManager(this);
        playerManager = new PlayerManager(this);

        playerManager.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // services
        stopServices();

        // lobby internals
        playerManager.onDisable();

        queueManager = null;
        playerManager = null;
    }
}
