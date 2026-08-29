package dev.smnl.smessential.permission;

import dev.smnl.smessential.service.RankService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class RankPermissible extends PermissibleBase {

  private final Player player;
  private RankService rankService;
  private Permissible oldPermissible;

  public RankPermissible(@NotNull Player player, @NotNull RankService rankService) {
    super(player);
    this.player = player;
    this.rankService = rankService;
  }

  public void setRankService(@NotNull RankService rankService) {
    this.rankService = rankService;
  }

  @Override
  public boolean isOp() {
    Set<String> perms = rankService.getEffectivePermissionsForPlayer(player.getUniqueId());
    return perms.contains("*") || perms.contains("'*'") || perms.contains("\"*\"");
  }

  @Override
  public boolean isPermissionSet(@NotNull String name) {
    if (name == null || name.isBlank()) return false;
    String lower = name.trim().toLowerCase(Locale.ROOT);
    if (rankService.hasPermission(player, lower)) {
      return true;
    }
    Set<String> perms = rankService.getEffectivePermissionsForPlayer(player.getUniqueId());
    if (perms.contains("-" + lower)) {
      return true;
    }
    for (String perm : perms) {
      String p = perm.trim().toLowerCase(Locale.ROOT);
      if (p.startsWith("-")) {
        String neg = p.substring(1).trim();
        if (neg.equals("*") || neg.equals("'*'") || neg.equals("\"*\"")) {
          return true;
        }
        if (neg.endsWith(".*")) {
          String base = neg.substring(0, neg.length() - 2);
          if (lower.startsWith(base + ".") || lower.equalsIgnoreCase(base)) {
            return true;
          }
        } else if (neg.endsWith("*")) {
          String base = neg.substring(0, neg.length() - 1);
          if (lower.startsWith(base)) {
            return true;
          }
        }
      }
    }
    return super.isPermissionSet(name);
  }

  @Override
  public boolean isPermissionSet(@NotNull Permission perm) {
    if (perm == null) return false;
    return isPermissionSet(perm.getName());
  }

  @Override
  public boolean hasPermission(@NotNull String name) {
    if (name == null || name.isBlank()) return false;
    String lower = name.trim().toLowerCase(Locale.ROOT);
    Set<String> perms = rankService.getEffectivePermissionsForPlayer(player.getUniqueId());

    if (perms.contains("-" + lower)) {
      return false;
    }

    for (String perm : perms) {
      String p = perm.trim().toLowerCase(Locale.ROOT);
      if (p.startsWith("-")) {
        String neg = p.substring(1).trim();
        if (neg.equals("*") || neg.equals("'*'") || neg.equals("\"*\"")) {
          return false;
        }
        if (neg.endsWith(".*")) {
          String base = neg.substring(0, neg.length() - 2);
          if (lower.startsWith(base + ".") || lower.equalsIgnoreCase(base)) {
            return false;
          }
        } else if (neg.endsWith("*")) {
          String base = neg.substring(0, neg.length() - 1);
          if (lower.startsWith(base)) {
            return false;
          }
        }
      }
    }

    if (rankService.hasPermission(player, lower)) {
      return true;
    }

    if (lower.startsWith("smessential.")) {
      return false;
    }

    if (super.isPermissionSet(name)) {
      return super.hasPermission(name);
    }

    Permission perm = Bukkit.getPluginManager().getPermission(name);
    if (perm != null) {
      if (perm.getDefault() == PermissionDefault.TRUE) {
        return true;
      }
      if (perm.getDefault() == PermissionDefault.NOT_OP && !isOp()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean hasPermission(@NotNull Permission perm) {
    if (perm == null) return false;
    return hasPermission(perm.getName());
  }

  @Override
  public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
    Map<String, PermissionAttachmentInfo> map = new HashMap<>();
    for (PermissionAttachmentInfo pai : super.getEffectivePermissions()) {
      map.put(pai.getPermission().toLowerCase(Locale.ROOT), pai);
    }

    Set<String> perms = rankService.getEffectivePermissionsForPlayer(player.getUniqueId());
    for (String perm : perms) {
      String lower = perm.trim().toLowerCase(Locale.ROOT);
      boolean value = !lower.startsWith("-");
      String clean = value ? lower : lower.substring(1).trim();
      map.put(clean, new PermissionAttachmentInfo(this, clean, null, value));
    }
    return Collections.unmodifiableSet(new HashSet<>(map.values()));
  }

  private static sun.misc.Unsafe getUnsafe() {
    try {
      Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      return (sun.misc.Unsafe) f.get(null);
    } catch (Throwable t) {
      return null;
    }
  }

  private static Field findPermField(Class<?> startClass) {
    Class<?> clazz = startClass;
    while (clazz != null && clazz != Object.class) {
      try {
        Field f = clazz.getDeclaredField("perm");
        return f;
      } catch (NoSuchFieldException ignored) {
      }
      for (Field f : clazz.getDeclaredFields()) {
        if (PermissibleBase.class.isAssignableFrom(f.getType())) {
          return f;
        }
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }

  private static Object getFieldValue(Object target, Field field) {
    try {
      field.setAccessible(true);
      return field.get(target);
    } catch (Throwable t) {
      try {
        sun.misc.Unsafe unsafe = getUnsafe();
        if (unsafe != null) {
          long offset = unsafe.objectFieldOffset(field);
          return unsafe.getObject(target, offset);
        }
      } catch (Throwable ignored) {
      }
    }
    return null;
  }

  private static void setFieldValue(Object target, Field field, Object value) {
    try {
      field.setAccessible(true);
      field.set(target, value);
    } catch (Throwable t) {
      try {
        sun.misc.Unsafe unsafe = getUnsafe();
        if (unsafe != null) {
          long offset = unsafe.objectFieldOffset(field);
          unsafe.putObject(target, offset, value);
        }
      } catch (Throwable ignored) {
      }
    }
  }

  public static void inject(@NotNull Player player, @NotNull RankService rankService) {
    try {
      Field permField = findPermField(player.getClass());
      if (permField == null) {
        return;
      }
      Object currentObj = getFieldValue(player, permField);
      if (currentObj instanceof RankPermissible rp) {
        rp.setRankService(rankService);
        return;
      }
      Permissible current = currentObj instanceof Permissible p ? p : null;
      RankPermissible rankPermissible = new RankPermissible(player, rankService);
      rankPermissible.oldPermissible = current;

      if (current instanceof PermissibleBase pb) {
        try {
          Field attachmentsField = PermissibleBase.class.getDeclaredField("attachments");
          List<?> oldAttachments = (List<?>) getFieldValue(pb, attachmentsField);
          List<?> newAttachments = (List<?>) getFieldValue(rankPermissible, attachmentsField);
          if (oldAttachments != null && newAttachments != null) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) newAttachments;
            for (Object att : oldAttachments) {
              if (att instanceof PermissionAttachment && !list.contains(att)) {
                list.add(att);
              }
            }
          }
        } catch (Throwable ignored) {
        }
      }

      setFieldValue(player, permField, rankPermissible);
    } catch (Throwable ignored) {
    }
  }

  public static void uninject(@NotNull Player player) {
    try {
      Field permField = findPermField(player.getClass());
      if (permField == null) {
        return;
      }
      Object currentObj = getFieldValue(player, permField);
      if (currentObj instanceof RankPermissible rp && rp.oldPermissible != null) {
        setFieldValue(player, permField, rp.oldPermissible);
      }
    } catch (Throwable ignored) {
    }
  }
}
