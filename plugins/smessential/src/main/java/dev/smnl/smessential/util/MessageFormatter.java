package dev.smnl.smessential.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public final class MessageFormatter {

  public enum MessageType {
    INFO(NamedTextColor.GOLD, NamedTextColor.GRAY),
    ERROR(NamedTextColor.DARK_RED, NamedTextColor.RED);

    private final NamedTextColor tagColor;
    private final NamedTextColor textColor;

    MessageType(NamedTextColor tagColor, NamedTextColor textColor) {
      this.tagColor = tagColor;
      this.textColor = textColor;
    }

    public NamedTextColor getTagColor() {
      return tagColor;
    }

    public NamedTextColor getTextColor() {
      return textColor;
    }
  }

  private MessageFormatter() {}

  public static @NotNull Component format(
      @NotNull String toolName, @NotNull String message, @NotNull MessageType type) {
    return format(toolName, message, type, false);
  }

  public static @NotNull Component format(
      @NotNull String toolName,
      @NotNull String message,
      @NotNull MessageType type,
      boolean withNewlines) {
    Component content =
        Component.text("[HQ] ", NamedTextColor.BLUE)
            .append(Component.text("(" + toolName + ") ", type.getTagColor()))
            .append(Component.text(message, type.getTextColor()));

    if (withNewlines) {
      return Component.empty()
          .append(Component.newline())
          .append(content)
          .append(Component.newline());
    }

    return content;
  }

  public static @NotNull Component format(
      @NotNull String toolName, @NotNull Component message, @NotNull MessageType type) {
    return Component.text("[HQ] ", NamedTextColor.BLUE)
        .append(Component.text("(" + toolName + ") ", type.getTagColor()))
        .append(message);
  }

  public static @NotNull Component formatInfo(@NotNull String toolName, @NotNull String message) {
    return format(toolName, message, MessageType.INFO, false);
  }

  public static @NotNull Component formatInfo(
      @NotNull String toolName, @NotNull Component message) {
    return format(toolName, message, MessageType.INFO);
  }

  public static @NotNull Component formatError(@NotNull String toolName, @NotNull String message) {
    return format(toolName, message, MessageType.ERROR, false);
  }

  public static @NotNull Component formatError(
      @NotNull String toolName, @NotNull Component message) {
    return format(toolName, message, MessageType.ERROR);
  }

  public static @NotNull Component formatNoPermission(@NotNull String toolName) {
    return formatError(toolName, "You do not have permission to execute this command.");
  }

  public static @NotNull Component formatCustom(
      @NotNull String toolName,
      @NotNull String message,
      @NotNull NamedTextColor tagColor,
      @NotNull NamedTextColor textColor) {
    return Component.text("[HQ] ", NamedTextColor.BLUE)
        .append(Component.text("(" + toolName + ") ", tagColor))
        .append(Component.text(message, textColor));
  }
}
