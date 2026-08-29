package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ListCommand extends EssentialCommand {

  public ListCommand() {
    super(
        "List",
        "Administration",
        "Lists online players",
        "smessential.command.list",
        false,
        new String[] {"players", "online"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    List<Player> visiblePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

    int count = visiblePlayers.size();
    int max = Bukkit.getMaxPlayers();

    if (count == 0) {
      sender.sendMessage(
          MessageFormatter.formatInfo(
              getToolName(),
              Component.text(
                  "There are currently no players online (0/" + max + ").", NamedTextColor.GRAY)));
      return;
    }

    visiblePlayers.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));

    Component countComp =
        Component.text("Online (", NamedTextColor.GRAY)
            .append(Component.text(count, NamedTextColor.WHITE))
            .append(Component.text("/" + max + "): ", NamedTextColor.GRAY));

    Component playerList = Component.empty();
    for (int i = 0; i < visiblePlayers.size(); i++) {
      Player player = visiblePlayers.get(i);
      Component nameComp = PlayerUtils.getPlayerDisplayName(player);

      playerList = playerList.append(nameComp);
      if (i < visiblePlayers.size() - 1) {
        playerList = playerList.append(Component.text(", ", NamedTextColor.GRAY));
      }
    }

    Component fullMsg = countComp.append(playerList);
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), fullMsg));
  }
}
