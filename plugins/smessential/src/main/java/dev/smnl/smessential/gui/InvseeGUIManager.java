package dev.smnl.smessential.gui;

import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class InvseeGUIManager {

  public void openInventoryGUI(@NotNull Player staff, @NotNull Player target) {
    if (!target.isOnline()) {
      return;
    }

    GUIWindow gui = new GUIWindow(target.getName() + "'s Inventory", 54);
    renderInventoryGUI(gui, staff, target);
    gui.open(staff);
  }

  private void renderInventoryGUI(
      @NotNull GUIWindow gui, @NotNull Player staff, @NotNull Player target) {
    PlayerInventory inv = target.getInventory();

    for (int i = 9; i <= 35; i++) {
      ItemStack item = inv.getItem(i);
      gui.setItem(i - 9, item != null ? item.clone() : null, null);
    }

    for (int i = 0; i <= 8; i++) {
      ItemStack item = inv.getItem(i);
      gui.setItem(27 + i, item != null ? item.clone() : null, null);
    }

    ItemStack helmet = inv.getHelmet();
    gui.setItem(
        36,
        helmet != null
            ? helmet.clone()
            : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name("Helmet (Empty)", NamedTextColor.DARK_GRAY)
                .build(),
        null);

    ItemStack chestplate = inv.getChestplate();
    gui.setItem(
        37,
        chestplate != null
            ? chestplate.clone()
            : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name("Chestplate (Empty)", NamedTextColor.DARK_GRAY)
                .build(),
        null);

    ItemStack leggings = inv.getLeggings();
    gui.setItem(
        38,
        leggings != null
            ? leggings.clone()
            : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name("Leggings (Empty)", NamedTextColor.DARK_GRAY)
                .build(),
        null);

    ItemStack boots = inv.getBoots();
    gui.setItem(
        39,
        boots != null
            ? boots.clone()
            : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name("Boots (Empty)", NamedTextColor.DARK_GRAY)
                .build(),
        null);

    ItemStack offhand = inv.getItemInOffHand();
    gui.setItem(
        40,
        offhand != null && !offhand.getType().isAir()
            ? offhand.clone()
            : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name("Offhand (Empty)", NamedTextColor.DARK_GRAY)
                .build(),
        null);

    Location loc = target.getLocation();
    String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "Unknown";
    gui.setItem(
        41,
        ItemBuilder.of(Material.COMPASS)
            .name("Location", NamedTextColor.GOLD)
            .lore(
                "World: " + worldName,
                "XYZ: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
            .build(),
        null);

    List<String> statusLore = new ArrayList<>();
    statusLore.add("Health: " + Math.round(target.getHealth()) + " HP");
    statusLore.add("Food: " + target.getFoodLevel() + "/20");
    statusLore.add("Level: " + target.getLevel() + " (" + Math.round(target.getExp() * 100) + "%)");
    statusLore.add("Gamemode: " + FontUtils.formatEnumTitleCase(target.getGameMode().name()));
    statusLore.add("Ping: " + target.getPing() + "ms");

    gui.setItem(
        42,
        ItemBuilder.of(Material.GOLDEN_APPLE)
            .name(target.getName() + " Status", NamedTextColor.GOLD)
            .lore(statusLore.toArray(new String[0]))
            .build(),
        null);

    gui.setItem(
        43,
        ItemBuilder.of(Material.ENDER_CHEST)
            .name("View Ender Chest", NamedTextColor.LIGHT_PURPLE)
            .lore("Click to inspect " + target.getName() + "'s Ender Chest")
            .build(),
        event -> openEnderChestGUI(staff, target));

    gui.setItem(
        44,
        ItemBuilder.of(Material.SUNFLOWER)
            .name("Refresh", NamedTextColor.YELLOW)
            .lore("Click to refresh with live inventory")
            .build(),
        event -> {
          if (target.isOnline()) {
            renderInventoryGUI(gui, staff, target);
          } else {
            staff.closeInventory();
          }
        });

    ItemStack filler =
        ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ", NamedTextColor.BLACK).build();
    for (int slot = 45; slot <= 52; slot++) {
      gui.setItem(slot, filler, null);
    }

    gui.setCloseButton(53);
  }

  public void openEnderChestGUI(@NotNull Player staff, @NotNull Player target) {
    if (!target.isOnline()) {
      return;
    }

    GUIWindow gui = new GUIWindow(target.getName() + "'s Ender Chest", 36);
    renderEnderChestGUI(gui, staff, target);
    gui.open(staff);
  }

  private void renderEnderChestGUI(
      @NotNull GUIWindow gui, @NotNull Player staff, @NotNull Player target) {

    for (int i = 0; i < 27; i++) {
      ItemStack item = target.getEnderChest().getItem(i);
      gui.setItem(i, item != null ? item.clone() : null, null);
    }

    ItemStack filler =
        ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ", NamedTextColor.BLACK).build();
    for (int slot = 27; slot <= 30; slot++) {
      gui.setItem(slot, filler, null);
    }

    gui.setItem(
        31,
        ItemBuilder.of(Material.CHEST)
            .name("View Main Inventory", NamedTextColor.GOLD)
            .lore("Click to switch back to main inventory")
            .build(),
        event -> openInventoryGUI(staff, target));

    gui.setItem(
        32,
        ItemBuilder.of(Material.SUNFLOWER)
            .name("Refresh", NamedTextColor.YELLOW)
            .lore("Click to refresh with live ender chest")
            .build(),
        event -> {
          if (target.isOnline()) {
            renderEnderChestGUI(gui, staff, target);
          } else {
            staff.closeInventory();
          }
        });

    for (int slot = 33; slot <= 34; slot++) {
      gui.setItem(slot, filler, null);
    }

    gui.setCloseButton(35);
  }
}
