package dev.smnl.smessential.gui;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.service.BanService;
import dev.smnl.smessential.service.FreezeService;
import dev.smnl.smessential.service.MuteService;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PunishGUIManager {

  private final MuteService muteService;
  private final BanService banService;
  private final FreezeService freezeService;
  private final DatabaseManager databaseManager;

  public PunishGUIManager(
      @NotNull MuteService muteService,
      @NotNull BanService banService,
      @Nullable FreezeService freezeService,
      @Nullable DatabaseManager databaseManager) {
    this.muteService = muteService;
    this.banService = banService;
    this.freezeService = freezeService;
    this.databaseManager = databaseManager;
  }

  public PunishGUIManager(@NotNull MuteService muteService, @NotNull BanService banService) {
    this(muteService, banService, null, null);
  }

  public void openPunishGUI(
      @NotNull Player staff, @NotNull String targetName, @NotNull String reason) {
    Player targetOnline = PlayerUtils.findOnlinePlayer(targetName);
    UUID targetUuid = resolveTargetUuid(targetName, targetOnline);

    boolean isMuted = targetUuid != null && muteService.isMuted(targetUuid);
    boolean isBanned = targetUuid != null && banService.isBanned(targetUuid);
    boolean isFrozen =
        targetOnline != null
            && freezeService != null
            && freezeService.isFrozen(targetOnline.getUniqueId());

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

    if (isFrozen) {
      gui.setItem(
          16,
          Material.PACKED_ICE,
          "Unfreeze",
          NamedTextColor.GREEN,
          null,
          event -> {
            staff.closeInventory();
            executeUnfreeze(staff, targetName);
          });
    } else {
      gui.setItem(
          16,
          Material.ICE,
          "Freeze",
          NamedTextColor.GOLD,
          null,
          event -> {
            staff.closeInventory();
            executeFreeze(staff, targetName, reason);
          });
    }

    gui.setItem(
        22,
        Material.PAPER,
        "Warn",
        NamedTextColor.GOLD,
        null,
        event -> {
          staff.closeInventory();
          executeWarn(staff, targetName, reason);
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
    if (databaseManager != null) {
      databaseManager.savePunishment(
          target.getUniqueId().toString(),
          "KICK",
          target.getName(),
          reason,
          staff.getUniqueId().toString(),
          System.currentTimeMillis(),
          0L);
    }
    target.kick(BanService.createKickScreen(reason));
    Component kickMsg =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" has been kicked.", NamedTextColor.GRAY));
    Bukkit.getServer().sendMessage(MessageFormatter.formatInfo("Moderation", kickMsg));
  }

  private void executeWarn(Player staff, String targetName, String reason) {
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    if (target == null) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", "'" + targetName + "' is not online."));
      return;
    }
    if (databaseManager != null) {
      databaseManager.savePunishment(
          target.getUniqueId().toString(),
          "WARN",
          target.getName(),
          reason,
          staff.getUniqueId().toString(),
          System.currentTimeMillis(),
          0L);
    }
    target.sendMessage(
        MessageFormatter.formatError("Moderation", "You have been warned for " + reason + "."));
    Component warnMsg =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" has been warned.", NamedTextColor.GRAY));
    Bukkit.getServer().sendMessage(MessageFormatter.formatInfo("Moderation", warnMsg));
  }

  private void executeFreeze(Player staff, String targetName, String reason) {
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    if (target == null) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", "'" + targetName + "' is not online."));
      return;
    }
    if (freezeService != null) {
      freezeService.freezePlayer(target, staff.getUniqueId().toString(), reason);
    }
    Component freezeMsg =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" has been frozen.", NamedTextColor.GRAY));
    Bukkit.getServer().sendMessage(MessageFormatter.formatInfo("Moderation", freezeMsg));
  }

  private void executeUnfreeze(Player staff, String targetName) {
    Player target = PlayerUtils.findOnlinePlayer(targetName);
    if (target == null) {
      staff.sendMessage(
          MessageFormatter.formatError("Moderation", "'" + targetName + "' is not online."));
      return;
    }
    if (freezeService != null) {
      freezeService.unfreezePlayer(target, staff.getUniqueId().toString());
    }
    Component unfreezeMsg =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" has been unfrozen.", NamedTextColor.GRAY));
    Bukkit.getServer().sendMessage(MessageFormatter.formatInfo("Moderation", unfreezeMsg));
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
}
