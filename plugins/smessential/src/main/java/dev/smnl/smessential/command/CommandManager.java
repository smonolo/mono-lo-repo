package dev.smnl.smessential.command;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class CommandManager {

  private final JavaPlugin plugin;
  private final List<EssentialCommand> registeredCommands = new ArrayList<>();

  public CommandManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void register(@NotNull EssentialCommand... commands) {
    Collections.addAll(registeredCommands, commands);
    plugin
        .getLifecycleManager()
        .registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> {
              for (EssentialCommand cmd : commands) {
                List<String> aliases = Arrays.asList(cmd.getAliases());
                event
                    .registrar()
                    .register(
                        plugin.getPluginMeta(),
                        cmd.getName().toLowerCase(),
                        cmd.getDescription(),
                        aliases,
                        cmd);
              }
            });
  }

  public List<EssentialCommand> getRegisteredCommands() {
    return Collections.unmodifiableList(registeredCommands);
  }
}
