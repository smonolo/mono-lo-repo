package dev.smnl.smessential.manager;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BossManager implements Listener {

  private final JavaPlugin plugin;

  public BossManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @SuppressWarnings("deprecation")
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CHUNK_GEN
        || (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL
            && !(event.getEntity() instanceof Warden))) {
      return;
    }

    if (event.getEntity() instanceof Wither) {
      PlayerUtils.broadcastMessage(
          MessageFormatter.formatInfo("World", "The Wither has been summoned!"));
    } else if (event.getEntity() instanceof Warden) {
      PlayerUtils.broadcastMessage(
          MessageFormatter.formatInfo("World", "A Warden has emerged from the darkness!"));
    } else if (event.getEntity() instanceof EnderDragon
        && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DEFAULT) {
      PlayerUtils.broadcastMessage(
          MessageFormatter.formatInfo("World", "The Ender Dragon has appeared in The End!"));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    Player killer = entity.getKiller();
    Component killerDisplay = killer != null ? PlayerUtils.getGeneralDisplayName(killer) : null;

    if (entity instanceof EnderDragon) {
      if (killerDisplay != null) {
        Component body =
            Component.empty()
                .append(killerDisplay)
                .append(Component.text(" has defeated the Ender Dragon!", NamedTextColor.GRAY));
        PlayerUtils.broadcastMessage(MessageFormatter.formatInfo("World", body));
      } else {
        PlayerUtils.broadcastMessage(
            MessageFormatter.formatInfo("World", "The Ender Dragon has been defeated!"));
      }
    } else if (entity instanceof Wither) {
      if (killerDisplay != null) {
        Component body =
            Component.empty()
                .append(killerDisplay)
                .append(Component.text(" has defeated the Wither!", NamedTextColor.GRAY));
        PlayerUtils.broadcastMessage(MessageFormatter.formatInfo("World", body));
      } else {
        PlayerUtils.broadcastMessage(
            MessageFormatter.formatInfo("World", "The Wither has been defeated!"));
      }
    } else if (entity instanceof ElderGuardian) {
      if (killerDisplay != null) {
        Component body =
            Component.empty()
                .append(killerDisplay)
                .append(Component.text(" has slain an Elder Guardian!", NamedTextColor.GRAY));
        PlayerUtils.broadcastMessage(MessageFormatter.formatInfo("World", body));
      } else {
        PlayerUtils.broadcastMessage(
            MessageFormatter.formatInfo("World", "An Elder Guardian has been slain!"));
      }
    } else if (entity instanceof Warden) {
      if (killerDisplay != null) {
        Component body =
            Component.empty()
                .append(killerDisplay)
                .append(Component.text(" has slain a Warden!", NamedTextColor.GRAY));
        PlayerUtils.broadcastMessage(MessageFormatter.formatInfo("World", body));
      } else {
        PlayerUtils.broadcastMessage(
            MessageFormatter.formatInfo("World", "A Warden has been slain!"));
      }
    }
  }
}
