package dev.smnl.smessential.manager;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RaidManager implements Listener {

  private final JavaPlugin plugin;

  public RaidManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onRaidTrigger(RaidTriggerEvent event) {
    Player player = event.getPlayer();
    Component message =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(player))
            .append(Component.text(" has triggered a Raid!", NamedTextColor.GRAY));

    Component fullMessage = MessageFormatter.formatInfo("World", message);
    PlayerUtils.broadcastMessage(fullMessage);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onRaidFinish(RaidFinishEvent event) {
    List<Player> winners = event.getWinners();
    Component message;

    if (!winners.isEmpty()) {
      Component winnersComp = Component.empty();
      for (int i = 0; i < winners.size(); i++) {
        Player winner = winners.get(i);
        winnersComp = winnersComp.append(PlayerUtils.getGeneralDisplayName(winner));
        if (i < winners.size() - 1) {
          winnersComp = winnersComp.append(Component.text(", ", NamedTextColor.GRAY));
        }
      }

      message =
          Component.text("Victory! The raid was defeated by ", NamedTextColor.GRAY)
              .append(winnersComp)
              .append(Component.text("!", NamedTextColor.GRAY));
    } else {
      message = Component.text("Victory! The raid has been defeated!", NamedTextColor.GRAY);
    }

    Component fullMessage = MessageFormatter.formatInfo("World", message);
    PlayerUtils.broadcastMessage(fullMessage);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onRaidStop(RaidStopEvent event) {
    if (event.getRaid().getStatus() == Raid.RaidStatus.LOSS) {
      Component message =
          MessageFormatter.formatInfo("World", "The village was defeated in the raid.");
      PlayerUtils.broadcastMessage(message);
    }
  }
}
