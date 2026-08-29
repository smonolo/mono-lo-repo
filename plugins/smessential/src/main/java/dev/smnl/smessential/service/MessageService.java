package dev.smnl.smessential.service;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageService {

  private final MuteService muteService;
  private final Map<UUID, UUID> lastMessaged = new ConcurrentHashMap<>();

  public MessageService(@NotNull MuteService muteService) {
    this.muteService = muteService;
  }

  public void sendMessage(
      @NotNull CommandSender sender, @NotNull Player target, @NotNull String message) {
    if (sender instanceof Player player) {
      if (player.getUniqueId().equals(target.getUniqueId())) {
        sender.sendMessage(MessageFormatter.formatError("Message", "You cannot message yourself."));
        return;
      }

      if (muteService.isMuted(player.getUniqueId())) {
        sender.sendMessage(
            MessageFormatter.formatError("Moderation", "You cannot send messages while muted."));
        return;
      }

      lastMessaged.put(player.getUniqueId(), target.getUniqueId());
      lastMessaged.put(target.getUniqueId(), player.getUniqueId());
    }

    Component senderNameComp = PlayerUtils.getGeneralDisplayName(sender);
    Component targetNameComp = PlayerUtils.getGeneralDisplayName(target);

    Component toSenderMsg =
        Component.text("[HQ] ", NamedTextColor.BLUE)
            .append(Component.text("(To ", NamedTextColor.GOLD))
            .append(targetNameComp)
            .append(Component.text(")", NamedTextColor.GOLD))
            .append(Component.text(": ", NamedTextColor.WHITE))
            .append(Component.text(message, NamedTextColor.GRAY));

    Component toRecipientMsg =
        Component.text("[HQ] ", NamedTextColor.BLUE)
            .append(Component.text("(From ", NamedTextColor.GOLD))
            .append(senderNameComp)
            .append(Component.text(")", NamedTextColor.GOLD))
            .append(Component.text(": ", NamedTextColor.WHITE))
            .append(Component.text(message, NamedTextColor.GRAY));

    sender.sendMessage(toSenderMsg);
    target.sendMessage(toRecipientMsg);
  }

  public void sendReply(@NotNull Player player, @NotNull String message) {
    if (muteService.isMuted(player.getUniqueId())) {
      player.sendMessage(
          MessageFormatter.formatError("Moderation", "You cannot send messages while muted."));
      return;
    }

    UUID targetUuid = lastMessaged.get(player.getUniqueId());
    if (targetUuid == null) {
      player.sendMessage(MessageFormatter.formatError("Message", "You have nobody to reply to."));
      return;
    }

    Player target = Bukkit.getPlayer(targetUuid);
    if (target == null || !target.isOnline()) {
      player.sendMessage(
          MessageFormatter.formatError("Message", "That player is no longer online."));
      return;
    }

    sendMessage(player, target, message);
  }

  public @Nullable Player findTarget(@NotNull String name, @NotNull CommandSender sender) {
    return PlayerUtils.findOnlinePlayer(name, sender);
  }
}
