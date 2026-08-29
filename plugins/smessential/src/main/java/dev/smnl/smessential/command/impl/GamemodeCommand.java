package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GamemodeCommand extends EssentialCommand {

  public GamemodeCommand() {
    super(
        "Gamemode",
        "Administration",
        "Sets a player's game mode",
        "smessential.command.administration",
        false,
        new String[] {"gm"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      if (sender instanceof Player) {
        sendUsage(sender, "/gamemode <survival|creative|adventure|spectator> [player]");
      } else {
        sendUsage(sender, "/gamemode <survival|creative|adventure|spectator> <player>");
      }
      return;
    }

    GameMode targetMode = parseGameMode(args[0]);
    if (targetMode == null) {
      sendError(
          sender,
          "Invalid game mode: '" + args[0] + "'. Use survival, creative, adventure, or spectator.");
      return;
    }

    Player target;
    if (args.length >= 2) {
      target = PlayerUtils.findOnlinePlayer(args[1], sender);
      if (target == null) {
        sendError(sender, "'" + args[1] + "' is not online.");
        return;
      }
    } else {
      if (sender instanceof Player player) {
        target = player;
      } else {
        sendUsage(sender, "/gamemode " + args[0] + " <player>");
        return;
      }
    }

    applyGamemode(sender, target, targetMode, getToolName());
  }

  public static void applyGamemode(
      @NotNull CommandSender sender,
      @NotNull Player target,
      @NotNull GameMode targetMode,
      @NotNull String toolName) {
    target.setGameMode(targetMode);
    String modeName = FontUtils.formatEnumTitleCase(targetMode.name());

    target.sendMessage(
        MessageFormatter.formatInfo(toolName, "Your game mode has been set to " + modeName + "."));

    if (target != sender && sender instanceof Player) {
      Component senderMsg =
          Component.text("Set game mode to ", NamedTextColor.GRAY)
              .append(Component.text(modeName, NamedTextColor.WHITE))
              .append(Component.text(" for ", NamedTextColor.GRAY))
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(toolName, senderMsg));
    }

    Component broadcastMsg =
        MessageFormatter.formatInfo(
            toolName,
            Component.empty()
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(Component.text("'s game mode was set to ", NamedTextColor.GRAY))
                .append(Component.text(modeName, NamedTextColor.WHITE))
                .append(Component.text(" by a staff member.", NamedTextColor.GRAY)));

    PlayerUtils.broadcastMessage(broadcastMsg);
  }

  public static @Nullable GameMode parseGameMode(@NotNull String input) {
    String lower = input.toLowerCase(Locale.ROOT);
    return switch (lower) {
      case "survival", "surv", "s", "0" -> GameMode.SURVIVAL;
      case "creative", "creat", "c", "1" -> GameMode.CREATIVE;
      case "adventure", "adv", "a", "2" -> GameMode.ADVENTURE;
      case "spectator", "spec", "sp", "3" -> GameMode.SPECTATOR;
      default -> null;
    };
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      List<String> modes = List.of("survival", "creative", "adventure", "spectator");
      return modes.stream().filter(m -> m.startsWith(prefix)).toList();
    }
    if (args.length == 2) {
      String prefix = args[1];
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
    }
    return Collections.emptyList();
  }

  public static class QuickCreative extends EssentialCommand {
    public QuickCreative() {
      super(
          "gmc",
          "Administration",
          "Sets game mode to Creative",
          "smessential.command.administration",
          false,
          true,
          new String[0]);
    }

    @Override
    protected void run(
        @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
      Player target = resolveTarget(sender, args);
      if (target != null) {
        applyGamemode(sender, target, GameMode.CREATIVE, getToolName());
      }
    }

    @Override
    public @NotNull Collection<String> suggest(
        @NotNull CommandSourceStack stack, @NotNull String[] args) {
      return args.length <= 1
          ? PlayerUtils.getSuggestedPlayerNames(stack.getSender(), args.length == 1 ? args[0] : "")
          : Collections.emptyList();
    }
  }

  public static class QuickSurvival extends EssentialCommand {
    public QuickSurvival() {
      super(
          "gms",
          "Administration",
          "Sets game mode to Survival",
          "smessential.command.administration",
          false,
          true,
          new String[0]);
    }

    @Override
    protected void run(
        @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
      Player target = resolveTarget(sender, args);
      if (target != null) {
        applyGamemode(sender, target, GameMode.SURVIVAL, getToolName());
      }
    }

    @Override
    public @NotNull Collection<String> suggest(
        @NotNull CommandSourceStack stack, @NotNull String[] args) {
      return args.length <= 1
          ? PlayerUtils.getSuggestedPlayerNames(stack.getSender(), args.length == 1 ? args[0] : "")
          : Collections.emptyList();
    }
  }

  public static class QuickSpectator extends EssentialCommand {
    public QuickSpectator() {
      super(
          "gmsp",
          "Administration",
          "Sets game mode to Spectator",
          "smessential.command.administration",
          false,
          true,
          new String[0]);
    }

    @Override
    protected void run(
        @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
      Player target = resolveTarget(sender, args);
      if (target != null) {
        applyGamemode(sender, target, GameMode.SPECTATOR, getToolName());
      }
    }

    @Override
    public @NotNull Collection<String> suggest(
        @NotNull CommandSourceStack stack, @NotNull String[] args) {
      return args.length <= 1
          ? PlayerUtils.getSuggestedPlayerNames(stack.getSender(), args.length == 1 ? args[0] : "")
          : Collections.emptyList();
    }
  }

  public static class QuickAdventure extends EssentialCommand {
    public QuickAdventure() {
      super(
          "gma",
          "Administration",
          "Sets game mode to Adventure",
          "smessential.command.administration",
          false,
          true,
          new String[0]);
    }

    @Override
    protected void run(
        @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
      Player target = resolveTarget(sender, args);
      if (target != null) {
        applyGamemode(sender, target, GameMode.ADVENTURE, getToolName());
      }
    }

    @Override
    public @NotNull Collection<String> suggest(
        @NotNull CommandSourceStack stack, @NotNull String[] args) {
      return args.length <= 1
          ? PlayerUtils.getSuggestedPlayerNames(stack.getSender(), args.length == 1 ? args[0] : "")
          : Collections.emptyList();
    }
  }

  private static @Nullable Player resolveTarget(
      @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length > 0) {
      Player target = PlayerUtils.findOnlinePlayer(args[0], sender);
      if (target == null) {
        sender.sendMessage(
            MessageFormatter.formatError("Administration", "'" + args[0] + "' is not online."));
        return null;
      }
      return target;
    }
    if (sender instanceof Player player) {
      return player;
    }
    sender.sendMessage(
        MessageFormatter.formatError("Administration", "Usage: /<command> <player>"));
    return null;
  }
}
