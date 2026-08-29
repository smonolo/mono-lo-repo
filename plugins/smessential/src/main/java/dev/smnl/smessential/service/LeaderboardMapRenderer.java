package dev.smnl.smessential.service;

import java.awt.image.BufferedImage;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

public class LeaderboardMapRenderer extends MapRenderer {

  private final String tileId;
  private final LeaderboardService leaderboardService;
  private volatile BufferedImage cachedImage;
  private volatile boolean dirty = true;
  private boolean hasRenderedToCanvas = false;

  public LeaderboardMapRenderer(
      @NotNull String tileId, @NotNull LeaderboardService leaderboardService) {
    super(false);
    this.tileId = tileId;
    this.leaderboardService = leaderboardService;
  }

  public void markDirty() {
    this.dirty = true;
    this.hasRenderedToCanvas = false;
  }

  public void updateImage(@NotNull BufferedImage image) {
    this.cachedImage = image;
    this.dirty = false;
    this.hasRenderedToCanvas = false;
  }

  @Override
  public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
    if (hasRenderedToCanvas && !dirty) {
      return;
    }

    if (dirty || cachedImage == null) {
      cachedImage = leaderboardService.getOrRenderTileImage(tileId);
      dirty = false;
    }

    if (cachedImage != null) {
      canvas.drawImage(0, 0, cachedImage);
      hasRenderedToCanvas = true;
    }
  }
}
