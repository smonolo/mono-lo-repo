package dev.smnl.smessential.model;

import org.bukkit.Material;

public enum AlertTarget {
  ALL("All", Material.NETHER_STAR, "Sends alert to both Top Banner and Chat"),
  BANNER("Top Banner", Material.BEACON, "Displays persistent alert at the top of the screen"),
  CHAT("Chat", Material.PAPER, "Broadcasts message into the chat");

  private final String displayName;
  private final Material material;
  private final String description;

  AlertTarget(String displayName, Material material, String description) {
    this.displayName = displayName;
    this.material = material;
    this.description = description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Material getMaterial() {
    return material;
  }

  public String getDescription() {
    return description;
  }
}
