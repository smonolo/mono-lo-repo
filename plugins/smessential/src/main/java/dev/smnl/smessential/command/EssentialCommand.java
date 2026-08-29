package dev.smnl.smessential.command;

import dev.smnl.smessential.util.MessageFormatter;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EssentialCommand implements BasicCommand {

  private final String name;
  private final String toolName;
  private final String description;
  private final String permission;
  private final boolean playerOnly;
  private final boolean hiddenFromHelp;
  private final String[] aliases;

  public EssentialCommand(
      @NotNull String name,
      @NotNull String toolName,
      @NotNull String description,
      String[] aliases) {
    this(
        name,
        toolName,
        description,
        "smessential.command." + name.toLowerCase(),
        false,
        false,
        aliases);
  }

  public EssentialCommand(
      @NotNull String name,
      @NotNull String toolName,
      @NotNull String description,
      boolean playerOnly,
      String[] aliases) {
    this(
        name,
        toolName,
        description,
        "smessential.command." + name.toLowerCase(),
        playerOnly,
        false,
        aliases);
  }

  public EssentialCommand(
      @NotNull String name,
      @NotNull String toolName,
      @NotNull String description,
      @Nullable String permission,
      boolean playerOnly,
      String[] aliases) {
    this(name, toolName, description, permission, playerOnly, false, aliases);
  }

  public EssentialCommand(
      @NotNull String name,
      @NotNull String toolName,
      @NotNull String description,
      @Nullable String permission,
      boolean playerOnly,
      boolean hiddenFromHelp,
      String[] aliases) {
    this.name = name;
    this.toolName = toolName;
    this.description = description;
    this.permission = permission;
    this.playerOnly = playerOnly;
    this.hiddenFromHelp = hiddenFromHelp;
    this.aliases = aliases;
  }

  public boolean isHiddenFromHelp() {
    return hiddenFromHelp;
  }

  public @NotNull String getName() {
    return name;
  }

  public @NotNull String getToolName() {
    return toolName;
  }

  public @NotNull String getDescription() {
    return description;
  }

  public @NotNull String[] getAliases() {
    return aliases;
  }

  public @Nullable String getPermissionNode() {
    return permission;
  }

  @Override
  public @Nullable String permission() {
    return permission;
  }

  private static dev.smnl.smessential.service.RankService rankService;

  public static void setRankService(@Nullable dev.smnl.smessential.service.RankService service) {
    rankService = service;
  }

  @Override
  public boolean canUse(@NotNull CommandSender sender) {
    if (playerOnly && !(sender instanceof Player)) {
      return false;
    }
    if (permission == null) {
      return true;
    }
    if (rankService != null && sender instanceof Player player) {
      return rankService.hasPermission(player, permission);
    }
    return sender.hasPermission(permission);
  }

  public boolean canUse(@NotNull CommandSourceStack stack) {
    return canUse(stack.getSender());
  }

  public boolean isPlayerOnly() {
    return playerOnly;
  }

  @Override
  public final void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
    CommandSender sender = stack.getSender();

    if (!canUse(sender)) {
      if (playerOnly && !(sender instanceof Player)) {
        sender.sendMessage(
            MessageFormatter.formatError(toolName, "Only players can execute this command."));
      } else {
        sender.sendMessage(MessageFormatter.formatNoPermission(toolName));
      }
      return;
    }

    run(stack, sender, args);
  }

  protected abstract void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args);

  protected @Nullable Player requireOnlinePlayer(
      @NotNull CommandSender sender, @NotNull String name) {
    Player target = Bukkit.getPlayer(name);
    if (target == null) {
      sendError(sender, "'" + name + "' is not online.");
    }
    return target;
  }

  protected @NotNull String joinArgs(@NotNull String[] args, int startIndex) {
    if (startIndex >= args.length) {
      return "";
    }
    return String.join(" ", Arrays.copyOfRange(args, startIndex, args.length));
  }

  protected void sendInfo(@NotNull CommandSender sender, @NotNull String message) {
    sender.sendMessage(MessageFormatter.formatInfo(toolName, message));
  }

  protected void sendError(@NotNull CommandSender sender, @NotNull String message) {
    sender.sendMessage(MessageFormatter.formatError(toolName, message));
  }

  protected void sendUsage(@NotNull CommandSender sender, @NotNull String usage) {
    sender.sendMessage(MessageFormatter.formatError(toolName, "Usage: " + usage));
  }

  public record HelpEntry(@NotNull String command, @NotNull String description) {}

  protected void sendPaginatedHelp(
      @NotNull CommandSender sender,
      @NotNull String headerTitle,
      @NotNull String commandRoot,
      @NotNull java.util.List<HelpEntry> entries,
      int page) {
    int pageSize = 10;
    if (entries.isEmpty()) {
      sendInfo(sender, "No commands available.");
      return;
    }

    int totalPages = Math.max(1, (int) Math.ceil((double) entries.size() / pageSize));
    if (page < 1 || page > totalPages) {
      sendError(sender, "Page " + page + " does not exist (1-" + totalPages + ").");
      return;
    }

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, entries.size());

    String headerText =
        totalPages > 1
            ? headerTitle + " (Page " + page + "/" + totalPages + "):"
            : headerTitle + ":";

    net.kyori.adventure.text.TextComponent.Builder builder =
        net.kyori.adventure.text.Component.text();
    builder.append(net.kyori.adventure.text.Component.newline());
    builder.append(
        MessageFormatter.format(toolName, headerText, MessageFormatter.MessageType.INFO, false));

    for (int i = startIndex; i < endIndex; i++) {
      HelpEntry entry = entries.get(i);
      net.kyori.adventure.text.Component lineComp =
          net.kyori.adventure.text.Component.text(
                  "- ", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
              .append(
                  net.kyori.adventure.text.Component.text(
                      entry.command(), net.kyori.adventure.text.format.NamedTextColor.WHITE))
              .append(
                  net.kyori.adventure.text.Component.text(
                      " - ", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY))
              .append(
                  net.kyori.adventure.text.Component.text(
                      entry.description(), net.kyori.adventure.text.format.NamedTextColor.GRAY));
      builder.append(net.kyori.adventure.text.Component.newline()).append(lineComp);
    }

    if (page < totalPages) {
      String nextPageCmd =
          commandRoot.equalsIgnoreCase("help")
              ? "/help " + (page + 1)
              : "/" + commandRoot + " help " + (page + 1);
      builder
          .append(net.kyori.adventure.text.Component.newline())
          .append(
              net.kyori.adventure.text.Component.text(
                  "Type " + nextPageCmd + " for the next page.",
                  net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY));
    }

    builder.append(net.kyori.adventure.text.Component.newline());
    sender.sendMessage(builder.build());
  }
}
