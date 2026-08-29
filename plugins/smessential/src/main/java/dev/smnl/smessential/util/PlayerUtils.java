package dev.smnl.smessential.util;

import dev.smnl.smessential.SMEssential;
import dev.smnl.smessential.model.Rank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerUtils {

  private PlayerUtils() {}

  public static boolean isBedrockPlayer(@NotNull Player player) {
    return isBedrockPlayer(player.getUniqueId(), player.getName());
  }

  public static boolean isBedrockPlayer(@NotNull UUID uuid, @Nullable String name) {
    try {
      Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
      Object instance = floodgateApiClass.getMethod("getInstance").invoke(null);
      if (instance != null) {
        boolean isFloodgate =
            (boolean)
                floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class).invoke(instance, uuid);
        if (isFloodgate) {
          return true;
        }
      }
    } catch (Throwable ignored) {
    }

    try {
      Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
      Object instance = geyserApiClass.getMethod("api").invoke(null);
      if (instance != null) {
        boolean isGeyser =
            (boolean)
                geyserApiClass.getMethod("isBedrockPlayer", UUID.class).invoke(instance, uuid);
        if (isGeyser) {
          return true;
        }
      }
    } catch (Throwable ignored) {
    }

    return name != null && (name.startsWith(".") || name.startsWith("*") || name.startsWith("_"));
  }

  public static @NotNull List<String> getSuggestedPlayerNames(
      @NotNull CommandSender sender, @NotNull String prefix) {
    String lowerPrefix = prefix.toLowerCase();
    List<String> names = new ArrayList<>();

    for (Player player : Bukkit.getOnlinePlayers()) {
      String name = player.getName();
      if (name.toLowerCase().startsWith(lowerPrefix)) {
        names.add(name);
      }
    }
    return names;
  }

  public static @Nullable Player findOnlinePlayer(
      @NotNull String name, @Nullable CommandSender viewer) {
    Player player = Bukkit.getPlayer(name);

    if (player == null) {
      for (Player online : Bukkit.getOnlinePlayers()) {
        if (online.getName().equalsIgnoreCase(name)) {
          player = online;
          break;
        }
      }
    }

    if (player == null) {
      String cleanInput = name.replaceFirst("^[.*_]", "");
      for (Player online : Bukkit.getOnlinePlayers()) {
        String cleanOnline = online.getName().replaceFirst("^[.*_]", "");
        if (cleanOnline.equalsIgnoreCase(cleanInput)) {
          player = online;
          break;
        }
      }
    }

    return player;
  }

  public static @Nullable Player findOnlinePlayer(@NotNull String name) {
    return findOnlinePlayer(name, null);
  }

  public static @NotNull Component getStaffVisibleDisplayName(@NotNull CommandSender sender) {
    if (sender instanceof Player player) {
      return getPlayerDisplayName(player);
    }
    return Component.text(sender.getName());
  }

  public static @NotNull Component getGeneralDisplayName(@NotNull CommandSender sender) {
    if (sender instanceof Player player) {
      return getPlayerDisplayName(player);
    }
    return Component.text(sender.getName(), NamedTextColor.WHITE);
  }

  public static @NotNull Component getPlayerDisplayName(@NotNull Player player) {
    if (SMEssential.getInstance() != null && SMEssential.getInstance().getRankService() != null) {
      Rank rank = SMEssential.getInstance().getRankService().getPlayerRank(player);
      return rank.formatPlayerDisplayName(player.getName());
    }
    return Component.text(player.getName(), NamedTextColor.WHITE);
  }

  public static @NotNull String getPlayerRank(@NotNull Player player) {
    if (SMEssential.getInstance() != null && SMEssential.getInstance().getRankService() != null) {
      return SMEssential.getInstance().getRankService().getPlayerRank(player).getName();
    }
    return "Default";
  }

  public static @NotNull Component getPlayerRankComponent(@NotNull Player player) {
    if (SMEssential.getInstance() != null && SMEssential.getInstance().getRankService() != null) {
      return SMEssential.getInstance()
          .getRankService()
          .getPlayerRank(player)
          .getRankDisplayNameComponent();
    }
    return Component.text("Default", NamedTextColor.GOLD);
  }

  public static @NotNull Component getPlayerPrefixComponent(@NotNull Player player) {
    if (SMEssential.getInstance() != null && SMEssential.getInstance().getRankService() != null) {
      return SMEssential.getInstance().getRankService().getPlayerRank(player).getPrefixComponent();
    }
    return Component.empty();
  }

  public static @Nullable String getPlayerGroupColor(@NotNull Player player) {
    if (SMEssential.getInstance() != null && SMEssential.getInstance().getRankService() != null) {
      return SMEssential.getInstance().getRankService().getPlayerRank(player).getColor();
    }
    return null;
  }

  public static @NotNull Component getPlayerGroupColoredName(
      @NotNull Player player, @NotNull String name) {
    if (SMEssential.getInstance() != null && SMEssential.getInstance().getRankService() != null) {
      return SMEssential.getInstance()
          .getRankService()
          .getPlayerRank(player)
          .formatPlayerColoredName(name);
    }
    return Component.text(name, NamedTextColor.WHITE);
  }

  public static @NotNull String resolvePlayerName(@NotNull Player player) {
    return player.getName();
  }

  public static void broadcastMessage(@NotNull Component message) {
    for (Player online : Bukkit.getOnlinePlayers()) {
      online.sendMessage(message);
    }
  }

  public static @NotNull String getPlayerIp(@NotNull Player player) {
    if (player.getAddress() != null && player.getAddress().getAddress() != null) {
      return player.getAddress().getAddress().getHostAddress();
    }
    return "Unknown";
  }

  public static @NotNull String getPlayerVersionString(@NotNull Player player) {
    String versionName = resolveVersionName(player);
    String brand = formatBrand(player.getClientBrandName());
    if (brand != null && !brand.isBlank() && !brand.equalsIgnoreCase("Vanilla")) {
      return versionName + " (" + brand + ")";
    }
    return versionName;
  }

  private static @NotNull String resolveVersionName(@NotNull Player player) {
    // 1. Try ViaVersion reflection if available
    try {
      Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via");
      Object viaAPI = viaClass.getMethod("getAPI").invoke(null);
      if (viaAPI != null) {
        int playerProto =
            (int)
                viaAPI
                    .getClass()
                    .getMethod("getPlayerVersion", Object.class)
                    .invoke(viaAPI, player);
        Class<?> protoClass =
            Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
        Object protoObj = protoClass.getMethod("getProtocol", int.class).invoke(null, playerProto);
        if (protoObj != null) {
          String name = (String) protoClass.getMethod("getName").invoke(protoObj);
          if (name != null && !name.isBlank() && !name.toLowerCase().startsWith("unknown")) {
            return name;
          }
        }
      }
    } catch (Throwable ignored) {
    }

    // 2. Map known protocol integers
    int protocol = player.getProtocolVersion();
    String mapped = mapProtocolToVersion(protocol);
    if (mapped != null) {
      return mapped;
    }

    // 3. Fallback to server's Minecraft version
    String mcVer = Bukkit.getMinecraftVersion();
    if (mcVer != null && !mcVer.isBlank()) {
      return mcVer;
    }

    return "1.21";
  }

  private static @Nullable String formatBrand(@Nullable String rawBrand) {
    if (rawBrand == null || rawBrand.isBlank()) {
      return null;
    }
    String trimmed = rawBrand.trim();
    if (trimmed.equalsIgnoreCase("vanilla")) {
      return "Vanilla";
    }
    if (trimmed.equalsIgnoreCase("fabric")) {
      return "Fabric";
    }
    if (trimmed.equalsIgnoreCase("forge")) {
      return "Forge";
    }
    if (trimmed.equalsIgnoreCase("neoforge")) {
      return "NeoForge";
    }
    if (trimmed.equalsIgnoreCase("lunarclient") || trimmed.equalsIgnoreCase("lunar")) {
      return "Lunar";
    }
    if (trimmed.equalsIgnoreCase("badlion")) {
      return "Badlion";
    }
    if (trimmed.equalsIgnoreCase("feather")) {
      return "Feather";
    }
    if (trimmed.equalsIgnoreCase("labymod")) {
      return "LabyMod";
    }
    return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
  }

  private static @Nullable String mapProtocolToVersion(int protocol) {
    return switch (protocol) {
      case 776, 775, 774, 773, 772, 771, 770, 769, 768, 767 -> "1.21";
      case 766 -> "1.20.5/1.20.6";
      case 765 -> "1.20.3/1.20.4";
      case 764 -> "1.20.2";
      case 763 -> "1.20/1.20.1";
      case 762 -> "1.19.4";
      case 761 -> "1.19.3";
      case 760 -> "1.19.1/1.19.2";
      case 759 -> "1.19";
      case 758 -> "1.18.2";
      case 757 -> "1.18/1.18.1";
      case 756 -> "1.17.1";
      case 755 -> "1.17";
      case 754 -> "1.16.4/1.16.5";
      case 753 -> "1.16.3";
      case 751 -> "1.16.2";
      case 736 -> "1.16.1";
      case 735 -> "1.16";
      case 578 -> "1.15.2";
      case 575 -> "1.15.1";
      case 573 -> "1.15";
      case 498 -> "1.14.4";
      case 477 -> "1.14";
      case 404 -> "1.13.2";
      case 393 -> "1.13";
      case 340 -> "1.12.2";
      case 47 -> "1.8.9";
      default -> null;
    };
  }

  public static double getPlayerMaxHealth(@NotNull Player player) {
    try {
      var instance = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
      if (instance != null) {
        return instance.getValue();
      }
    } catch (Throwable ignored) {
    }
    return 20.0;
  }
}
