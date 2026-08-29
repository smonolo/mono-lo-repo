package dev.smnl.smessential.util;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemBuilder {

  private final ItemStack itemStack;
  private final ItemMeta meta;

  private ItemBuilder(Material material) {
    this.itemStack = new ItemStack(material);
    this.meta = itemStack.getItemMeta();
  }

  public static ItemBuilder of(Material material) {
    return new ItemBuilder(material);
  }

  public ItemBuilder name(String name, NamedTextColor color) {
    if (meta != null) {
      meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
    }
    return this;
  }

  public ItemBuilder componentName(Component component) {
    if (meta != null) {
      meta.displayName(component.decoration(TextDecoration.ITALIC, false));
    }
    return this;
  }

  public ItemBuilder lore(String... lines) {
    if (meta != null && lines.length > 0) {
      List<Component> loreList = new ArrayList<>();
      for (String line : lines) {
        loreList.add(
            Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
      }
      meta.lore(loreList);
    }
    return this;
  }

  public ItemBuilder lore(List<String> lines) {
    if (meta != null && lines != null && !lines.isEmpty()) {
      List<Component> loreList = new ArrayList<>(lines.size());
      for (String line : lines) {
        loreList.add(
            Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
      }
      meta.lore(loreList);
    }
    return this;
  }

  public ItemBuilder componentLore(List<Component> loreList) {
    if (meta != null && loreList != null) {
      meta.lore(loreList.stream().map(c -> c.decoration(TextDecoration.ITALIC, false)).toList());
    }
    return this;
  }

  public ItemBuilder skullOwner(OfflinePlayer player) {
    if (meta instanceof SkullMeta skullMeta) {
      skullMeta.setOwningPlayer(player);
    }
    return this;
  }

  public ItemBuilder glow(boolean glow) {
    if (meta != null) {
      meta.setEnchantmentGlintOverride(glow);
    }
    return this;
  }

  public ItemBuilder amount(int amount) {
    itemStack.setAmount(amount);
    return this;
  }

  public ItemStack build() {
    if (meta != null) {
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }
}
