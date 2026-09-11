package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EffectCommand extends EssentialCommand {

  private static final int DEFAULT_DURATION_TICKS = 30 * 20;
  private static final int MAX_SECONDS = 1_000_000;
  private static final int MAX_AMPLIFIER = 255;
  private static final List<String> EFFECT_NAMES = new ArrayList<>();

  static {
    try {
      for (PotionEffectType type : PotionEffectType.values()) {
        String key = type.getKey().getKey().toLowerCase(Locale.ROOT);
        if (!EFFECT_NAMES.contains(key)) {
          EFFECT_NAMES.add(key);
        }
        String enumName = type.getName().toLowerCase(Locale.ROOT);
        if (!EFFECT_NAMES.contains(enumName)) {
          EFFECT_NAMES.add(enumName);
        }
      }
    } catch (Throwable ignored) {
    }
    Collections.sort(EFFECT_NAMES);
  }

  public EffectCommand() {
    super(
        "Effect",
        "Administration",
        "Gives or clears status effects",
        "smessential.command.administration",
        false,
        new String[0]);
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      if (sender instanceof Player player) {
        displayActiveEffects(player, player);
      } else {
        sendUsage(
            sender,
            "/effect <give|clear> <player> [effect] [seconds|infinite] [amplifier]"
                + " [hideParticles]");
      }
      return;
    }

    String sub = args[0].toLowerCase(Locale.ROOT);
    switch (sub) {
      case "give" -> handleGive(sender, args);
      case "clear" -> handleClear(sender, args);
      case "list" -> handleList(sender, args);
      default ->
          sendUsage(
              sender,
              "/effect <give|clear> <player> [effect] [seconds|infinite] [amplifier]"
                  + " [hideParticles]");
    }
  }

  private void handleGive(@NotNull CommandSender sender, @NotNull String[] args) {
    // /effect give <player> <effect> [seconds|infinite] [amplifier] [hideParticles]
    // /effect give <effect> [seconds|infinite] [amplifier] [hideParticles] (self)
    if (args.length < 2) {
      sendUsage(sender, "/effect give <player> <effect> [seconds|infinite] [amplifier]");
      return;
    }

    Player target;
    PotionEffectType type;
    int valueIndex;

    if (args.length == 2) {
      if (!(sender instanceof Player player)) {
        sendUsage(sender, "/effect give <player> <effect> [seconds|infinite] [amplifier]");
        return;
      }
      target = player;
      type = matchEffect(args[1]);
      if (type == null) {
        sendError(sender, "'" + args[1] + "' is not a valid effect.");
        return;
      }
      valueIndex = 2;
    } else {
      Player found = PlayerUtils.findOnlinePlayer(args[1]);
      if (found != null) {
        target = found;
        type = matchEffect(args[2]);
        if (type == null) {
          sendError(sender, "'" + args[2] + "' is not a valid effect.");
          return;
        }
        valueIndex = 3;
      } else if (sender instanceof Player player) {
        target = player;
        type = matchEffect(args[1]);
        if (type == null) {
          sendError(sender, "'" + args[1] + "' is not online.");
          return;
        }
        valueIndex = 2;
      } else {
        sendError(sender, "'" + args[1] + "' is not online.");
        return;
      }
    }

    int durationTicks = DEFAULT_DURATION_TICKS;
    if (args.length > valueIndex) {
      Integer parsed = parseDurationToTicks(args[valueIndex]);
      if (parsed == null) {
        sendError(
            sender,
            "Invalid duration: '"
                + args[valueIndex]
                + "'. Use seconds (1-"
                + MAX_SECONDS
                + ") or infinite.");
        return;
      }
      durationTicks = parsed;
    }

    int amplifier = 0;
    if (args.length > valueIndex + 1) {
      Integer parsed = parseAmplifier(args[valueIndex + 1]);
      if (parsed == null) {
        sendError(
            sender,
            "Invalid amplifier: '" + args[valueIndex + 1] + "'. Use 0-" + MAX_AMPLIFIER + ".");
        return;
      }
      amplifier = parsed;
    }

    boolean hideParticles = false;
    if (args.length > valueIndex + 2) {
      Boolean parsed = parseBoolean(args[valueIndex + 2]);
      if (parsed == null) {
        sendError(
            sender, "Invalid hideParticles value: '" + args[valueIndex + 2] + "'. Use true/false.");
        return;
      }
      hideParticles = parsed;
    }

    target.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, false, !hideParticles));

    String effectName = FontUtils.formatEnumTitleCase(type.getKey().getKey());
    String level = " " + toRoman(amplifier + 1);
    String durationString =
        durationTicks == PotionEffect.INFINITE_DURATION ? "infinite" : formatTicks(durationTicks);
    Component effectComp =
        Component.text(effectName + level + " (" + durationString + ")", NamedTextColor.WHITE);

    Component targetMsg =
        Component.text("You were given ", NamedTextColor.GRAY)
            .append(effectComp)
            .append(Component.text(".", NamedTextColor.GRAY));
    target.sendMessage(MessageFormatter.formatInfo(getToolName(), targetMsg));

    if (target != sender && sender instanceof Player) {
      Component senderMsg =
          Component.text("Gave ", NamedTextColor.GRAY)
              .append(effectComp)
              .append(Component.text(" to ", NamedTextColor.GRAY))
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), senderMsg));
    }

    Component broadcastMsg =
        MessageFormatter.formatInfo(
            getToolName(),
            Component.empty()
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(Component.text(" was given ", NamedTextColor.GRAY))
                .append(effectComp)
                .append(Component.text(" by a staff member.", NamedTextColor.GRAY)));

    PlayerUtils.broadcastMessage(broadcastMsg);

    if (!(sender instanceof Player)) {
      sender.sendMessage(broadcastMsg);
    }
  }

  private void handleClear(@NotNull CommandSender sender, @NotNull String[] args) {
    // /effect clear <player> [effect]
    // /effect clear [effect] (self)
    // /effect clear (self, all)
    Player target;
    PotionEffectType type = null;

    if (args.length == 1) {
      if (!(sender instanceof Player player)) {
        sendUsage(sender, "/effect clear <player> [effect]");
        return;
      }
      target = player;
    } else if (args.length == 2) {
      Player found = PlayerUtils.findOnlinePlayer(args[1]);
      if (found != null) {
        target = found;
      } else if (sender instanceof Player player) {
        type = matchEffect(args[1]);
        if (type == null) {
          sendError(sender, "'" + args[1] + "' is not online.");
          return;
        }
        target = player;
      } else {
        sendError(sender, "'" + args[1] + "' is not online.");
        return;
      }
    } else {
      target = PlayerUtils.findOnlinePlayer(args[1]);
      if (target == null) {
        sendError(sender, "'" + args[1] + "' is not online.");
        return;
      }
      type = matchEffect(args[2]);
      if (type == null) {
        sendError(sender, "'" + args[2] + "' is not a valid effect.");
        return;
      }
    }

    int removed;
    if (type == null) {
      removed = target.getActivePotionEffects().size();
      for (PotionEffect active : List.copyOf(target.getActivePotionEffects())) {
        target.removePotionEffect(active.getType());
      }
    } else {
      removed = target.hasPotionEffect(type) ? 1 : 0;
      target.removePotionEffect(type);
    }

    String effectName =
        type == null ? "all effects" : FontUtils.formatEnumTitleCase(type.getKey().getKey());

    if (removed == 0) {
      sendError(
          sender,
          PlayerUtils.resolvePlayerName(target)
              + " has no "
              + (type == null ? "active effects." : "active '" + effectName + "' effect."));
      return;
    }

    Component effectComp = Component.text(effectName, NamedTextColor.WHITE);

    Component targetMsg =
        type == null
            ? Component.text("Your effects have been cleared.", NamedTextColor.GRAY)
            : Component.text("Your ", NamedTextColor.GRAY)
                .append(effectComp)
                .append(Component.text(" effect has been cleared.", NamedTextColor.GRAY));
    target.sendMessage(MessageFormatter.formatInfo(getToolName(), targetMsg));

    if (target != sender && sender instanceof Player) {
      Component senderMsg =
          Component.text("Cleared ", NamedTextColor.GRAY)
              .append(effectComp)
              .append(Component.text(" for ", NamedTextColor.GRAY))
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), senderMsg));
    }

    Component broadcastMsg =
        MessageFormatter.formatInfo(
            getToolName(),
            Component.empty()
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(Component.text("'s ", NamedTextColor.GRAY))
                .append(effectComp)
                .append(
                    Component.text(
                        type == null ? " were cleared" : " effect was cleared",
                        NamedTextColor.GRAY))
                .append(Component.text(" by a staff member.", NamedTextColor.GRAY)));

    PlayerUtils.broadcastMessage(broadcastMsg);

    if (!(sender instanceof Player)) {
      sender.sendMessage(broadcastMsg);
    }
  }

  private void handleList(@NotNull CommandSender sender, @NotNull String[] args) {
    Player target;
    if (args.length >= 2) {
      target = PlayerUtils.findOnlinePlayer(args[1]);
      if (target == null) {
        sendError(sender, "'" + args[1] + "' is not online.");
        return;
      }
    } else if (sender instanceof Player player) {
      target = player;
    } else {
      sendUsage(sender, "/effect list <player>");
      return;
    }
    displayActiveEffects(sender, target);
  }

  private void displayActiveEffects(@NotNull CommandSender viewer, @NotNull Player target) {
    Collection<PotionEffect> active = target.getActivePotionEffects();
    if (active.isEmpty()) {
      Component msg =
          Component.text(PlayerUtils.resolvePlayerName(target), NamedTextColor.WHITE)
              .append(Component.text(" has no active effects.", NamedTextColor.GRAY));
      viewer.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      return;
    }

    Component msg =
        Component.text(
            "Active effects for " + PlayerUtils.resolvePlayerName(target) + ": ",
            NamedTextColor.GRAY);
    boolean first = true;
    for (PotionEffect effect : active) {
      String name =
          FontUtils.formatEnumTitleCase(effect.getType().getKey().getKey())
              + " "
              + toRoman(effect.getAmplifier() + 1);
      if (!first) {
        msg = msg.append(Component.text(", ", NamedTextColor.GRAY));
      }
      first = false;
      msg = msg.append(Component.text(name, NamedTextColor.WHITE));
    }
    msg = msg.append(Component.text(".", NamedTextColor.GRAY));
    viewer.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private @Nullable PotionEffectType matchEffect(@NotNull String input) {
    String clean = input.toLowerCase(Locale.ROOT);
    if (clean.startsWith("minecraft:")) {
      clean = clean.substring("minecraft:".length());
    }
    String normalized = clean.replace('-', '_').replace(' ', '_');

    PotionEffectType byName = PotionEffectType.getByName(normalized);
    if (byName != null) {
      return byName;
    }
    PotionEffectType byUpper = PotionEffectType.getByName(normalized.toUpperCase(Locale.ROOT));
    if (byUpper != null) {
      return byUpper;
    }

    try {
      for (PotionEffectType type : PotionEffectType.values()) {
        if (type.getKey().getKey().equalsIgnoreCase(normalized)
            || type.getName().equalsIgnoreCase(normalized)) {
          return type;
        }
      }
    } catch (Throwable ignored) {
    }

    return null;
  }

  private @Nullable Integer parseDurationToTicks(@NotNull String input) {
    String lower = input.toLowerCase(Locale.ROOT);
    if (lower.equals("infinite") || lower.equals("inf") || lower.equals("forever")) {
      return PotionEffect.INFINITE_DURATION;
    }
    try {
      if (lower.endsWith("h")) {
        long hours = Long.parseLong(lower.substring(0, lower.length() - 1));
        if (hours < 0 || hours * 3600 > MAX_SECONDS) {
          return null;
        }
        return (int) (hours * 3600 * 20);
      }
      if (lower.endsWith("m")) {
        long minutes = Long.parseLong(lower.substring(0, lower.length() - 1));
        if (minutes < 0 || minutes * 60 > MAX_SECONDS) {
          return null;
        }
        return (int) (minutes * 60 * 20);
      }
      if (lower.endsWith("s")) {
        long seconds = Long.parseLong(lower.substring(0, lower.length() - 1));
        if (seconds <= 0 || seconds > MAX_SECONDS) {
          return null;
        }
        return (int) (seconds * 20);
      }
      if (lower.endsWith("t")) {
        long ticks = Long.parseLong(lower.substring(0, lower.length() - 1));
        if (ticks <= 0) {
          return null;
        }
        return (int) Math.min(ticks, (long) MAX_SECONDS * 20);
      }
      long seconds = Long.parseLong(lower);
      if (seconds <= 0 || seconds > MAX_SECONDS) {
        return null;
      }
      return (int) (seconds * 20);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private @Nullable Integer parseAmplifier(@NotNull String input) {
    try {
      int value = Integer.parseInt(input.toLowerCase(Locale.ROOT).replaceAll("^(lvl|level)", ""));
      return value >= 0 && value <= MAX_AMPLIFIER ? value : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private @Nullable Boolean parseBoolean(@NotNull String input) {
    String lower = input.toLowerCase(Locale.ROOT);
    return switch (lower) {
      case "true", "yes", "1" -> true;
      case "false", "no", "0" -> false;
      default -> null;
    };
  }

  private @NotNull String formatTicks(int ticks) {
    long seconds = ticks / 20L;
    if (seconds >= 3600) {
      long hours = seconds / 3600;
      long remainingM = (seconds % 3600) / 60;
      return hours + "h" + (remainingM > 0 ? " " + remainingM + "m" : "");
    }
    if (seconds >= 60) {
      long minutes = seconds / 60;
      long remainingS = seconds % 60;
      return minutes + "m" + (remainingS > 0 ? " " + remainingS + "s" : "");
    }
    return seconds + "s";
  }

  private @NotNull String toRoman(int number) {
    return switch (number) {
      case 1 -> "I";
      case 2 -> "II";
      case 3 -> "III";
      case 4 -> "IV";
      case 5 -> "V";
      case 6 -> "VI";
      case 7 -> "VII";
      case 8 -> "VIII";
      case 9 -> "IX";
      case 10 -> "X";
      default -> String.valueOf(number);
    };
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      List<String> options = List.of("give", "clear", "list");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    String sub = args[0].toLowerCase(Locale.ROOT);
    switch (sub) {
      case "give" -> {
        if (args.length == 2) {
          List<String> suggestions = new ArrayList<>();
          String prefix = args[1].toLowerCase(Locale.ROOT);
          for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
              suggestions.add(p.getName());
            }
          }
          if (commandSourceStack.getSender() instanceof Player) {
            for (String effect : EFFECT_NAMES) {
              if (effect.startsWith(prefix)) {
                suggestions.add(effect);
              }
            }
          }
          return suggestions;
        }
        if (args.length == 3) {
          String prefix = args[2].toLowerCase(Locale.ROOT);
          List<String> suggestions = new ArrayList<>();
          for (String effect : EFFECT_NAMES) {
            if (effect.startsWith(prefix)) {
              suggestions.add(effect);
            }
          }
          return suggestions;
        }
        if (args.length == 4) {
          String prefix = args[3].toLowerCase(Locale.ROOT);
          List<String> options = List.of("30s", "60s", "5m", "10m", "infinite");
          return options.stream().filter(s -> s.startsWith(prefix)).toList();
        }
        if (args.length == 5) {
          String prefix = args[4].toLowerCase(Locale.ROOT);
          List<String> options = List.of("0", "1", "2", "4", "9");
          return options.stream().filter(s -> s.startsWith(prefix)).toList();
        }
        if (args.length == 6) {
          String prefix = args[5].toLowerCase(Locale.ROOT);
          List<String> options = List.of("true", "false");
          return options.stream().filter(s -> s.startsWith(prefix)).toList();
        }
      }
      case "clear" -> {
        if (args.length == 2) {
          String prefix = args[1].toLowerCase(Locale.ROOT);
          List<String> suggestions = new ArrayList<>();
          for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
              suggestions.add(p.getName());
            }
          }
          for (String effect : EFFECT_NAMES) {
            if (effect.startsWith(prefix)) {
              suggestions.add(effect);
            }
          }
          return suggestions;
        }
        if (args.length == 3) {
          String prefix = args[2].toLowerCase(Locale.ROOT);
          Player target = PlayerUtils.findOnlinePlayer(args[1]);
          List<String> suggestions = new ArrayList<>();
          if (target != null) {
            for (PotionEffect active : target.getActivePotionEffects()) {
              String key = active.getType().getKey().getKey().toLowerCase(Locale.ROOT);
              if (key.startsWith(prefix)) {
                suggestions.add(key);
              }
            }
          }
          for (String effect : EFFECT_NAMES) {
            if (effect.startsWith(prefix) && !suggestions.contains(effect)) {
              suggestions.add(effect);
            }
          }
          return suggestions;
        }
      }
      case "list" -> {
        if (args.length == 2) {
          String prefix = args[1];
          return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
        }
      }
      default -> {
        return Collections.emptyList();
      }
    }
    return Collections.emptyList();
  }
}
