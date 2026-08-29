package dev.smnl.smessential.gui;

import dev.smnl.smessential.service.TeamService;
import dev.smnl.smessential.util.ItemBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamInviteGUIManager {

  private final TeamService teamService;

  public TeamInviteGUIManager(@NotNull TeamService teamService) {
    this.teamService = teamService;
  }

  public void openInviteGUI(@NotNull Player target, @NotNull Player inviter) {
    GUIWindow gui = new GUIWindow("Team Invite", 27);

    String inviterName = inviter.getName();

    // Slot 4: Inviter Skull
    ItemBuilder headItem =
        ItemBuilder.of(Material.PLAYER_HEAD)
            .skullOwner(inviter)
            .name(inviterName, NamedTextColor.GOLD)
            .lore("Invited you to join their team.");
    gui.setItem(4, headItem.build(), null);

    // Slot 11: Accept
    gui.setItem(
        11,
        ItemBuilder.of(Material.LIME_CONCRETE)
            .name("Accept", NamedTextColor.GREEN)
            .lore("Click to accept the team invitation")
            .build(),
        event -> {
          target.closeInventory();
          teamService.acceptInvite(target, inviter.getName());
        });

    // Slot 15: Decline
    gui.setItem(
        15,
        ItemBuilder.of(Material.RED_CONCRETE)
            .name("Decline", NamedTextColor.RED)
            .lore("Click to decline the team invitation")
            .build(),
        event -> {
          target.closeInventory();
          teamService.declineInvite(target, inviter.getName());
        });

    // Slot 26: Close button
    gui.setCloseButton();

    gui.open(target);
  }
}
