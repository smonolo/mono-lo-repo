package dev.smnl.smessential.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Rank implements Comparable<Rank> {

  private final String id;
  private String name;
  private String color;
  private String prefix;
  private int weight;
  private boolean isDefault;
  private boolean isPrimary;
  private final Set<String> permissions = ConcurrentHashMap.newKeySet();
  private final Set<String> parents = ConcurrentHashMap.newKeySet();

  private TextColor cachedTextColor;
  private Component cachedPrefixComponent;
  private Component cachedDisplayNameComponent;

  public Rank(
      @NotNull String id,
      @NotNull String name,
      @NotNull String color,
      @NotNull String prefix,
      int weight,
      boolean isDefault,
      boolean isPrimary,
      @Nullable Set<String> permissions) {
    this.id = id.toLowerCase();
    this.name = name;
    this.color = color;
    this.prefix = prefix;
    this.weight = weight;
    this.isDefault = isDefault;
    this.isPrimary = isPrimary;
    if (permissions != null) {
      this.permissions.addAll(permissions);
    }
    recalculateVisualCache();
  }

  public Rank(
      @NotNull String id,
      @NotNull String name,
      @NotNull String color,
      @NotNull String prefix,
      int weight,
      boolean isDefault,
      @Nullable Set<String> permissions) {
    this(id, name, color, prefix, weight, isDefault, true, permissions);
  }

  private void recalculateVisualCache() {
    this.cachedTextColor = parseTextColor(this.color);
    this.cachedPrefixComponent = parsePrefixComponent(this.prefix, this.cachedTextColor);
    this.cachedDisplayNameComponent = Component.text(this.name, this.cachedTextColor);
  }

  public @NotNull String getId() {
    return id;
  }

  public @NotNull String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
    recalculateVisualCache();
  }

  public @NotNull String getColor() {
    return color;
  }

  public void setColor(@NotNull String color) {
    this.color = color;
    recalculateVisualCache();
  }

  public @NotNull String getPrefix() {
    return prefix;
  }

  public void setPrefix(@NotNull String prefix) {
    this.prefix = prefix;
    recalculateVisualCache();
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public boolean isPrimary() {
    return isPrimary;
  }

  public void setPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
    if (!isPrimary) {
      parents.clear();
    }
  }

  public boolean isSecondary() {
    return !isPrimary;
  }

  public @NotNull Set<String> getParents() {
    return isPrimary ? Collections.unmodifiableSet(parents) : Collections.emptySet();
  }

  public void addParent(@NotNull String parentRankId) {
    if (isPrimary) {
      parents.add(parentRankId.toLowerCase());
    }
  }

  public boolean removeParent(@NotNull String parentRankId) {
    return parents.remove(parentRankId.toLowerCase());
  }

  public void setParents(@NotNull Set<String> newParents) {
    parents.clear();
    if (isPrimary) {
      for (String p : newParents) {
        parents.add(p.toLowerCase());
      }
    }
  }

  public boolean hasParent(@NotNull String parentRankId) {
    return isPrimary && parents.contains(parentRankId.toLowerCase());
  }

  public @NotNull Set<String> getPermissions() {
    return Collections.unmodifiableSet(permissions);
  }

  public void addPermission(@NotNull String permission) {
    permissions.add(permission.toLowerCase());
  }

  public boolean removePermission(@NotNull String permission) {
    return permissions.remove(permission.toLowerCase());
  }

  public void setPermissions(@NotNull Set<String> newPermissions) {
    permissions.clear();
    for (String perm : newPermissions) {
      permissions.add(perm.toLowerCase());
    }
  }

  public boolean hasPermissionDirectly(@NotNull String permission) {
    String lower = permission.toLowerCase();
    if (permissions.contains("-" + lower)) {
      return false;
    }
    if (permissions.contains("*") || permissions.contains("'*'") || permissions.contains("\"*\"")) {
      return true;
    }
    if (permissions.contains(lower)) {
      return true;
    }

    for (String perm : permissions) {
      if (perm.endsWith(".*")) {
        String base = perm.substring(0, perm.length() - 2);
        if (lower.startsWith(base + ".") || lower.equalsIgnoreCase(base)) {
          return true;
        }
      }
    }
    return false;
  }

  private static @NotNull TextColor parseTextColor(@Nullable String rawColor) {
    if (rawColor == null || rawColor.isBlank()) {
      return NamedTextColor.WHITE;
    }
    String normalized = rawColor.trim().toLowerCase().replace("-", "_").replace(" ", "_");
    if (normalized.startsWith("<") && normalized.endsWith(">")) {
      normalized = normalized.substring(1, normalized.length() - 1);
    }
    NamedTextColor named = NamedTextColor.NAMES.value(normalized);
    if (named != null) {
      return named;
    }
    if (normalized.startsWith("#")) {
      TextColor hex = TextColor.fromHexString(normalized);
      if (hex != null) return hex;
    }
    return NamedTextColor.WHITE;
  }

  private static @NotNull Component parsePrefixComponent(
      @Nullable String rawPrefix, @NotNull TextColor color) {
    if (rawPrefix == null || rawPrefix.trim().isBlank()) {
      return Component.empty();
    }
    String cleanPrefix = rawPrefix.trim();
    if (cleanPrefix.startsWith("<") && cleanPrefix.contains(">")) {
      cleanPrefix = cleanPrefix.replaceAll("<[^>]*>", "").trim();
    }
    if (cleanPrefix.isEmpty()) {
      return Component.empty();
    }
    return Component.text(cleanPrefix, color);
  }

  public @NotNull TextColor getTextColor() {
    return cachedTextColor != null ? cachedTextColor : NamedTextColor.WHITE;
  }

  public @NotNull Component getPrefixComponent() {
    return cachedPrefixComponent != null ? cachedPrefixComponent : Component.empty();
  }

  public @NotNull Component getRankDisplayNameComponent() {
    return cachedDisplayNameComponent != null
        ? cachedDisplayNameComponent
        : Component.text(name, NamedTextColor.WHITE);
  }

  public @NotNull Component formatPlayerColoredName(@NotNull String playerName) {
    return Component.text(playerName, getTextColor());
  }

  public @NotNull Component formatPlayerDisplayName(@NotNull String playerName) {
    Component prefixComp = getPrefixComponent();
    if (prefixComp.equals(Component.empty())) {
      return formatPlayerColoredName(playerName);
    }
    return prefixComp.append(Component.space()).append(formatPlayerColoredName(playerName));
  }

  @Override
  public int compareTo(@NotNull Rank other) {

    int cmp = Integer.compare(other.weight, this.weight);
    if (cmp != 0) return cmp;
    return this.id.compareToIgnoreCase(other.id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Rank rank)) return false;
    return id.equalsIgnoreCase(rank.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id.toLowerCase());
  }
}
