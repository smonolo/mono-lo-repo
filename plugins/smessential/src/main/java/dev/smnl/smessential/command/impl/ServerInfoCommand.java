package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.MessageFormatter.MessageType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ServerInfoCommand extends EssentialCommand {

  public ServerInfoCommand() {
    super(
        "ServerInfo",
        "Administration",
        "Displays server hardware, TPS, and runtime statistics",
        "smessential.command.administration",
        false,
        new String[] {"sinfo", "sysinfo", "tps"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(
        MessageFormatter.format(
            getToolName(), "Server & Performance Overview:", MessageType.INFO, false));

    double[] tps = Bukkit.getTPS();
    double mspt = Bukkit.getAverageTickTime();

    Component tps1 = formatTps(tps.length > 0 ? tps[0] : 20.0);
    Component tps5 = formatTps(tps.length > 1 ? tps[1] : 20.0);
    Component tps15 = formatTps(tps.length > 2 ? tps[2] : 20.0);
    Component msptComp = formatMspt(mspt);

    Component perfLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("TPS: ", NamedTextColor.GRAY))
            .append(tps1)
            .append(Component.text(", ", NamedTextColor.DARK_GRAY))
            .append(tps5)
            .append(Component.text(", ", NamedTextColor.DARK_GRAY))
            .append(tps15)
            .append(Component.text(" (1m, 5m, 15m)", NamedTextColor.DARK_GRAY))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("MSPT: ", NamedTextColor.GRAY))
            .append(msptComp);
    builder.append(Component.newline()).append(perfLine);

    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long usedMemory = totalMemory - freeMemory;

    long maxMb = maxMemory / (1024 * 1024);
    long usedMb = usedMemory / (1024 * 1024);
    long allocatedMb = totalMemory / (1024 * 1024);
    double usedPercent = (double) usedMemory / (double) maxMemory * 100.0;

    NamedTextColor memColor;
    if (usedPercent < 75.0) {
      memColor = NamedTextColor.GREEN;
    } else if (usedPercent < 90.0) {
      memColor = NamedTextColor.YELLOW;
    } else {
      memColor = NamedTextColor.RED;
    }

    Component memLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("Memory: ", NamedTextColor.GRAY))
            .append(
                Component.text(
                    String.format(
                        Locale.ROOT, "%d MB / %d MB (%.1f%%)", usedMb, maxMb, usedPercent),
                    memColor))
            .append(
                Component.text(
                    String.format(Locale.ROOT, " [Allocated: %d MB]", allocatedMb),
                    NamedTextColor.DARK_GRAY));
    builder.append(Component.newline()).append(memLine);

    long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
    String uptimeStr = formatUptime(uptimeMs);
    String javaVersion = System.getProperty("java.version", "Unknown");
    String mcVersion = Bukkit.getMinecraftVersion();

    Component sysLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("Software: ", NamedTextColor.GRAY))
            .append(Component.text("Paper " + mcVersion, NamedTextColor.WHITE))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Java: ", NamedTextColor.GRAY))
            .append(Component.text(javaVersion, NamedTextColor.WHITE))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Uptime: ", NamedTextColor.GRAY))
            .append(Component.text(uptimeStr, NamedTextColor.WHITE));
    builder.append(Component.newline()).append(sysLine);

    OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
    int availableProcessors = osMxBean.getAvailableProcessors();
    String osName = osMxBean.getName();
    String osArch = osMxBean.getArch();
    double cpuLoad = -1;
    if (osMxBean instanceof com.sun.management.OperatingSystemMXBean sunOs) {
      cpuLoad = sunOs.getCpuLoad() * 100.0;
    }

    int onlineCount = Bukkit.getOnlinePlayers().size();
    int maxPlayers = Bukkit.getMaxPlayers();

    Component hwLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("Players: ", NamedTextColor.GRAY))
            .append(
                Component.text(onlineCount + "/" + maxPlayers + " online", NamedTextColor.WHITE))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("System: ", NamedTextColor.GRAY))
            .append(Component.text(osName + " (" + osArch + ")", NamedTextColor.WHITE))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Cores: ", NamedTextColor.GRAY))
            .append(Component.text(availableProcessors, NamedTextColor.WHITE))
            .append(
                Component.text(
                    cpuLoad >= 0 ? String.format(Locale.ROOT, " (%.1f%% CPU)", cpuLoad) : "",
                    NamedTextColor.DARK_GRAY));
    builder.append(Component.newline()).append(hwLine);

    builder
        .append(Component.newline())
        .append(
            Component.text("- ", NamedTextColor.DARK_GRAY)
                .append(Component.text("Worlds:", NamedTextColor.GRAY)));

    for (World world : Bukkit.getWorlds()) {
      int chunkCount = world.getLoadedChunks().length;
      int entityCount = world.getEntityCount();
      int worldPlayers = world.getPlayerCount();
      String envName = formatEnvironment(world.getEnvironment());

      Component worldLine =
          Component.text("  • ", NamedTextColor.DARK_GRAY)
              .append(Component.text(world.getName(), NamedTextColor.WHITE))
              .append(Component.text(" (" + envName + "): ", NamedTextColor.DARK_GRAY))
              .append(Component.text("chunks: ", NamedTextColor.GRAY))
              .append(Component.text(chunkCount, NamedTextColor.WHITE))
              .append(Component.text(", ", NamedTextColor.DARK_GRAY))
              .append(Component.text("entities: ", NamedTextColor.GRAY))
              .append(Component.text(entityCount, NamedTextColor.WHITE))
              .append(Component.text(", ", NamedTextColor.DARK_GRAY))
              .append(Component.text("players: ", NamedTextColor.GRAY))
              .append(Component.text(worldPlayers, NamedTextColor.WHITE));
      builder.append(Component.newline()).append(worldLine);
    }

    builder.append(Component.newline());
    sender.sendMessage(builder.build());
  }

  private @NotNull Component formatTps(double tps) {
    double clampedTps = Math.min(20.0, Math.max(0.0, tps));
    NamedTextColor color;
    if (clampedTps >= 19.5) {
      color = NamedTextColor.GREEN;
    } else if (clampedTps >= 18.0) {
      color = NamedTextColor.YELLOW;
    } else {
      color = NamedTextColor.RED;
    }
    return Component.text(String.format(Locale.ROOT, "%.2f", clampedTps), color);
  }

  private @NotNull Component formatMspt(double mspt) {
    NamedTextColor color;
    if (mspt < 40.0) {
      color = NamedTextColor.GREEN;
    } else if (mspt < 50.0) {
      color = NamedTextColor.YELLOW;
    } else {
      color = NamedTextColor.RED;
    }
    return Component.text(String.format(Locale.ROOT, "%.1f ms", mspt), color);
  }

  private @NotNull String formatUptime(long uptimeMs) {
    long seconds = uptimeMs / 1000;
    long days = seconds / 86400;
    long hours = (seconds % 86400) / 3600;
    long minutes = (seconds % 3600) / 60;
    long secs = seconds % 60;

    StringBuilder sb = new StringBuilder();
    if (days > 0) {
      sb.append(days).append("d ");
    }
    if (hours > 0 || days > 0) {
      sb.append(hours).append("h ");
    }
    sb.append(minutes).append("m ").append(secs).append("s");
    return sb.toString();
  }

  private @NotNull String formatEnvironment(@NotNull World.Environment env) {
    return switch (env) {
      case NORMAL -> "Overworld";
      case NETHER -> "Nether";
      case THE_END -> "The End";
      case CUSTOM -> "Custom";
    };
  }
}
