package dev.smnl.smessential.manager;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MotdManager implements Listener {

  private final JavaPlugin plugin;

  public MotdManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onServerListPing(PaperServerListPingEvent event) {
    Component line1 =
        Component.text()
            .decoration(TextDecoration.BOLD, false)
            .append(Component.text("Headquarters", NamedTextColor.BLUE, TextDecoration.BOLD))
            .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Classic Vanilla Survival", NamedTextColor.GOLD))
            .build();

    Component line2 =
        Component.text()
            .decoration(TextDecoration.BOLD, false)
            .append(Component.text("mc.smnl.dev", NamedTextColor.GOLD))
            .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Paper 1.21.x Cross-Play", NamedTextColor.GRAY))
            .build();

    Component fullMotd =
        Component.text()
            .decoration(TextDecoration.BOLD, false)
            .append(line1)
            .append(Component.newline())
            .append(line2)
            .build();
    event.motd(fullMotd);
  }
}
