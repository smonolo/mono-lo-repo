package dev.smnl.smessential.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ComponentUtils {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
  private static final PlainTextComponentSerializer PLAIN_SERIALIZER =
      PlainTextComponentSerializer.plainText();
  private static final LegacyComponentSerializer LEGACY_SERIALIZER =
      LegacyComponentSerializer.legacyAmpersand();

  private ComponentUtils() {}

  public static @NotNull Component parseLegacyOrMiniMessage(@Nullable String text) {
    if (text == null || text.isBlank()) {
      return Component.empty();
    }
    if (text.contains("&") || text.contains("§")) {
      String clean = text.replace('§', '&');
      return LEGACY_SERIALIZER.deserialize(clean);
    }
    try {
      return MINI_MESSAGE.deserialize(text);
    } catch (Throwable t) {
      return LEGACY_SERIALIZER.deserialize(text);
    }
  }

  public static @NotNull String toPlainText(@NotNull Component component) {
    return PLAIN_SERIALIZER.serialize(component);
  }

  public static @Nullable NamedTextColor extractLastNamedTextColor(@Nullable Component component) {
    if (component == null) {
      return null;
    }
    NamedTextColor color = null;
    if (component.color() instanceof NamedTextColor named) {
      color = named;
    }
    for (Component child : component.children()) {
      NamedTextColor childColor = extractLastNamedTextColor(child);
      if (childColor != null) {
        color = childColor;
      }
    }
    return color;
  }
}
