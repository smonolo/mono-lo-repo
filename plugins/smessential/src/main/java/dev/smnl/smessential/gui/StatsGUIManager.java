package dev.smnl.smessential.gui;

import dev.smnl.smessential.database.DatabaseManager.UserData;
import dev.smnl.smessential.model.Rank;
import dev.smnl.smessential.model.StatisticType;
import dev.smnl.smessential.service.RankService;
import dev.smnl.smessential.service.StatisticService;
import dev.smnl.smessential.service.UserService;
import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.ItemBuilder;
import dev.smnl.smessential.util.PlayerUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatsGUIManager {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault());

  private final StatisticService statisticService;
  private final UserService userService;
  private final RankService rankService;

  public StatsGUIManager(
      @NotNull StatisticService statisticService,
      @Nullable UserService userService,
      @Nullable RankService rankService) {
    this.statisticService = statisticService;
    this.userService = userService;
    this.rankService = rankService;
  }

  public void openStatsGUI(@NotNull Player viewer, @NotNull OfflinePlayer target) {
    GUIWindow gui = new GUIWindow("Player Statistics", 54);
    renderStatsGUI(gui, viewer, target);
    gui.open(viewer);
  }

  private void renderStatsGUI(
      @NotNull GUIWindow gui, @NotNull Player viewer, @NotNull OfflinePlayer target) {
    // Row 1 Center (Slot 4): Target Player Skull
    List<Component> skullLore = new ArrayList<>();

    String targetName =
        target.getName() != null ? target.getName() : target.getUniqueId().toString();
    if (rankService != null) {
      Rank primary = rankService.getPrimaryRank(target.getUniqueId());
      skullLore.add(
          Component.text("Rank: ", NamedTextColor.GRAY)
              .append(primary.getRankDisplayNameComponent()));
    }

    if (target.isOnline() && target.getPlayer() != null) {
      Player onlinePlayer = target.getPlayer();
      boolean isBedrock = PlayerUtils.isBedrockPlayer(onlinePlayer);
      skullLore.add(
          Component.text("Platform: ", NamedTextColor.GRAY)
              .append(
                  Component.text(
                      isBedrock ? "Bedrock (Geyser)" : "Java Edition", NamedTextColor.WHITE)));
      skullLore.add(
          Component.text("Game Mode: ", NamedTextColor.GRAY)
              .append(
                  Component.text(
                      FontUtils.formatEnumTitleCase(onlinePlayer.getGameMode().name()),
                      NamedTextColor.WHITE)));
      skullLore.add(
          Component.text("Ping: ", NamedTextColor.GRAY)
              .append(Component.text(onlinePlayer.getPing() + "ms", NamedTextColor.WHITE)));
    } else {
      skullLore.add(
          Component.text("Status: ", NamedTextColor.GRAY)
              .append(Component.text("Offline", NamedTextColor.RED)));
    }

    if (userService != null) {
      UserData userData = userService.getUser(target.getUniqueId());
      if (userData != null) {
        String firstJoinStr = DATE_FORMATTER.format(Instant.ofEpochMilli(userData.firstJoin()));
        skullLore.add(
            Component.text("First Joined: ", NamedTextColor.GRAY)
                .append(Component.text(firstJoinStr, NamedTextColor.WHITE)));
      }
    }

    long playTimeTicks = statisticService.getStatistic(target, StatisticType.PLAY_TIME);
    skullLore.add(
        Component.text("Total Play Time: ", NamedTextColor.GRAY)
            .append(
                Component.text(
                    StatisticType.PLAY_TIME.formatValue(playTimeTicks), NamedTextColor.GOLD)));

    ItemBuilder headItem =
        ItemBuilder.of(Material.PLAYER_HEAD)
            .skullOwner(target)
            .name(targetName, NamedTextColor.GOLD)
            .componentLore(skullLore);
    gui.setItem(4, headItem.build(), null);

    // Slot layout for statistics
    int[] slots = {
      10, 11, 12, 13, 14, 15, 16, // Row 2
      19, 20, 21, 22, 23, 24, 25, // Row 3
      28, 29, 30, 31 // Row 4
    };

    StatisticType[] types = StatisticType.values();
    for (int i = 0; i < types.length && i < slots.length; i++) {
      StatisticType type = types[i];
      int slot = slots[i];

      long value = statisticService.getStatistic(target, type);
      String formattedValue = type.formatValue(value);

      List<Component> lore = new ArrayList<>();
      lore.add(Component.text(type.getDescription(), NamedTextColor.GRAY));
      lore.add(Component.empty());
      lore.add(
          Component.text("Stat: ", NamedTextColor.GRAY)
              .append(Component.text(formattedValue, NamedTextColor.WHITE)));

      gui.setItem(
          slot,
          ItemBuilder.of(type.getIcon())
              .name(type.getDisplayName(), NamedTextColor.GOLD)
              .componentLore(lore)
              .build(),
          null);
    }

    // Row 6: Close Button
    gui.setCloseButton();
  }
}
