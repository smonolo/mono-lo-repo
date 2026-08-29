package dev.smnl.smessential.manager;

import dev.smnl.smessential.service.RankService;
import dev.smnl.smessential.util.ComponentUtils;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatManager implements Listener {

  private static final Pattern PLACEHOLDER_PATTERN =
      Pattern.compile("(?i)\\[(coords|pos|respawn|item|i)\\]");
  private static final Pattern MENTION_PATTERN = Pattern.compile("(?i)@([a-zA-Z0-9_.]{1,16})");

  private final JavaPlugin plugin;
  private final SidebarManager sidebarManager;
  private final RankService rankService;
  private final Set<UUID> reloadedPlayers = ConcurrentHashMap.newKeySet();

  public ChatManager(
      @NotNull JavaPlugin plugin,
      @NotNull SidebarManager sidebarManager,
      @Nullable RankService rankService) {
    this.plugin = plugin;
    this.sidebarManager = sidebarManager;
    this.rankService = rankService;
  }

  public ChatManager(@NotNull JavaPlugin plugin, @NotNull SidebarManager sidebarManager) {
    this(plugin, sidebarManager, null);
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
    if (rankService != null) {
      rankService.addPlayerUpdateListener(this::updatePlayerNameDisplays);
      rankService.addGlobalUpdateListener(this::updateAllNametags);
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      reloadedPlayers.add(player.getUniqueId());
      updatePlayerNameDisplays(player);
    }
  }

  public void updateAllNametags() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      updatePlayerNameDisplays(player);
    }
  }

  public void updatePlayerNameDisplays(Player player) {
    Component prefixComponent = PlayerUtils.getPlayerPrefixComponent(player);
    Component fullDisplayName = PlayerUtils.getPlayerDisplayName(player);
    Component customHeadName = Component.text(player.getName(), NamedTextColor.WHITE);

    player.displayName(fullDisplayName);
    player.playerListName(fullDisplayName);
    player.customName(customHeadName);
    player.setCustomNameVisible(true);

    String teamName = "hq_" + player.getName();
    if (teamName.length() > 16) {
      teamName = teamName.substring(0, 16);
    }

    NamedTextColor teamColor = ComponentUtils.extractLastNamedTextColor(prefixComponent);
    String activeEntry = player.getName();

    for (Player viewer : Bukkit.getOnlinePlayers()) {
      Scoreboard scoreboard = sidebarManager.getScoreboard(viewer);
      if (scoreboard == null) {
        continue;
      }

      Team team = scoreboard.getTeam(teamName);
      if (team == null) {
        team = scoreboard.registerNewTeam(teamName);
      }

      Component teamPrefix =
          prefixComponent.equals(Component.empty())
              ? Component.empty()
              : prefixComponent.append(Component.space());
      team.prefix(teamPrefix);
      team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
      team.color(teamColor != null ? teamColor : NamedTextColor.WHITE);

      for (String entry : new java.util.ArrayList<>(team.getEntries())) {
        if (!entry.equals(activeEntry)) {
          team.removeEntry(entry);
        }
      }

      if (!team.hasEntry(activeEntry)) {
        team.addEntry(activeEntry);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onAsyncChat(AsyncChatEvent event) {
    Player player = event.getPlayer();
    String plainMessage = ComponentUtils.toPlainText(event.message());

    Set<Player> mentionedPlayers = new HashSet<>();
    Component formattedMessage = formatChatMessage(player, plainMessage, mentionedPlayers);

    if (!mentionedPlayers.isEmpty()) {
      Bukkit.getScheduler()
          .runTask(
              plugin,
              () -> {
                for (Player mentioned : mentionedPlayers) {
                  if (mentioned.isOnline()) {
                    mentioned.playSound(
                        mentioned.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.4f);
                  }
                }
              });
    }

    Component prefixComponent = PlayerUtils.getPlayerPrefixComponent(player);
    Component nameComponent =
        prefixComponent.equals(Component.empty())
            ? PlayerUtils.getPlayerGroupColoredName(player, player.getName())
            : prefixComponent
                .append(Component.space())
                .append(PlayerUtils.getPlayerGroupColoredName(player, player.getName()));

    Component finalChatMessage =
        nameComponent.append(Component.text(": ", NamedTextColor.WHITE)).append(formattedMessage);

    event.renderer((source, sourceDisplayName, message, viewer) -> finalChatMessage);

    // Bedrock (Geyser/Floodgate) clients cannot parse custom prefixes and colors in player chat
    // packets (TextPacket.Type.CHAT).
    // Send rendered chat directly as system message (TextPacket.Type.RAW) to all Bedrock viewers
    // and remove them from event.viewers().
    Set<Audience> viewers = event.viewers();
    Set<Player> bedrockViewers = new HashSet<>();
    for (Audience viewer : viewers) {
      if (viewer instanceof Player p && PlayerUtils.isBedrockPlayer(p)) {
        bedrockViewers.add(p);
      }
    }

    if (!bedrockViewers.isEmpty()) {
      viewers.removeAll(bedrockViewers);
      for (Player bedrockViewer : bedrockViewers) {
        bedrockViewer.sendMessage(finalChatMessage);
      }
    }
  }

  private @NotNull Component createLocationComponent(@NotNull Location loc) {
    int x = loc.getBlockX();
    int y = loc.getBlockY();
    int z = loc.getBlockZ();
    String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "world";
    String coordStr = x + ", " + y + ", " + z;

    return Component.text("(" + coordStr + ")", NamedTextColor.WHITE)
        .hoverEvent(
            HoverEvent.showText(
                Component.text("World: ", NamedTextColor.GRAY)
                    .append(Component.text(worldName, NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Click to copy coordinates", NamedTextColor.GRAY))))
        .clickEvent(ClickEvent.copyToClipboard(coordStr));
  }

  private @NotNull Component createItemComponent(@NotNull Player player) {
    ItemStack hand = player.getInventory().getItemInMainHand();
    if (hand.getType() == Material.AIR) {
      return Component.text("[Air]", NamedTextColor.GRAY);
    }

    ItemMeta meta = hand.getItemMeta();
    Component nameComp =
        (meta != null && meta.hasDisplayName() && meta.displayName() != null)
            ? meta.displayName()
            : Component.translatable(hand.translationKey());

    int amount = hand.getAmount();
    Component amountComp =
        amount > 1 ? Component.text(" x" + amount, NamedTextColor.GRAY) : Component.empty();

    return Component.text("[", NamedTextColor.GOLD)
        .append(nameComp.colorIfAbsent(NamedTextColor.WHITE))
        .append(amountComp)
        .append(Component.text("]", NamedTextColor.GOLD))
        .hoverEvent(hand.asHoverEvent());
  }

  private @NotNull Component formatChatMessage(
      @NotNull Player player, @NotNull String plainMessage, @NotNull Set<Player> mentionedPlayers) {
    Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher(plainMessage);

    Component currentCoords = createLocationComponent(player.getLocation());
    Location respawnLoc = player.getRespawnLocation();
    if (respawnLoc == null) {
      respawnLoc = player.getWorld().getSpawnLocation();
    }
    Component respawnCoords = createLocationComponent(respawnLoc);
    Component itemComp = createItemComponent(player);

    Component builder = Component.empty();
    int lastEnd = 0;

    while (placeholderMatcher.find()) {
      if (placeholderMatcher.start() > lastEnd) {
        String sub = plainMessage.substring(lastEnd, placeholderMatcher.start());
        builder = builder.append(processMentions(sub, mentionedPlayers));
      }

      String tag = placeholderMatcher.group(1);
      if (tag.equalsIgnoreCase("respawn")) {
        builder = builder.append(respawnCoords);
      } else if (tag.equalsIgnoreCase("coords") || tag.equalsIgnoreCase("pos")) {
        builder = builder.append(currentCoords);
      } else if (tag.equalsIgnoreCase("item") || tag.equalsIgnoreCase("i")) {
        builder = builder.append(itemComp);
      } else {
        builder = builder.append(Component.text(placeholderMatcher.group(0), NamedTextColor.WHITE));
      }
      lastEnd = placeholderMatcher.end();
    }

    if (lastEnd < plainMessage.length()) {
      String sub = plainMessage.substring(lastEnd);
      builder = builder.append(processMentions(sub, mentionedPlayers));
    }

    return builder;
  }

  private @NotNull Component processMentions(
      @NotNull String text, @NotNull Set<Player> mentionedPlayers) {
    Matcher mentionMatcher = MENTION_PATTERN.matcher(text);
    if (!mentionMatcher.find()) {
      return Component.text(text, NamedTextColor.WHITE);
    }

    Component builder = Component.empty();
    int lastEnd = 0;
    mentionMatcher.reset();

    while (mentionMatcher.find()) {
      if (mentionMatcher.start() > lastEnd) {
        builder =
            builder.append(
                Component.text(
                    text.substring(lastEnd, mentionMatcher.start()), NamedTextColor.WHITE));
      }

      String targetName = mentionMatcher.group(1);
      Player target = PlayerUtils.findOnlinePlayer(targetName);

      if (target != null) {
        mentionedPlayers.add(target);
        Component mentionComponent =
            Component.text("@" + target.getName(), NamedTextColor.GOLD)
                .hoverEvent(
                    HoverEvent.showText(
                        Component.text("Mentioned ", NamedTextColor.GRAY)
                            .append(PlayerUtils.getPlayerDisplayName(target))));
        builder = builder.append(mentionComponent);
      } else {
        builder = builder.append(Component.text(mentionMatcher.group(0), NamedTextColor.WHITE));
      }

      lastEnd = mentionMatcher.end();
    }

    if (lastEnd < text.length()) {
      builder = builder.append(Component.text(text.substring(lastEnd), NamedTextColor.WHITE));
    }

    return builder;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    updateAllNametags();

    if (reloadedPlayers.remove(player.getUniqueId())) {
      event.joinMessage(null);
      return;
    }

    boolean isFirstJoin = !player.hasPlayedBefore();
    if (isFirstJoin) {
      event.joinMessage(
          MessageFormatter.formatInfo(
              "World",
              Component.empty()
                  .append(PlayerUtils.getPlayerDisplayName(player))
                  .append(Component.text(" joined for the first time.", NamedTextColor.GRAY))));
    } else {
      event.joinMessage(
          MessageFormatter.formatInfo(
              "World",
              Component.empty()
                  .append(PlayerUtils.getPlayerDisplayName(player))
                  .append(Component.text(" has joined.", NamedTextColor.GRAY))));
    }

    Component welcomeBuilder =
        Component.empty()
            .append(Component.newline())
            .append(Component.text("Headquarters", NamedTextColor.BLUE, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.newline())
            .append(
                Component.text(
                    "Welcome! If you need immediate support, use /s. For a list of available commands, use /h.",
                    NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.text("mc.smnl.dev", NamedTextColor.GOLD))
            .append(Component.newline());

    player.sendMessage(welcomeBuilder);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    reloadedPlayers.remove(player.getUniqueId());

    String teamName = "hq_" + player.getName();
    if (teamName.length() > 16) {
      teamName = teamName.substring(0, 16);
    }
    for (Player viewer : Bukkit.getOnlinePlayers()) {
      Scoreboard scoreboard = sidebarManager.getScoreboard(viewer);
      if (scoreboard != null) {
        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
          team.unregister();
        }
      }
    }

    event.quitMessage(
        MessageFormatter.formatInfo(
            "World",
            Component.empty()
                .append(PlayerUtils.getPlayerDisplayName(player))
                .append(Component.text(" has left.", NamedTextColor.GRAY))));
  }
}
