package dev.smnl.smessential.manager;

import dev.smnl.smessential.service.RankService;
import dev.smnl.smessential.util.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabListManager implements Listener {

  private static final Component FOOTER =
      Component.empty()
          .append(Component.newline())
          .append(
              Component.text("mc.smnl.dev", NamedTextColor.GOLD)
                  .decoration(TextDecoration.BOLD, false));

  private final JavaPlugin plugin;
  private final RankService rankService;
  private final java.util.Map<java.util.UUID, Component> lastHeaders =
      new java.util.concurrent.ConcurrentHashMap<>();

  public TabListManager(@NotNull JavaPlugin plugin, @Nullable RankService rankService) {
    this.plugin = plugin;
    this.rankService = rankService;
  }

  public TabListManager(@NotNull JavaPlugin plugin) {
    this(plugin, null);
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);

    if (rankService != null) {
      rankService.addPlayerUpdateListener(this::applyTabList);
      rankService.addGlobalUpdateListener(this::updateAll);
    }

    Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L, 20L);

    updateAll();
  }

  public void updateAll() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      applyTabList(player);
    }
  }

  public void applyTabList(@NotNull Player player) {
    if (!player.isOnline()) {
      return;
    }

    int onlineCount = Bukkit.getOnlinePlayers().size();
    int ping = player.getPing();
    String pingStr = ping + "ms";

    Component header =
        Component.empty()
            .append(Component.text("Headquarters", NamedTextColor.BLUE, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.text("Players: ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(onlineCount), NamedTextColor.WHITE))
            .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Rank: ", NamedTextColor.GRAY))
            .append(PlayerUtils.getPlayerRankComponent(player))
            .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Ping: ", NamedTextColor.GRAY))
            .append(Component.text(pingStr, NamedTextColor.WHITE))
            .append(Component.newline());

    Component lastHeader = lastHeaders.get(player.getUniqueId());
    if (header.equals(lastHeader)) {
      return;
    }

    lastHeaders.put(player.getUniqueId(), header);
    player.sendPlayerListHeaderAndFooter(header, FOOTER);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Bukkit.getScheduler().runTask(plugin, this::updateAll);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    java.util.UUID uuid = event.getPlayer().getUniqueId();
    lastHeaders.remove(uuid);
    Bukkit.getScheduler().runTask(plugin, this::updateAll);
  }
}
