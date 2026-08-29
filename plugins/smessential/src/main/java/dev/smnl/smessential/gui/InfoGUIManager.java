package dev.smnl.smessential.gui;

import dev.smnl.smessential.model.Rank;
import dev.smnl.smessential.model.StatisticType;
import dev.smnl.smessential.service.FreezeService;
import dev.smnl.smessential.service.MuteService;
import dev.smnl.smessential.service.StatisticService;
import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.ItemBuilder;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoGUIManager {

  private final FreezeService freezeService;
  private final MuteService muteService;
  private final PunishGUIManager punishGUIManager;
  private final StatisticService statisticService;
  private final InvseeGUIManager invseeGUIManager;

  public InfoGUIManager(
      @NotNull FreezeService freezeService,
      @NotNull MuteService muteService,
      @NotNull PunishGUIManager punishGUIManager,
      @Nullable StatisticService statisticService,
      @Nullable InvseeGUIManager invseeGUIManager) {
    this.freezeService = freezeService;
    this.muteService = muteService;
    this.punishGUIManager = punishGUIManager;
    this.statisticService = statisticService;
    this.invseeGUIManager = invseeGUIManager;
  }

  public InfoGUIManager(
      @NotNull FreezeService freezeService,
      @NotNull MuteService muteService,
      @NotNull PunishGUIManager punishGUIManager,
      @Nullable StatisticService statisticService) {
    this(freezeService, muteService, punishGUIManager, statisticService, null);
  }

  public InfoGUIManager(
      @NotNull FreezeService freezeService,
      @NotNull MuteService muteService,
      @NotNull PunishGUIManager punishGUIManager) {
    this(freezeService, muteService, punishGUIManager, null, null);
  }

  public void openInfoGUI(@NotNull Player staff, @NotNull Player target) {
    GUIWindow gui = new GUIWindow("Player Info", 27);
    renderInfoGUI(gui, staff, target);
    gui.open(staff);
  }

  private void renderInfoGUI(
      @NotNull GUIWindow gui, @NotNull Player staff, @NotNull Player target) {
    boolean isFrozen = freezeService.isFrozen(target.getUniqueId());
    boolean isMuted = muteService.isMuted(target.getUniqueId());
    boolean canFly = target.getAllowFlight();
    boolean isBedrock = PlayerUtils.isBedrockPlayer(target);

    // Row 1, Center (Slot 4): Player Skull with Info Tooltip
    List<Component> skullLore = new ArrayList<>();
    if (dev.smnl.smessential.SMEssential.getInstance() != null
        && dev.smnl.smessential.SMEssential.getInstance().getRankService() != null) {
      dev.smnl.smessential.service.RankService rs =
          dev.smnl.smessential.SMEssential.getInstance().getRankService();
      Rank primary = rs.getPrimaryRank(target.getUniqueId());
      java.util.Set<Rank> secondaries = rs.getSecondaryRanks(target.getUniqueId());
      Rank display = rs.getDisplayRank(target.getUniqueId());

      skullLore.add(
          Component.text("Primary Rank: ", NamedTextColor.GRAY)
              .append(primary.getRankDisplayNameComponent()));
      if (!secondaries.isEmpty()) {
        List<Rank> sortedSec = new ArrayList<>(secondaries);
        java.util.Collections.sort(sortedSec);
        Component secBuilder = Component.text("Secondary Ranks: ", NamedTextColor.GRAY);
        for (int i = 0; i < sortedSec.size(); i++) {
          secBuilder = secBuilder.append(sortedSec.get(i).getRankDisplayNameComponent());
          if (i < sortedSec.size() - 1) {
            secBuilder = secBuilder.append(Component.text(", ", NamedTextColor.DARK_GRAY));
          }
        }
        skullLore.add(secBuilder);
      }
      if (!display.getId().equals(primary.getId())) {
        skullLore.add(
            Component.text("Active Tag: ", NamedTextColor.GRAY)
                .append(display.getRankDisplayNameComponent()));
      }
    } else {
      skullLore.add(
          Component.text("Rank: ", NamedTextColor.GRAY)
              .append(PlayerUtils.getPlayerRankComponent(target)));
    }
    skullLore.add(
        Component.text("Platform: ", NamedTextColor.GRAY)
            .append(
                Component.text(
                    isBedrock ? "Bedrock (Geyser)" : "Java Edition", NamedTextColor.WHITE)));
    skullLore.add(
        Component.text("Game Mode: ", NamedTextColor.GRAY)
            .append(
                Component.text(
                    FontUtils.formatEnumTitleCase(target.getGameMode().name()),
                    NamedTextColor.WHITE)));
    skullLore.add(
        Component.text("Freeze: ", NamedTextColor.GRAY)
            .append(Component.text(isFrozen ? "Frozen" : "Normal", NamedTextColor.WHITE)));
    skullLore.add(
        Component.text("Mute: ", NamedTextColor.GRAY)
            .append(Component.text(isMuted ? "Muted" : "Normal", NamedTextColor.WHITE)));
    skullLore.add(
        Component.text("Flight: ", NamedTextColor.GRAY)
            .append(Component.text(canFly ? "Enabled" : "Disabled", NamedTextColor.WHITE)));
    skullLore.add(
        Component.text("Ping: ", NamedTextColor.GRAY)
            .append(Component.text(target.getPing() + "ms", NamedTextColor.WHITE)));
    skullLore.add(
        Component.text("IP: ", NamedTextColor.GRAY)
            .append(Component.text(PlayerUtils.getPlayerIp(target), NamedTextColor.GOLD)));
    skullLore.add(
        Component.text("Version: ", NamedTextColor.GRAY)
            .append(
                Component.text(PlayerUtils.getPlayerVersionString(target), NamedTextColor.WHITE)));
    if (statisticService != null) {
      long deaths = statisticService.getStatistic(target, StatisticType.DEATHS);
      skullLore.add(
          Component.text("Deaths: ", NamedTextColor.GRAY)
              .append(Component.text(deaths, NamedTextColor.WHITE)));
    }

    ItemBuilder headItem =
        ItemBuilder.of(Material.PLAYER_HEAD)
            .skullOwner(target)
            .name(target.getName(), NamedTextColor.GOLD)
            .componentLore(skullLore);
    gui.setItem(4, headItem.build(), null);

    // Row 2 Moderation Actions:
    // Slot 10: View Inventory
    if (invseeGUIManager != null) {
      gui.setItem(
          10,
          ItemBuilder.of(Material.CHEST)
              .name("View Inventory", NamedTextColor.GOLD)
              .lore("Click to view inventory and equipment silently")
              .build(),
          event -> {
            invseeGUIManager.openInventoryGUI(staff, target);
          });
    }

    // Slot 11: Punish GUI
    gui.setItem(
        11,
        ItemBuilder.of(Material.ANVIL)
            .name("Punish", NamedTextColor.GOLD)
            .lore("Click to open punishment menu")
            .build(),
        event -> {
          staff.closeInventory();
          punishGUIManager.openPunishGUI(staff, target.getName(), "Other");
        });

    // Slot 12: Teleport To Player
    gui.setItem(
        12,
        ItemBuilder.of(Material.ENDER_PEARL)
            .name("Teleport To", NamedTextColor.GOLD)
            .lore("Click to teleport to " + target.getName())
            .build(),
        event -> {
          staff.closeInventory();
          staff.teleport(target.getLocation());
          Component msg =
              Component.text("Teleported to ", NamedTextColor.GRAY)
                  .append(PlayerUtils.getGeneralDisplayName(target))
                  .append(Component.text(".", NamedTextColor.GRAY));
          staff.sendMessage(MessageFormatter.formatInfo("Moderation", msg));
        });

    // Slot 13: Teleport Player Here
    gui.setItem(
        13,
        ItemBuilder.of(Material.ENDER_EYE)
            .name("Teleport Here", NamedTextColor.GOLD)
            .lore("Click to teleport " + target.getName() + " to you")
            .build(),
        event -> {
          staff.closeInventory();
          target.teleport(staff.getLocation());
          Component msgStaff =
              Component.text("Teleported ", NamedTextColor.GRAY)
                  .append(PlayerUtils.getGeneralDisplayName(target))
                  .append(Component.text(" to your location.", NamedTextColor.GRAY));
          staff.sendMessage(MessageFormatter.formatInfo("Moderation", msgStaff));
          Component msgTarget =
              Component.text("You were teleported to ", NamedTextColor.GRAY)
                  .append(PlayerUtils.getGeneralDisplayName(staff))
                  .append(Component.text(".", NamedTextColor.GRAY));
          target.sendMessage(MessageFormatter.formatInfo("Moderation", msgTarget));
        });

    // Slot 14: Freeze / Unfreeze
    gui.setItem(
        14,
        ItemBuilder.of(isFrozen ? Material.PACKED_ICE : Material.ICE)
            .name(
                isFrozen ? "Unfreeze" : "Freeze",
                isFrozen ? NamedTextColor.GREEN : NamedTextColor.GOLD)
            .lore(
                isFrozen ? "Status: FROZEN" : "Status: NORMAL",
                "Click to " + (isFrozen ? "unfreeze" : "freeze") + " player")
            .build(),
        event -> {
          freezeService.toggleFreeze(target);
          renderInfoGUI(gui, staff, target);
        });

    // Slot 15: Toggle Flight
    gui.setItem(
        15,
        ItemBuilder.of(Material.FEATHER)
            .name("Toggle Flight", canFly ? NamedTextColor.GREEN : NamedTextColor.GOLD)
            .lore(canFly ? "Flight: ENABLED" : "Flight: DISABLED", "Click to toggle flight mode")
            .build(),
        event -> {
          boolean newFly = !target.getAllowFlight();
          target.setAllowFlight(newFly);
          if (!newFly) {
            target.setFlying(false);
          }
          String state = newFly ? "enabled" : "disabled";
          Component msgFlight =
              Component.text("Flight " + state + " for ", NamedTextColor.GRAY)
                  .append(PlayerUtils.getStaffVisibleDisplayName(target))
                  .append(Component.text(".", NamedTextColor.GRAY));
          staff.sendMessage(MessageFormatter.formatInfo("Moderation", msgFlight));
          target.sendMessage(MessageFormatter.formatInfo("Moderation", "Flight " + state + "."));

          Component broadcastMsg =
              MessageFormatter.formatInfo(
                  "Moderation",
                  Component.empty()
                      .append(Component.text("Flight was " + state + " for ", NamedTextColor.GRAY))
                      .append(PlayerUtils.getGeneralDisplayName(target))
                      .append(Component.text(" by a staff member.", NamedTextColor.GRAY)));
          PlayerUtils.broadcastMessage(broadcastMsg);

          renderInfoGUI(gui, staff, target);
        });

    // Slot 16: Copy UUID
    gui.setItem(
        16,
        ItemBuilder.of(Material.NAME_TAG)
            .name("Copy UUID", NamedTextColor.GOLD)
            .lore("UUID: " + target.getUniqueId(), "Click to copy UUID to clipboard")
            .build(),
        event -> {
          staff.closeInventory();
          Component prefixMsg =
              Component.text("UUID of ", NamedTextColor.GRAY)
                  .append(PlayerUtils.getGeneralDisplayName(target))
                  .append(Component.text(": ", NamedTextColor.GRAY));
          staff.sendMessage(
              MessageFormatter.formatInfo("Moderation", prefixMsg)
                  .append(
                      Component.text(target.getUniqueId().toString(), NamedTextColor.WHITE)
                          .clickEvent(ClickEvent.copyToClipboard(target.getUniqueId().toString()))
                          .hoverEvent(
                              HoverEvent.showText(
                                  Component.text(
                                      "Click to copy UUID to clipboard", NamedTextColor.GRAY)))));
        });

    // Row 3: Close Button
    gui.setCloseButton();
  }
}
