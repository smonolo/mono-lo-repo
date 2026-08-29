package dev.smnl.smessential.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIManager implements Listener {

  private final JavaPlugin plugin;

  public GUIManager(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory() == null) {
      return;
    }

    try {
      InventoryHolder holder = event.getInventory().getHolder();
      if (holder instanceof GUIWindow guiWindow) {
        event.setCancelled(true);
        guiWindow.handleClick(event);
      } else if (holder != null && holder.getClass().getName().endsWith("GUIWindow")) {
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
          player.closeInventory();
        }
      }
    } catch (Throwable ignored) {
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getInventory() == null) {
      return;
    }

    try {
      InventoryHolder holder = event.getInventory().getHolder();
      if (holder instanceof GUIWindow
          || (holder != null && holder.getClass().getName().endsWith("GUIWindow"))) {
        event.setCancelled(true);
      }
    } catch (Throwable ignored) {
    }
  }
}
