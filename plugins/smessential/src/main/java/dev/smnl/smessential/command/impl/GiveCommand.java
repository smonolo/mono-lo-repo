package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GiveCommand extends EssentialCommand {

  private static final int MAX_AMOUNT = 64 * 36; // Full inventory maximum (2304)
  private static final List<String> ITEM_NAMES = new ArrayList<>();

  static {
    try {
      for (Material m : Registry.MATERIAL) {
        if (m.isItem() && !m.isAir()) {
          ITEM_NAMES.add(m.name().toLowerCase(Locale.ROOT));
          ITEM_NAMES.add(m.getKey().getKey().toLowerCase(Locale.ROOT));
        }
      }
    } catch (Throwable ignored) {
    }
    Collections.sort(ITEM_NAMES);
  }

  public GiveCommand() {
    super(
        "Give",
        "Administration",
        "Gives items to a player",
        "smessential.command.administration",
        false,
        new String[] {"item", "i"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      if (sender instanceof Player) {
        sendUsage(sender, "/give <player> <item> [amount] or /give <item> [amount]");
      } else {
        sendUsage(sender, "/give <player> <item> [amount]");
      }
      return;
    }

    Player target = null;
    Material material = null;
    int amount = 1;

    if (args.length == 1) {
      if (sender instanceof Player player) {
        material = matchMaterial(args[0]);
        if (material != null && material.isItem() && !material.isAir()) {
          target = player;
        } else {
          Player foundPlayer = PlayerUtils.findOnlinePlayer(args[0]);
          if (foundPlayer != null) {
            sendUsage(sender, "/give " + args[0] + " <item> [amount]");
            return;
          }
          sendError(sender, "'" + args[0] + "' is not a valid item or player.");
          return;
        }
      } else {
        sendUsage(sender, "/give <player> <item> [amount]");
        return;
      }
    } else if (args.length == 2) {
      Player foundPlayer = PlayerUtils.findOnlinePlayer(args[0]);
      if (foundPlayer != null) {
        target = foundPlayer;
        material = matchMaterial(args[1]);
        if (material == null || !material.isItem() || material.isAir()) {
          sendError(sender, "'" + args[1] + "' is not a valid item.");
          return;
        }
      } else if (sender instanceof Player player) {
        material = matchMaterial(args[0]);
        if (material != null && material.isItem() && !material.isAir()) {
          Integer parsed = parseAmount(args[1]);
          if (parsed == null || parsed <= 0) {
            sendError(sender, "Invalid amount: '" + args[1] + "'. Must be a positive number.");
            return;
          }
          target = player;
          amount = parsed;
        } else {
          sendError(sender, "'" + args[0] + "' is not online.");
          return;
        }
      } else {
        sendError(sender, "'" + args[0] + "' is not online.");
        return;
      }
    } else {
      target = PlayerUtils.findOnlinePlayer(args[0]);
      if (target == null) {
        sendError(sender, "'" + args[0] + "' is not online.");
        return;
      }
      material = matchMaterial(args[1]);
      if (material == null || !material.isItem() || material.isAir()) {
        sendError(sender, "'" + args[1] + "' is not a valid item.");
        return;
      }
      Integer parsed = parseAmount(args[2]);
      if (parsed == null || parsed <= 0) {
        sendError(sender, "Invalid amount: '" + args[2] + "'. Must be a positive number.");
        return;
      }
      amount = parsed;
    }

    if (amount > MAX_AMOUNT) {
      sendError(sender, "Amount cannot exceed " + MAX_AMOUNT + ".");
      return;
    }

    giveItems(target, material, amount);

    String formattedItemName = FontUtils.formatEnumTitleCase(material.name());
    Component itemComp = Component.text(amount + "x " + formattedItemName, NamedTextColor.WHITE);

    Component targetMsg =
        Component.text("You were given ", NamedTextColor.GRAY)
            .append(itemComp)
            .append(Component.text(".", NamedTextColor.GRAY));
    target.sendMessage(MessageFormatter.formatInfo(getToolName(), targetMsg));

    if (target != sender && sender instanceof Player) {
      Component senderMsg =
          Component.text("Gave ", NamedTextColor.GRAY)
              .append(itemComp)
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
                .append(itemComp)
                .append(Component.text(" by a staff member.", NamedTextColor.GRAY)));

    PlayerUtils.broadcastMessage(broadcastMsg);

    if (!(sender instanceof Player)) {
      sender.sendMessage(broadcastMsg);
    }
  }

  private void giveItems(@NotNull Player player, @NotNull Material material, int amount) {
    int remaining = amount;
    int maxStack = material.getMaxStackSize();
    while (remaining > 0) {
      int stackSize = Math.min(remaining, maxStack);
      ItemStack stack = new ItemStack(material, stackSize);
      HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
      if (!leftover.isEmpty()) {
        for (ItemStack drop : leftover.values()) {
          player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
      }
      remaining -= stackSize;
    }
  }

  private @Nullable Material matchMaterial(@NotNull String input) {
    String clean = input.toLowerCase(Locale.ROOT);
    if (clean.startsWith("minecraft:")) {
      clean = clean.substring("minecraft:".length());
    }

    try {
      NamespacedKey key = NamespacedKey.minecraft(clean);
      Material mat = Registry.MATERIAL.get(key);
      if (mat != null && mat.isItem() && !mat.isAir()) {
        return mat;
      }
    } catch (Throwable ignored) {
    }

    String normalized = clean.replace('-', '_');
    try {
      NamespacedKey key = NamespacedKey.minecraft(normalized);
      Material mat = Registry.MATERIAL.get(key);
      if (mat != null && mat.isItem() && !mat.isAir()) {
        return mat;
      }
    } catch (Throwable ignored) {
    }

    Material mat = Material.matchMaterial(clean, false);
    if (mat == null) {
      mat = Material.matchMaterial(normalized, false);
    }
    if (mat != null && mat.isItem() && !mat.isAir()) {
      return mat;
    }

    return null;
  }

  private @Nullable Integer parseAmount(@NotNull String input) {
    try {
      return Integer.parseInt(input);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length == 0 || args.length == 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      List<String> suggestions = new ArrayList<>();
      for (Player p : Bukkit.getOnlinePlayers()) {
        String name = p.getName();
        if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
          suggestions.add(name);
        }
      }
      if (commandSourceStack.getSender() instanceof Player) {
        for (String key : ITEM_NAMES) {
          if (key.startsWith(prefix)) {
            suggestions.add(key);
          }
        }
      }
      return suggestions;
    }

    if (args.length == 2) {
      String prefix = args[1].toLowerCase(Locale.ROOT);
      List<String> suggestions = new ArrayList<>();
      for (String key : ITEM_NAMES) {
        if (key.startsWith(prefix)) {
          suggestions.add(key);
        }
      }
      Material mat = matchMaterial(args[0]);
      if (mat != null && mat.isItem() && !mat.isAir()) {
        for (String amt : List.of("1", "16", "32", "64")) {
          if (amt.startsWith(prefix)) {
            suggestions.add(amt);
          }
        }
      }
      return suggestions;
    }

    if (args.length == 3) {
      String prefix = args[2].toLowerCase(Locale.ROOT);
      List<String> suggestions = new ArrayList<>();
      for (String amt : List.of("1", "16", "32", "64")) {
        if (amt.startsWith(prefix)) {
          suggestions.add(amt);
        }
      }
      return suggestions;
    }

    return Collections.emptyList();
  }
}
