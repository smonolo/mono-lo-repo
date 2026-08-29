package dev.smnl.smessential.gui;

import dev.smnl.smessential.database.DatabaseManager.BanData;
import dev.smnl.smessential.database.DatabaseManager.MuteData;
import dev.smnl.smessential.service.BanService;
import dev.smnl.smessential.service.MuteService;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.MessageFormatter.MessageType;
import dev.smnl.smessential.util.PlayerUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PunishGUIManager {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault());

  private final MuteService muteService;
  private final BanService banService;

  public PunishGUIManager(@NotNull MuteService muteService, @NotNull BanService banService) {
    this.muteService = muteService;
    this.banService = banService;
  }

  public void openPunishGUI(
      @NotNull Player staff, @NotNull String targetName, @NotNull String reason) {
    Player targetOnline = PlayerUtils.findOnlinePlayer(targetName);
    UUID targetUuid = resolveTargetUuid(targetName, targetOnline);

    boolean isMuted = targetUuid != null && muteService.isMuted(targetUuid);
    boolean isBanned = targetUuid != null && banService.isBanned(targetUuid);

    GUIWindow gui = new GUIWindow("Moderation", 36);

    if (isMuted) {
      gui.setItem(
          10,
          Material.LIME_DYE,
          "Unmute",
          NamedTextColor.GOLD,
          null,
          event -> {
            staff.closeInventory();
            executeUnmute(staff, targetName);
          });
    } else {
      gui.setItem(
          10,
          Material.FEATHER,
          "Mute",
          NamedTextColor.GOLD,
          null,
          event -> {
            staff.closeInventory();
            executeMute(staff, targetName, reason);
          });
    }

    gui.setItem(
        12,
        Material.LEATHER_BOOTS,
        "Kick",
        NamedTextColor.GOLD,
        null,
        event -> {
          staff.closeInventory();
          executeKick(staff, targetName, reason);
        });

    if (isBanned) {
      gui.setItem(
          14,
          Material.LIME_DYE,
          "Unban",
          NamedTextColor.GOLD,
          null,
          event -> {
            staff.closeInventory();
            executeUnban(staff, targetName);
          });
    } else {
      gui.setItem(
          14,
          Material.IRON_DOOR,
          "Ban",
          NamedTextColor.GOLD,
          null,
          event -> {
            staff.closeInventory();
            executeBan(staff, targetName, reason);
          });
    }

    gui.setItem(
        16,
        Material.BOOK,
        "Active Punishments",
        NamedTextColor.GOLD,
        null,
        event -> {
          staff.closeInventory();
          executeCheckPunishments(staff, targetName);
        });

    gui.setCloseButton();

    gui.open(staff);
  }

  private @Nullable UUID resolveTargetUuid(
      @NotNull String targetName, @Nullable Player targetOnline) {
    if (targetOnline != null) {
      return targetOnline.getUniqueId();
    }
    UUID mutedUuid = muteService.findMutedUuid(targetName);
    if (mutedUuid != null) {
      return mutedUuid;
    }
    return banService.findBannedUuid(targetName);
  }

  private void executeMute(Player staff, String targetName, String reason) {
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    if (target == null) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", "'" + targetName + "' is not online."));
      return;
    }
    if (muteService.isMuted(target.getUniqueId())) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", target.getName() + " is already muted."));
      return;
    }
    boolean muted =
        muteService.mutePlayer(
            target.getUniqueId(),
            target.getName(),
            reason,
            staff.getUniqueId().toString(),
            System.currentTimeMillis());
    if (!muted) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", target.getName() + " is already muted."));
      return;
    }
    Component muteMsg =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" has been muted.", NamedTextColor.GRAY));
    Bukkit.getServer().sendMessage(MessageFormatter.formatInfo("Moderation", muteMsg));
    target.sendMessage(
        MessageFormatter.formatError("Moderation", "You have been muted for " + reason + "."));
  }

  private void executeUnmute(Player staff, String targetName) {
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    UUID mutedUuid = target != null ? target.getUniqueId() : muteService.findMutedUuid(targetName);
    if (mutedUuid == null || !muteService.isMuted(mutedUuid)) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", targetName + " is not currently muted."));
      return;
    }
    muteService.unmutePlayer(mutedUuid, staff.getUniqueId().toString());
    if (target != null) {
      target.sendMessage(MessageFormatter.formatInfo("Moderation", "You have been unmuted."));
    }
    if (staff != target) {
      Component targetComp =
          target != null
              ? PlayerUtils.getGeneralDisplayName(target)
              : Component.text(targetName, NamedTextColor.GRAY);
      Component unmuteMsg =
          Component.empty()
              .append(targetComp)
              .append(Component.text(" has been unmuted.", NamedTextColor.GRAY));
      staff.sendMessage(MessageFormatter.formatInfo("Moderation", unmuteMsg));
    }
  }

  private void executeKick(Player staff, String targetName, String reason) {
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    if (target == null) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", "'" + targetName + "' is not online."));
      return;
    }
    target.kick(BanService.createKickScreen(reason));
    Component kickMsg =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" has been kicked.", NamedTextColor.GRAY));
    Bukkit.getServer().sendMessage(MessageFormatter.formatInfo("Moderation", kickMsg));
  }

  private void executeBan(Player staff, String targetName, String reason) {
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    if (target == null) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", "'" + targetName + "' is not online."));
      return;
    }
    if (banService.isBanned(target.getUniqueId())) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", target.getName() + " is already banned."));
      return;
    }
    boolean banned =
        banService.banPlayer(
            target.getUniqueId(),
            target.getName(),
            reason,
            staff.getUniqueId().toString(),
            System.currentTimeMillis());
    if (!banned) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", target.getName() + " is already banned."));
      return;
    }
    target.kick(BanService.createBanScreen(reason));
    Component banMsg =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" has been banned.", NamedTextColor.GRAY));
    Bukkit.getServer().sendMessage(MessageFormatter.formatInfo("Moderation", banMsg));
  }

  private void executeUnban(Player staff, String targetName) {
    boolean unbanned = banService.unbanPlayer(targetName, staff.getUniqueId().toString());
    if (!unbanned) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", targetName + " is not currently banned."));
      return;
    }
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    Component targetComp =
        target != null
            ? PlayerUtils.getGeneralDisplayName(target)
            : Component.text(targetName, NamedTextColor.GRAY);
    Component unbanMsg =
        Component.empty()
            .append(targetComp)
            .append(Component.text(" has been unbanned.", NamedTextColor.GRAY));
    staff.sendMessage(MessageFormatter.formatInfo("Moderation", unbanMsg));
  }

  private @NotNull String resolveIssuerName(@Nullable String issuer) {
    if (issuer == null || issuer.isBlank() || issuer.equalsIgnoreCase("CONSOLE")) {
      return "Console";
    }
    try {
      UUID issuerUuid = UUID.fromString(issuer);
      if (dev.smnl.smessential.SMEssential.getInstance() != null
          && dev.smnl.smessential.SMEssential.getInstance().getUserService() != null) {
        var ud =
            dev.smnl.smessential.SMEssential.getInstance().getUserService().getUser(issuerUuid);
        if (ud != null && ud.username() != null && !ud.username().isBlank()) {
          return ud.username();
        }
      }
      Player online = Bukkit.getPlayer(issuerUuid);
      if (online != null) {
        return online.getName();
      }
    } catch (IllegalArgumentException ignored) {
    }
    return issuer;
  }

  private void executeCheckPunishments(Player staff, String targetName) {
    Player targetOnline = PlayerUtils.findOnlinePlayer(targetName);
    Component targetComp =
        targetOnline != null
            ? PlayerUtils.getStaffVisibleDisplayName(targetOnline)
            : Component.text(targetName, NamedTextColor.GRAY);

    UUID uuid = resolveTargetUuid(targetName, targetOnline);
    MuteData muteData = uuid != null ? muteService.getMuteData(uuid) : null;
    BanData banData = uuid != null ? banService.getBanData(uuid) : null;

    if (muteData == null && banData == null) {
      Component noPunish =
          Component.empty()
              .append(targetComp)
              .append(Component.text(" has no active punishments.", NamedTextColor.GRAY));
      staff.sendMessage(MessageFormatter.formatInfo("Moderation", noPunish));
      return;
    }

    net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(
        MessageFormatter.format(
            "Moderation",
            Component.text("Active punishments for ", NamedTextColor.GRAY)
                .append(targetComp)
                .append(Component.text(":", NamedTextColor.GRAY)),
            MessageType.INFO));

    if (muteData != null) {
      String dateStr = DATE_FORMATTER.format(Instant.ofEpochMilli(muteData.createdAt()));
      String issuerDisplay = resolveIssuerName(muteData.issuer());
      Component muteLine =
          Component.text("- ", NamedTextColor.DARK_GRAY)
              .append(Component.text("Mute: ", NamedTextColor.WHITE))
              .append(
                  Component.text(
                      muteData.reason() + " on " + dateStr + " by " + issuerDisplay,
                      NamedTextColor.GRAY));
      builder.append(Component.newline()).append(muteLine);
    }

    if (banData != null) {
      String dateStr = DATE_FORMATTER.format(Instant.ofEpochMilli(banData.createdAt()));
      String issuerDisplay = resolveIssuerName(banData.issuer());
      Component banLine =
          Component.text("- ", NamedTextColor.DARK_GRAY)
              .append(Component.text("Ban: ", NamedTextColor.WHITE))
              .append(
                  Component.text(
                      banData.reason() + " on " + dateStr + " by " + issuerDisplay,
                      NamedTextColor.GRAY));
      builder.append(Component.newline()).append(banLine);
    }

    builder.append(Component.newline());
    staff.sendMessage(builder.build());
  }
}
