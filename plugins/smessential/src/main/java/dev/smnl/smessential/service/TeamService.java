package dev.smnl.smessential.service;

import dev.smnl.smessential.manager.SidebarManager;
import dev.smnl.smessential.model.Team;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeamService implements Listener {

  private static final long INVITE_EXPIRATION_MS = 60_000L;

  private final Map<UUID, Team> playerTeams = new ConcurrentHashMap<>();

  private final Map<UUID, Map<UUID, Long>> pendingInvites = new ConcurrentHashMap<>();

  private JavaPlugin plugin;
  private SidebarManager sidebarManager;
  private dev.smnl.smessential.gui.TeamInviteGUIManager inviteGUIManager;

  public TeamService() {}

  public void setInviteGUIManager(
      @Nullable dev.smnl.smessential.gui.TeamInviteGUIManager inviteGUIManager) {
    this.inviteGUIManager = inviteGUIManager;
  }

  public void setup(@NotNull JavaPlugin plugin, @NotNull SidebarManager sidebarManager) {
    this.plugin = plugin;
    this.sidebarManager = sidebarManager;
    Bukkit.getPluginManager().registerEvents(this, plugin);

    Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredInvites, 100L, 100L);
  }

  public @Nullable Team getTeam(@NotNull UUID playerUuid) {
    return playerTeams.get(playerUuid);
  }

  public boolean isInTeam(@NotNull UUID playerUuid) {
    return playerTeams.containsKey(playerUuid);
  }

  public void invite(@NotNull Player sender, @NotNull Player target) {
    if (sender.getUniqueId().equals(target.getUniqueId())) {
      sender.sendMessage(MessageFormatter.formatError("Team", "You cannot invite yourself."));
      return;
    }

    Team senderTeam = getTeam(sender.getUniqueId());
    Team targetTeam = getTeam(target.getUniqueId());

    if (senderTeam != null && senderTeam.contains(target.getUniqueId())) {
      sender.sendMessage(
          MessageFormatter.formatError(
              "Team",
              Component.empty()
                  .append(PlayerUtils.getGeneralDisplayName(target))
                  .append(Component.text(" is already in your team.", NamedTextColor.RED))));
      return;
    }

    if (targetTeam != null) {
      sender.sendMessage(
          MessageFormatter.formatError(
              "Team",
              Component.empty()
                  .append(PlayerUtils.getGeneralDisplayName(target))
                  .append(Component.text(" is already in another team.", NamedTextColor.RED))));
      return;
    }

    Map<UUID, Long> targetInvites =
        pendingInvites.computeIfAbsent(target.getUniqueId(), k -> new ConcurrentHashMap<>());
    Long existingExpiry = targetInvites.get(sender.getUniqueId());
    if (existingExpiry != null && existingExpiry > System.currentTimeMillis()) {
      sender.sendMessage(
          MessageFormatter.formatError(
              "Team",
              Component.empty()
                  .append(Component.text("You have already invited ", NamedTextColor.RED))
                  .append(PlayerUtils.getGeneralDisplayName(target))
                  .append(
                      Component.text(". Please wait for their response.", NamedTextColor.RED))));
      return;
    }

    targetInvites.put(sender.getUniqueId(), System.currentTimeMillis() + INVITE_EXPIRATION_MS);

    Component senderNotice =
        Component.text("Invited ", NamedTextColor.GRAY)
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" to join the team.", NamedTextColor.GRAY));
    sender.sendMessage(MessageFormatter.formatInfo("Team", senderNotice));

    Component acceptBtn =
        Component.text("[Accept]", NamedTextColor.GREEN, TextDecoration.BOLD)
            .clickEvent(ClickEvent.runCommand("/team accept " + sender.getName()))
            .hoverEvent(
                HoverEvent.showText(
                    Component.text("Click to accept team invite", NamedTextColor.GREEN)));

    Component declineBtn =
        Component.text("[Decline]", NamedTextColor.RED, TextDecoration.BOLD)
            .clickEvent(ClickEvent.runCommand("/team decline " + sender.getName()))
            .hoverEvent(
                HoverEvent.showText(
                    Component.text("Click to decline team invite", NamedTextColor.RED)));

    Component targetInvite =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(sender))
            .append(Component.text(" invited you to join their team! ", NamedTextColor.GRAY))
            .append(acceptBtn)
            .append(Component.text(" "))
            .append(declineBtn);

    target.sendMessage(MessageFormatter.formatInfo("Team", targetInvite));

    if (inviteGUIManager != null) {
      inviteGUIManager.openInviteGUI(target, sender);
    }
  }

  public void acceptInvite(@NotNull Player player, @Nullable String senderName) {
    if (isInTeam(player.getUniqueId())) {
      player.sendMessage(
          MessageFormatter.formatError(
              "Team", "You are already in a team. Leave your current team first."));
      return;
    }

    Map<UUID, Long> invites = pendingInvites.get(player.getUniqueId());
    if (invites == null || invites.isEmpty()) {
      player.sendMessage(
          MessageFormatter.formatError("Team", "You do not have any pending team invitations."));
      return;
    }

    UUID inviterUuid = null;
    long now = System.currentTimeMillis();

    if (senderName != null && !senderName.isBlank()) {
      Player specifiedSender = PlayerUtils.findOnlinePlayer(senderName, player);
      if (specifiedSender != null) {
        Long exp = invites.get(specifiedSender.getUniqueId());
        if (exp != null && exp > now) {
          inviterUuid = specifiedSender.getUniqueId();
        }
      }
    } else {

      long latestTime = 0;
      for (Map.Entry<UUID, Long> entry : invites.entrySet()) {
        if (entry.getValue() > now && entry.getValue() > latestTime) {
          latestTime = entry.getValue();
          inviterUuid = entry.getKey();
        }
      }
    }

    if (inviterUuid == null) {
      player.sendMessage(
          MessageFormatter.formatError(
              "Team", "No valid pending team invitation found or it has expired."));
      return;
    }

    Player inviter = Bukkit.getPlayer(inviterUuid);
    if (inviter == null || !inviter.isOnline()) {
      invites.remove(inviterUuid);
      player.sendMessage(
          MessageFormatter.formatError("Team", "The player who invited you is no longer online."));
      return;
    }

    invites.remove(inviterUuid);

    Team inviterTeam = getTeam(inviter.getUniqueId());
    if (inviterTeam != null) {
      inviterTeam.addMember(player.getUniqueId());
      playerTeams.put(player.getUniqueId(), inviterTeam);

      Component joinNotice =
          Component.empty()
              .append(PlayerUtils.getGeneralDisplayName(player))
              .append(Component.text(" joined the team.", NamedTextColor.GRAY));
      broadcastToTeam(inviterTeam, MessageFormatter.formatInfo("Team", joinNotice));
      updateTeamSidebars(inviterTeam);
    } else {
      Team newTeam = new Team(inviter.getUniqueId(), player.getUniqueId());
      playerTeams.put(inviter.getUniqueId(), newTeam);
      playerTeams.put(player.getUniqueId(), newTeam);

      Component formedWithPlayer =
          Component.text("Team formed with ", NamedTextColor.GRAY)
              .append(PlayerUtils.getGeneralDisplayName(player))
              .append(Component.text(".", NamedTextColor.GRAY));
      inviter.sendMessage(MessageFormatter.formatInfo("Team", formedWithPlayer));

      Component formedWithInviter =
          Component.text("Team formed with ", NamedTextColor.GRAY)
              .append(PlayerUtils.getGeneralDisplayName(inviter))
              .append(Component.text(".", NamedTextColor.GRAY));
      player.sendMessage(MessageFormatter.formatInfo("Team", formedWithInviter));

      updateTeamSidebars(newTeam);
    }
  }

  public void declineInvite(@NotNull Player player, @Nullable String senderName) {
    Map<UUID, Long> invites = pendingInvites.get(player.getUniqueId());
    if (invites == null || invites.isEmpty()) {
      player.sendMessage(
          MessageFormatter.formatError("Team", "You do not have any pending team invitations."));
      return;
    }

    UUID inviterUuid = null;
    long now = System.currentTimeMillis();

    if (senderName != null && !senderName.isBlank()) {
      Player specifiedSender = PlayerUtils.findOnlinePlayer(senderName, player);
      if (specifiedSender != null && invites.containsKey(specifiedSender.getUniqueId())) {
        inviterUuid = specifiedSender.getUniqueId();
      }
    } else {
      for (Map.Entry<UUID, Long> entry : invites.entrySet()) {
        if (entry.getValue() > now) {
          inviterUuid = entry.getKey();
          break;
        }
      }
    }

    if (inviterUuid == null) {
      player.sendMessage(MessageFormatter.formatError("Team", "No pending invitation to decline."));
      return;
    }

    invites.remove(inviterUuid);
    Player inviter = Bukkit.getPlayer(inviterUuid);

    Component declinedNotice =
        Component.text("Declined team invitation", NamedTextColor.GRAY)
            .append(
                inviter != null
                    ? Component.text(" from ", NamedTextColor.GRAY)
                        .append(PlayerUtils.getGeneralDisplayName(inviter))
                    : Component.empty())
            .append(Component.text(".", NamedTextColor.GRAY));
    player.sendMessage(MessageFormatter.formatInfo("Team", declinedNotice));

    if (inviter != null && inviter.isOnline()) {
      Component inviterNotice =
          Component.empty()
              .append(PlayerUtils.getGeneralDisplayName(player))
              .append(Component.text(" declined your team invitation.", NamedTextColor.GRAY));
      inviter.sendMessage(MessageFormatter.formatInfo("Team", inviterNotice));
    }
  }

  public void removeMember(@NotNull Player actor, @NotNull Player target) {
    Team team = getTeam(actor.getUniqueId());
    if (team == null) {
      actor.sendMessage(MessageFormatter.formatError("Team", "You are not in a team."));
      return;
    }

    if (!team.contains(target.getUniqueId())) {
      actor.sendMessage(
          MessageFormatter.formatError(
              "Team",
              Component.empty()
                  .append(PlayerUtils.getGeneralDisplayName(target))
                  .append(Component.text(" is not in your team.", NamedTextColor.RED))));
      return;
    }

    if (actor.getUniqueId().equals(target.getUniqueId())) {
      leaveTeam(actor);
      return;
    }

    team.removeMember(target.getUniqueId());
    playerTeams.remove(target.getUniqueId());

    Component targetMsg =
        Component.text("You were removed from the team by ", NamedTextColor.GRAY)
            .append(PlayerUtils.getGeneralDisplayName(actor))
            .append(Component.text(".", NamedTextColor.GRAY));
    target.sendMessage(MessageFormatter.formatInfo("Team", targetMsg));
    if (sidebarManager != null) {
      sidebarManager.updateSidebar(target);
    }

    Component teamNotice =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(target))
            .append(Component.text(" was removed from the team by ", NamedTextColor.GRAY))
            .append(PlayerUtils.getGeneralDisplayName(actor))
            .append(Component.text(".", NamedTextColor.GRAY));
    broadcastToTeam(team, MessageFormatter.formatInfo("Team", teamNotice));

    checkTeamDisband(team);
  }

  public void leaveTeam(@NotNull Player player) {
    Team team = getTeam(player.getUniqueId());
    if (team == null) {
      player.sendMessage(MessageFormatter.formatError("Team", "You are not in a team."));
      return;
    }

    team.removeMember(player.getUniqueId());
    playerTeams.remove(player.getUniqueId());

    player.sendMessage(MessageFormatter.formatInfo("Team", "You left the team."));
    if (sidebarManager != null) {
      sidebarManager.updateSidebar(player);
    }

    Component leaveNotice =
        Component.empty()
            .append(PlayerUtils.getGeneralDisplayName(player))
            .append(Component.text(" left the team.", NamedTextColor.GRAY));
    broadcastToTeam(team, MessageFormatter.formatInfo("Team", leaveNotice));

    checkTeamDisband(team);
  }

  private void checkTeamDisband(@NotNull Team team) {
    if (team.size() < 2) {

      for (UUID memberUuid : team.getMembers()) {
        playerTeams.remove(memberUuid);
        Player member = Bukkit.getPlayer(memberUuid);
        if (member != null && member.isOnline()) {
          member.sendMessage(
              MessageFormatter.formatInfo(
                  "Team", "The team has been disbanded as there are not enough players."));
          if (sidebarManager != null) {
            sidebarManager.updateSidebar(member);
          }
        }
      }
    } else {
      updateTeamSidebars(team);
    }
  }

  public void updateTeamSidebars(@NotNull Team team) {
    if (sidebarManager == null) return;
    for (UUID memberUuid : team.getMembers()) {
      Player member = Bukkit.getPlayer(memberUuid);
      if (member != null && member.isOnline()) {
        sidebarManager.updateSidebar(member);
      }
    }
  }

  public void broadcastToTeam(@NotNull Team team, @NotNull Component message) {
    for (UUID memberUuid : team.getMembers()) {
      Player member = Bukkit.getPlayer(memberUuid);
      if (member != null && member.isOnline()) {
        member.sendMessage(message);
      }
    }
  }

  public @NotNull List<String> getPendingInviterNames(@NotNull UUID targetUuid) {
    Map<UUID, Long> invites = pendingInvites.get(targetUuid);
    if (invites == null || invites.isEmpty()) {
      return List.of();
    }
    long now = System.currentTimeMillis();
    List<String> names = new ArrayList<>();
    for (Map.Entry<UUID, Long> entry : invites.entrySet()) {
      if (entry.getValue() > now) {
        Player inviter = Bukkit.getPlayer(entry.getKey());
        if (inviter != null && inviter.isOnline()) {
          names.add(inviter.getName());
        }
      }
    }
    return names;
  }

  private void cleanupExpiredInvites() {
    if (pendingInvites.isEmpty()) {
      return;
    }
    long now = System.currentTimeMillis();
    for (Map.Entry<UUID, Map<UUID, Long>> entry : pendingInvites.entrySet()) {
      entry.getValue().values().removeIf(expiry -> expiry <= now);
      if (entry.getValue().isEmpty()) {
        pendingInvites.remove(entry.getKey());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    pendingInvites.remove(uuid);
    for (Map<UUID, Long> map : pendingInvites.values()) {
      map.remove(uuid);
    }

    Team team = playerTeams.remove(uuid);
    if (team != null) {
      team.removeMember(uuid);
      Component dcNotice =
          Component.empty()
              .append(PlayerUtils.getGeneralDisplayName(player))
              .append(Component.text(" disconnected and left the team.", NamedTextColor.GRAY));
      broadcastToTeam(team, MessageFormatter.formatInfo("Team", dcNotice));
      checkTeamDisband(team);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      Team team = getTeam(player.getUniqueId());
      if (team != null && plugin != null) {

        Bukkit.getScheduler().runTask(plugin, () -> updateTeamSidebars(team));
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityRegainHealth(EntityRegainHealthEvent event) {
    if (event.getEntity() instanceof Player player) {
      Team team = getTeam(player.getUniqueId());
      if (team != null && plugin != null) {
        Bukkit.getScheduler().runTask(plugin, () -> updateTeamSidebars(team));
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    Team team = getTeam(player.getUniqueId());
    if (team != null && plugin != null) {
      Bukkit.getScheduler().runTask(plugin, () -> updateTeamSidebars(team));
    }
  }
}
