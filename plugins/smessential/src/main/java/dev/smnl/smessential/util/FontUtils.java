package dev.smnl.smessential.util;

import org.jetbrains.annotations.NotNull;

public final class FontUtils {

  private static final int DEFAULT_SPACE_WIDTH = 4;

  private FontUtils() {}

  public static int getPixelWidth(@NotNull String s) {
    return getPixelWidth(s, false);
  }

  public static int getPixelWidth(@NotNull String s, boolean bold) {
    int width = 0;
    for (char c : s.toCharArray()) {
      int charWidth;
      switch (c) {
        case 'i', '.', ',', '!', ';', '|' -> charWidth = 2;
        case 'l', '\'', '`' -> charWidth = 3;
        case ' ', 'I', '[', ']', 't' -> charWidth = 4;
        case 'f', 'k', '"', '<', '>', '*' -> charWidth = 5;
        case '@', '~' -> charWidth = 7;
        default -> charWidth = 6;
      }
      if (bold && c != ' ') {
        charWidth += 1;
      }
      width += charWidth;
    }
    return width;
  }

  public static @NotNull String centerLine(@NotNull String text, @NotNull String referenceLine) {
    return centerLine(text, getPixelWidth(referenceLine));
  }

  public static @NotNull String centerLine(@NotNull String text, int targetPixelWidth) {
    int textWidth = getPixelWidth(text);
    int paddingPixels = (targetPixelWidth - textWidth) / 2;
    if (paddingPixels <= 0) {
      return text;
    }

    int spaceCount = Math.round((float) paddingPixels / DEFAULT_SPACE_WIDTH);
    return " ".repeat(spaceCount) + text;
  }

  public static @NotNull String centerAndPadLine(@NotNull String text, int targetPixelWidth) {
    int textWidth = getPixelWidth(text);
    int paddingPixels = (targetPixelWidth - textWidth) / 2;
    if (paddingPixels <= 0) {
      return text;
    }

    int spaceCount = Math.max(1, Math.round((float) paddingPixels / DEFAULT_SPACE_WIDTH));
    return " ".repeat(spaceCount) + text + " ".repeat(spaceCount);
  }

  public static @NotNull String findLongestLine(@NotNull String... lines) {
    String longest = "";
    int maxWidth = -1;
    for (String line : lines) {
      if (line == null || line.isEmpty()) {
        continue;
      }
      int width = getPixelWidth(line);
      if (width > maxWidth) {
        maxWidth = width;
        longest = line;
      }
    }
    return longest;
  }

  public static int getMaxPixelWidth(int initialWidth, @NotNull String... lines) {
    int max = initialWidth;
    for (String line : lines) {
      if (line == null || line.isEmpty()) {
        continue;
      }
      int width = getPixelWidth(line);
      if (width > max) {
        max = width;
      }
    }
    return max;
  }

  public static @NotNull String formatEnumTitleCase(@NotNull String enumName) {
    String[] words = enumName.toLowerCase().split("_");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      if (!words[i].isEmpty()) {
        sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
        if (i < words.length - 1) {
          sb.append(" ");
        }
      }
    }
    return sb.toString();
  }
}
