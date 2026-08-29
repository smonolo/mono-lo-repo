package dev.smnl.smessential.manager;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AdvancementManager implements Listener {

  private final JavaPlugin plugin;

  public AdvancementManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
    Advancement advancement = event.getAdvancement();
    AdvancementDisplay display = advancement.getDisplay();

    if (display == null || !display.doesAnnounceToChat()) {
      event.message(null);
      return;
    }

    Player player = event.getPlayer();
    Component advancementComponent = formatAdvancementDisplay(display);
    Component actionComponent = formatAction(display.frame());
    Component playerDisplay = PlayerUtils.getGeneralDisplayName(player);

    Component messageBody =
        Component.empty()
            .append(playerDisplay)
            .append(actionComponent)
            .append(advancementComponent);

    Component fullMessage = MessageFormatter.formatInfo("World", messageBody);
    event.message(fullMessage);
  }

  private @NotNull Component formatAction(@NotNull AdvancementDisplay.Frame frame) {
    return switch (frame) {
      case GOAL -> Component.text(" has reached the goal ", NamedTextColor.GRAY);
      case CHALLENGE -> Component.text(" has completed the challenge ", NamedTextColor.GRAY);
      case TASK -> Component.text(" has made the advancement ", NamedTextColor.GRAY);
    };
  }

  private @NotNull Component formatAdvancementDisplay(@NotNull AdvancementDisplay display) {
    Component title = display.title();
    Component description = display.description();
    AdvancementDisplay.Frame frame = display.frame();

    NamedTextColor color =
        switch (frame) {
          case CHALLENGE -> NamedTextColor.DARK_PURPLE;
          case GOAL -> NamedTextColor.GOLD;
          case TASK -> NamedTextColor.GREEN;
        };

    Component tooltip =
        Component.empty()
            .append(title.color(color))
            .append(Component.newline())
            .append(description.color(NamedTextColor.GRAY));

    return Component.text("[", color)
        .append(title.color(color))
        .append(Component.text("]", color))
        .hoverEvent(HoverEvent.showText(tooltip));
  }
}
