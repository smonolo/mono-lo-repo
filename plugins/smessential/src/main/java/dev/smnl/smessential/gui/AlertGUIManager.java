package dev.smnl.smessential.gui;

import dev.smnl.smessential.model.AlertTarget;
import dev.smnl.smessential.service.AlertService;
import dev.smnl.smessential.util.ItemBuilder;
import dev.smnl.smessential.util.MessageFormatter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AlertGUIManager {

  private final AlertService alertService;

  public AlertGUIManager(@NotNull AlertService alertService) {
    this.alertService = alertService;
  }

  public void openAlertGUI(@NotNull Player player, @NotNull String message) {
    boolean hasActive = alertService.hasActiveAlert();

    if (message.isBlank() && !hasActive) {
      player.sendMessage(MessageFormatter.formatError("Administration", "Usage: /alert <message>"));
      return;
    }

    GUIWindow gui = new GUIWindow("Alert", 27);

    // Slot 11: Top Banner (Toggle active/inactive like mute/unmute)
    if (hasActive) {
      String current = alertService.getBannerAlert();
      ItemBuilder clearItem =
          ItemBuilder.of(Material.REDSTONE_BLOCK)
              .name("Clear Top Banner", NamedTextColor.RED)
              .lore(
                  current != null ? "Current: " + current : "Active banner",
                  "Click to clear active banner alert");
      gui.setItem(
          11,
          clearItem.build(),
          event -> {
            player.closeInventory();
            alertService.clearAlert();
            player.sendMessage(
                MessageFormatter.formatInfo("Administration", "Active alert has been cleared."));
          });
    } else {
      gui.setItem(
          11,
          ItemBuilder.of(AlertTarget.BANNER.getMaterial())
              .name(AlertTarget.BANNER.getDisplayName(), NamedTextColor.GOLD)
              .lore(AlertTarget.BANNER.getDescription(), "Click to send alert")
              .build(),
          event -> {
            player.closeInventory();
            alertService.sendAlert(AlertTarget.BANNER, message);
            player.sendMessage(MessageFormatter.formatInfo("Administration", "Alert sent."));
          });
    }

    // Only render Chat and All if a message is provided
    if (!message.isBlank()) {
      // Slot 13: Chat (always sends to chat)
      gui.setItem(
          13,
          ItemBuilder.of(AlertTarget.CHAT.getMaterial())
              .name(AlertTarget.CHAT.getDisplayName(), NamedTextColor.GOLD)
              .lore(AlertTarget.CHAT.getDescription(), "Click to send alert")
              .build(),
          event -> {
            player.closeInventory();
            alertService.sendAlert(AlertTarget.CHAT, message);
            player.sendMessage(MessageFormatter.formatInfo("Administration", "Alert sent."));
          });

      // Slot 15: All (sends to chat and sets/overwrites top banner)
      String allLore =
          hasActive
              ? "Sends alert to chat and overwrites active top banner"
              : AlertTarget.ALL.getDescription();
      gui.setItem(
          15,
          ItemBuilder.of(AlertTarget.ALL.getMaterial())
              .name(AlertTarget.ALL.getDisplayName(), NamedTextColor.GOLD)
              .lore(allLore, "Click to send alert")
              .build(),
          event -> {
            player.closeInventory();
            alertService.sendAlert(AlertTarget.ALL, message);
            player.sendMessage(MessageFormatter.formatInfo("Administration", "Alert sent."));
          });
    }

    // Close button
    gui.setCloseButton();
    gui.open(player);
  }
}
