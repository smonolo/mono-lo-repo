package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.FreezeService;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HealCommand extends EssentialCommand {

  private final FreezeService freezeService;

  public HealCommand(@Nullable FreezeService freezeService) {
    super(
        "Heal",
        "Moderation",
        "Restores a player to full health and clears harmful effects",
        "smessential.command.moderation",
        false,
        new String[0]);
    this.freezeService = freezeService;
  }

  public HealCommand() {
    this(null);
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    Player target;
    if (args.length > 0) {
      target = PlayerUtils.findOnlinePlayer(args[0], sender);
      if (target == null) {
        sendError(sender, "'" + args[0] + "' is not online.");
        return;
      }
    } else {
      if (sender instanceof Player player) {
        target = player;
      } else {
        sendUsage(sender, "/heal <player>");
        return;
      }
    }

    if (freezeService != null && freezeService.isFrozen(target.getUniqueId())) {
      sendError(sender, "You cannot heal a frozen player.");
      return;
    }

    double maxHealth = PlayerUtils.getPlayerMaxHealth(target);
    target.setHealth(maxHealth);
    target.setFoodLevel(20);
    target.setSaturation(20.0f);
    target.setFireTicks(0);

    for (PotionEffect effect : target.getActivePotionEffects()) {
      PotionEffectType type = effect.getType();
      if (type.equals(PotionEffectType.POISON)
          || type.equals(PotionEffectType.WITHER)
          || type.equals(PotionEffectType.SLOWNESS)
          || type.equals(PotionEffectType.MINING_FATIGUE)
          || type.equals(PotionEffectType.BLINDNESS)
          || type.equals(PotionEffectType.NAUSEA)
          || type.equals(PotionEffectType.HUNGER)
          || type.equals(PotionEffectType.WEAKNESS)
          || type.equals(PotionEffectType.LEVITATION)
          || type.equals(PotionEffectType.UNLUCK)
          || type.equals(PotionEffectType.BAD_OMEN)
          || type.equals(PotionEffectType.DARKNESS)) {
        target.removePotionEffect(type);
      }
    }

    target.sendMessage(
        MessageFormatter.formatInfo(getToolName(), "You have been restored to full health."));

    if (target != sender && sender instanceof Player) {
      Component msg =
          Component.text("Healed ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
    }

    Component broadcastMsg =
        MessageFormatter.formatInfo(
            getToolName(),
            Component.empty()
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(Component.text(" was healed by a staff member.", NamedTextColor.GRAY)));

    PlayerUtils.broadcastMessage(broadcastMsg);
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0] : "";
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
    }
    return Collections.emptyList();
  }
}
