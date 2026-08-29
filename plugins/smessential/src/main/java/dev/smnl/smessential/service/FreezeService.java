package dev.smnl.smessential.service;

import dev.smnl.smessential.util.MessageFormatter;
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

public class FreezeService implements Listener {

  private static final Component ACTION_BAR_MSG =
      Component.text("(Frozen) ", NamedTextColor.DARK_RED)
          .append(Component.text("You are currently frozen by staff.", NamedTextColor.RED));

  private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();

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

  public boolean freezePlayer(@NotNull Player player) {
    boolean added = frozenPlayers.add(player.getUniqueId());
    if (added) {
      player.sendMessage(
          MessageFormatter.formatError("Moderation", "You have been frozen by a staff member."));
      Component msg =
          Component.text("(Frozen) ", NamedTextColor.DARK_RED)
              .append(Component.text("You are currently frozen by staff.", NamedTextColor.RED));
      player.sendActionBar(msg);
    }
    return added;
  }

  public boolean unfreezePlayer(@NotNull Player player) {
    boolean removed = frozenPlayers.remove(player.getUniqueId());
    if (removed) {
      player.sendActionBar(Component.empty());
      player.sendMessage(MessageFormatter.formatInfo("Moderation", "You have been unfrozen."));
    }
    return removed;
  }

  public boolean toggleFreeze(@NotNull Player player) {
    if (isFrozen(player.getUniqueId())) {
      unfreezePlayer(player);
      return false;
    } else {
      freezePlayer(player);
      return true;
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!isFrozen(event.getPlayer().getUniqueId())) {
      return;
    }

    Location from = event.getFrom();
    Location to = event.getTo();

    if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
      Location newLoc = from.clone();
      newLoc.setYaw(to.getYaw());
      newLoc.setPitch(to.getPitch());
      event.setTo(newLoc);
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
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (isFrozen(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (isFrozen(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityPickupItem(EntityPickupItemEvent event) {
    if (event.getEntity() instanceof Player player && isFrozen(player.getUniqueId())) {
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
    if (event.getDamager() instanceof Player player && isFrozen(player.getUniqueId())) {
      event.setCancelled(true);
      player.sendMessage(
          MessageFormatter.formatError("Moderation", "You cannot attack while frozen."));
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    if (!isFrozen(player.getUniqueId())) {
      return;
    }

    String message = event.getMessage().trim().toLowerCase();
    String command = message.split("\\s+")[0];

    if (command.equals("/msg")
        || command.equals("/reply")
        || command.equals("/r")
        || command.equals("/support")
        || command.equals("/s")
        || command.equals("/help")
        || command.equals("/?")) {
      return;
    }

    event.setCancelled(true);
    player.sendMessage(
        MessageFormatter.formatError("Moderation", "You cannot use commands while frozen."));
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (frozenPlayers.remove(player.getUniqueId())) {
      Component staffAlert =
          Component.text("[HQ] ", NamedTextColor.BLUE)
              .append(Component.text("(Staff) ", NamedTextColor.GOLD))
              .append(
                  Component.text(
                      "Frozen player " + player.getName() + " disconnected from the server!",
                      NamedTextColor.RED));

      for (Player staff : Bukkit.getOnlinePlayers()) {
        if (staff.hasPermission("smessential.command.moderation")) {
          staff.sendMessage(staffAlert);
        }
      }
    }
  }
}
