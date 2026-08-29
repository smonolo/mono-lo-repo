package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.model.AlertTarget;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.MessageFormatter.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlertService implements Listener {

  private final DatabaseManager databaseManager;
  private String bannerAlert = null;
  private BossBar activeBossBar = null;

  public AlertService(@NotNull DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public void setup(@NotNull JavaPlugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
    loadPersistedAlerts();
  }

  public void loadPersistedAlerts() {
    bannerAlert = databaseManager.getAlert("BANNER");
    if (bannerAlert == null || bannerAlert.isBlank()) {
      bannerAlert = databaseManager.getAlert("GLOBAL");
    }

    if (bannerAlert != null && !bannerAlert.isBlank()) {
      updateBossBar(bannerAlert);
    }
  }

  public boolean hasActiveAlert() {
    return bannerAlert != null && !bannerAlert.isBlank();
  }

  public @Nullable String getBannerAlert() {
    return bannerAlert;
  }

  public void sendAlert(@NotNull AlertTarget target, @NotNull String message) {
    if (target == AlertTarget.CHAT || target == AlertTarget.ALL) {
      Bukkit.getServer()
          .sendMessage(MessageFormatter.format("Alert", message, MessageType.INFO, true));
    }

    if (target == AlertTarget.BANNER || target == AlertTarget.ALL) {
      this.bannerAlert = message;
      databaseManager.saveAlert("BANNER", message);
      updateBossBar(message);
    }
  }

  private void updateBossBar(@NotNull String message) {
    Component barTitle =
        Component.text("(Alert) ", NamedTextColor.GOLD)
            .append(Component.text(message, NamedTextColor.WHITE));

    if (activeBossBar == null) {
      activeBossBar =
          BossBar.bossBar(barTitle, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
    } else {
      activeBossBar.name(barTitle);
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      player.showBossBar(activeBossBar);
    }
  }

  public void clearAlert() {
    this.bannerAlert = null;
    databaseManager.deleteAlert("BANNER");
    databaseManager.deleteAlert("GLOBAL");

    if (activeBossBar != null) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        player.hideBossBar(activeBossBar);
      }
      activeBossBar = null;
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (activeBossBar != null) {
      event.getPlayer().showBossBar(activeBossBar);
    }
  }
}
