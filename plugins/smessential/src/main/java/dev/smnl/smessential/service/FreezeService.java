package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FreezeService implements Listener {

  private static final Component ACTION_BAR_MSG =
      Component.text("(Frozen) ", NamedTextColor.DARK_RED)
          .append(Component.text("You are currently frozen by staff.", NamedTextColor.RED));

  private final DatabaseManager databaseManager;
  private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();

  public FreezeService(@Nullable DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public FreezeService() {
    this(null);
  }

  public void setup(@NotNull JavaPlugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);

    Bukkit.getScheduler()
        .runTaskTimer(
            plugin,
            () -> {
              if (frozenPlayers.isEmpty()) return;
              for (UUID uuid : frozenPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                  player.sendActionBar(ACTION_BAR_MSG);
                }
              }
            },
            20L,
            20L);
  }

  public boolean isFrozen(@NotNull UUID uuid) {
    return !frozenPlayers.isEmpty() && frozenPlayers.contains(uuid);
  }

  public boolean freezePlayer(
      @NotNull Player player, @NotNull String staffUuid, @NotNull String reason) {
    boolean added = frozenPlayers.add(player.getUniqueId());
    if (added) {
      player.sendMessage(
          MessageFormatter.formatError("Moderation", "You have been frozen by a staff member."));
      Component msg =
          Component.text("(Frozen) ", NamedTextColor.DARK_RED)
              .append(Component.text("You are currently frozen by staff.", NamedTextColor.RED));
      player.sendActionBar(msg);
      if (databaseManager != null) {
        databaseManager.savePunishment(
            player.getUniqueId().toString(),
            "FREEZE",
            player.getName(),
            reason,
            staffUuid,
            System.currentTimeMillis(),
            0L);
      }
    }
    return added;
  }

  public boolean freezePlayer(@NotNull Player player) {
    return freezePlayer(player, "CONSOLE", "Staff Investigation");
  }

  public boolean unfreezePlayer(@NotNull Player player, @NotNull String staffUuid) {
    boolean removed = frozenPlayers.remove(player.getUniqueId());
    if (removed) {
      player.sendActionBar(Component.empty());
      player.sendMessage(MessageFormatter.formatInfo("Moderation", "You have been unfrozen."));
      if (databaseManager != null) {
        databaseManager.unpunish(
            player.getUniqueId().toString(), "FREEZE", staffUuid, System.currentTimeMillis());
      }
    }
    return removed;
  }

  public boolean unfreezePlayer(@NotNull Player player) {
    return unfreezePlayer(player, "CONSOLE");
  }

  public boolean toggleFreeze(
      @NotNull Player player, @NotNull String staffUuid, @NotNull String reason) {
    if (isFrozen(player.getUniqueId())) {
      unfreezePlayer(player, staffUuid);
      return false;
    } else {
      freezePlayer(player, staffUuid, reason);
      return true;
    }
  }

  public boolean toggleFreeze(@NotNull Player player) {
    return toggleFreeze(player, "CONSOLE", "Staff Investigation");
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!isFrozen(event.getPlayer().getUniqueId())) {
      return;
    }

    Location from = event.getFrom();
    Location to = event.getTo();

    if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
      Location clamped = from.clone();
      clamped.setYaw(to.getYaw());
      clamped.setPitch(to.getPitch());
      event.setTo(clamped);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    if (isFrozen(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
      event
          .getPlayer()
          .sendMessage(
              MessageFormatter.formatError("Moderation", "You cannot break blocks while frozen."));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    if (isFrozen(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
      event
          .getPlayer()
          .sendMessage(
              MessageFormatter.formatError("Moderation", "You cannot place blocks while frozen."));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerDrop(PlayerDropItemEvent event) {
    if (isFrozen(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
      event
          .getPlayer()
          .sendMessage(
              MessageFormatter.formatError("Moderation", "You cannot drop items while frozen."));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityPickup(EntityPickupItemEvent event) {
    if (event.getEntity() instanceof Player player && isFrozen(player.getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (isFrozen(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player && isFrozen(player.getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player damager && isFrozen(damager.getUniqueId())) {
      event.setCancelled(true);
      damager.sendMessage(
          MessageFormatter.formatError(
              "Moderation", "You cannot interact with entities while frozen."));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    if (!isFrozen(event.getPlayer().getUniqueId())) {
      return;
    }

    String message = event.getMessage().toLowerCase().trim();
    String command = message.split(" ")[0];

    boolean allowed =
        command.equals("/msg")
            || command.equals("/reply")
            || command.equals("/r")
            || command.equals("/support")
            || command.equals("/s")
            || command.equals("/help")
            || command.equals("/?")
            || command.equals("/h");

    if (!allowed) {
      event.setCancelled(true);
      event
          .getPlayer()
          .sendMessage(
              MessageFormatter.formatError(
                  "Moderation", "You cannot use this command while frozen."));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (isFrozen(event.getPlayer().getUniqueId())) {
      Component alert =
          Component.text("[ALERT] ", NamedTextColor.RED)
              .append(PlayerUtils.getStaffVisibleDisplayName(event.getPlayer()))
              .append(Component.text(" disconnected while being frozen!", NamedTextColor.DARK_RED));
      Bukkit.broadcast(
          MessageFormatter.formatInfo("Moderation", alert), "smessential.command.moderation");
    }
  }
}
