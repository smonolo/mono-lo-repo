package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.database.DatabaseManager.DatabaseInfo;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.MessageFormatter.MessageType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DatabaseInfoCommand extends EssentialCommand {

  private final DatabaseManager databaseManager;

  public DatabaseInfoCommand(@NotNull DatabaseManager databaseManager) {
    super(
        "DatabaseInfo",
        "Administration",
        "Displays database connection and pool metrics",
        "smessential.command.administration",
        false,
        new String[] {"dbinfo", "database", "db"});
    this.databaseManager = databaseManager;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    DatabaseInfo info = databaseManager.getDatabaseInfo();

    TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(
        MessageFormatter.format(
            getToolName(), "Database & Connection Overview:", MessageType.INFO, false));

    Component statusComp =
        info.connected()
            ? Component.text("Connected", NamedTextColor.GREEN)
            : Component.text("Disconnected", NamedTextColor.RED);

    Component statusLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("Status: ", NamedTextColor.GRAY))
            .append(statusComp)
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Engine: ", NamedTextColor.GRAY))
            .append(Component.text("PostgreSQL", NamedTextColor.WHITE));
    builder.append(Component.newline()).append(statusLine);

    Component endpointLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("Endpoint: ", NamedTextColor.GRAY))
            .append(
                Component.text(
                    info.host() + ":" + info.port() + "/" + info.database(), NamedTextColor.WHITE))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("Schema: ", NamedTextColor.GRAY))
            .append(Component.text(info.schema(), NamedTextColor.GOLD));
    builder.append(Component.newline()).append(endpointLine);

    Component userLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("User: ", NamedTextColor.GRAY))
            .append(Component.text(info.username(), NamedTextColor.WHITE))
            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
            .append(Component.text("SSL: ", NamedTextColor.GRAY))
            .append(
                Component.text(
                    info.ssl() ? "Enabled" : "Disabled",
                    info.ssl() ? NamedTextColor.GREEN : NamedTextColor.GRAY));
    builder.append(Component.newline()).append(userLine);

    Component poolLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("Pool (HikariCP): ", NamedTextColor.GRAY))
            .append(Component.text(info.activeConnections(), NamedTextColor.WHITE))
            .append(Component.text(" active, ", NamedTextColor.GRAY))
            .append(Component.text(info.idleConnections(), NamedTextColor.WHITE))
            .append(Component.text(" idle, ", NamedTextColor.GRAY))
            .append(
                Component.text(
                    info.totalConnections() + "/" + info.maxConnections(), NamedTextColor.WHITE))
            .append(Component.text(" total", NamedTextColor.GRAY))
            .append(
                Component.text(
                    String.format(Locale.ROOT, " [Awaiting: %d]", info.threadsAwaitingConnection()),
                    NamedTextColor.DARK_GRAY));
    builder.append(Component.newline()).append(poolLine);

    NamedTextColor pingColor;
    if (!info.connected() || info.pingMs() < 0) {
      pingColor = NamedTextColor.RED;
    } else if (info.pingMs() < 25) {
      pingColor = NamedTextColor.GREEN;
    } else if (info.pingMs() < 100) {
      pingColor = NamedTextColor.YELLOW;
    } else {
      pingColor = NamedTextColor.RED;
    }

    String pingText = info.pingMs() >= 0 ? info.pingMs() + " ms" : "N/A";
    Component pingLine =
        Component.text("- ", NamedTextColor.DARK_GRAY)
            .append(Component.text("Latency: ", NamedTextColor.GRAY))
            .append(Component.text(pingText, pingColor));
    builder.append(Component.newline()).append(pingLine);

    builder.append(Component.newline());
    sender.sendMessage(builder.build());
  }
}
