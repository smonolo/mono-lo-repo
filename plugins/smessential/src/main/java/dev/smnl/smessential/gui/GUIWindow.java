package dev.smnl.smessential.gui;

import dev.smnl.smessential.util.ItemBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GUIWindow implements InventoryHolder {

  private final Inventory inventory;
  private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();

  public GUIWindow(String title, int size) {
    Component titleComp = Component.text(title, NamedTextColor.BLUE, TextDecoration.BOLD);
    this.inventory = Bukkit.createInventory(this, size, titleComp);
  }

  public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
    inventory.setItem(slot, item);
    if (onClick != null) {
      clickHandlers.put(slot, onClick);
    } else {
      clickHandlers.remove(slot);
    }
  }

  public void setItem(
      int slot,
      Material material,
      String name,
      NamedTextColor color,
      String[] lore,
      Consumer<InventoryClickEvent> onClick) {
    ItemBuilder builder = ItemBuilder.of(material).name(name, color);
    if (lore != null && lore.length > 0) {
      builder.lore(lore);
    }
    setItem(slot, builder.build(), onClick);
  }

  public void setCloseButton() {
    setCloseButton(inventory.getSize() - 1);
  }

  public void setCloseButton(int slot) {
    ItemStack closeItem =
        ItemBuilder.of(Material.BARRIER).name("Close", NamedTextColor.RED).build();
    setItem(
        slot,
        closeItem,
        event -> {
          if (event.getWhoClicked() instanceof Player player) {
            player.closeInventory();
          }
        });
  }

  public void handleClick(InventoryClickEvent event) {
    Consumer<InventoryClickEvent> handler = clickHandlers.get(event.getRawSlot());
    if (handler != null) {
      handler.accept(event);
    }
  }

  public void open(Player player) {
    player.openInventory(inventory);
  }

  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }
}
