package dev.smnl.smessential.manager;

import dev.smnl.smessential.service.StatisticService;
import dev.smnl.smessential.util.ComponentUtils;
import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeathManager implements Listener {

  private final JavaPlugin plugin;
  private final StatisticService statisticService;

  public DeathManager(@NotNull JavaPlugin plugin, @Nullable StatisticService statisticService) {
    this.plugin = plugin;
    this.statisticService = statisticService;
  }

  public DeathManager(@NotNull JavaPlugin plugin) {
    this(plugin, null);
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player victim = event.getEntity();
    long nativeDeaths = 0L;
    try {
      nativeDeaths = victim.getStatistic(org.bukkit.Statistic.DEATHS);
    } catch (Exception ignored) {
    }
    long totalDeaths = Math.max(1L, nativeDeaths);

    Component victimDisplay = PlayerUtils.getGeneralDisplayName(victim);

    Component deathBody = buildDeathMessageBody(victim, victimDisplay, event);
    Component deathCountSuffix =
        Component.text(" (", NamedTextColor.GRAY)
            .append(Component.text(totalDeaths, NamedTextColor.GRAY))
            .append(Component.text(totalDeaths == 1 ? " death)" : " deaths)", NamedTextColor.GRAY));

    Component fullMessage =
        MessageFormatter.formatInfo("World", deathBody.append(deathCountSuffix));

    event.deathMessage(fullMessage);

    Location deathLoc = victim.getLocation();
    int x = deathLoc.getBlockX();
    int y = deathLoc.getBlockY();
    int z = deathLoc.getBlockZ();
    String coordStr = x + ", " + y + ", " + z;
    String worldName = deathLoc.getWorld() != null ? deathLoc.getWorld().getName() : "world";

    Component privateCoordMsg =
        MessageFormatter.formatInfo(
            "World",
            Component.text("You died at ", NamedTextColor.GRAY)
                .append(
                    Component.text("(" + coordStr + ")", NamedTextColor.WHITE)
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Click to copy coordinates", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.copyToClipboard(coordStr)))
                .append(Component.text(" in ", NamedTextColor.GRAY))
                .append(Component.text(worldName, NamedTextColor.WHITE))
                .append(Component.text(".", NamedTextColor.GRAY)));

    victim.sendMessage(privateCoordMsg);
  }

  private @NotNull Component buildDeathMessageBody(
      @NotNull Player victim, @NotNull Component victimDisplay, @NotNull PlayerDeathEvent event) {
    EntityDamageEvent lastDamage = victim.getLastDamageCause();
    DamageCause cause = lastDamage != null ? lastDamage.getCause() : null;
    DamageSource damageSource = event.getDamageSource();

    Player killer = victim.getKiller();
    Entity causingEntity = resolveCausingEntity(damageSource, lastDamage);
    Entity directEntity = resolveDirectEntity(damageSource, lastDamage);

    // 1. Direct or indirect Player Kill
    if (killer != null || causingEntity instanceof Player) {
      Player attackingPlayer = killer != null ? killer : (Player) causingEntity;
      return buildPlayerKillMessage(
          victimDisplay, attackingPlayer, directEntity, cause, damageSource);
    }

    // 2. Mob / Non-Player Entity Kill
    if (causingEntity != null) {
      return buildMobKillMessage(victimDisplay, causingEntity, directEntity, cause, damageSource);
    }

    // 3. Environmental / Natural Causes
    return buildEnvironmentalDeathMessage(victimDisplay, cause, damageSource);
  }

  private @NotNull Component buildPlayerKillMessage(
      @NotNull Component victimDisplay,
      @NotNull Player killer,
      @Nullable Entity directEntity,
      @Nullable DamageCause cause,
      @NotNull DamageSource damageSource) {
    Component killerDisplay = PlayerUtils.getGeneralDisplayName(killer);
    String damageTypeKey = damageSource.getDamageType().getKey().getKey();

    if ("mace_smash".equalsIgnoreCase(damageTypeKey)) {
      return formatPlayerDeath(
          victimDisplay, " was smashed into oblivion by ", killerDisplay, killer);
    }

    if (directEntity instanceof Projectile projectile) {
      if (projectile instanceof Trident) {
        return formatPlayerDeath(victimDisplay, " was impaled by ", killerDisplay, killer);
      }
      if (projectile instanceof Arrow || projectile instanceof SpectralArrow) {
        return formatPlayerDeath(victimDisplay, " was shot by ", killerDisplay, killer);
      }
      if (projectile instanceof WindCharge) {
        return formatPlayerDeath(victimDisplay, " was blasted away by ", killerDisplay, killer);
      }
    }

    if (directEntity instanceof Firework) {
      return formatPlayerDeath(
          victimDisplay, " went off with a bang due to ", killerDisplay, killer);
    }

    if (cause == DamageCause.THORNS) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" died trying to hurt ", NamedTextColor.GRAY))
          .append(killerDisplay);
    }

    if (cause == DamageCause.MAGIC || cause == DamageCause.POISON) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was killed by ", NamedTextColor.GRAY))
          .append(killerDisplay)
          .append(Component.text(" using magic", NamedTextColor.GRAY));
    }

    return formatPlayerDeath(victimDisplay, " was slain by ", killerDisplay, killer);
  }

  private @NotNull Component formatPlayerDeath(
      @NotNull Component victimDisplay,
      @NotNull String action,
      @NotNull Component killerDisplay,
      @NotNull Player killer) {
    Component base =
        Component.empty()
            .append(victimDisplay)
            .append(Component.text(action, NamedTextColor.GRAY))
            .append(killerDisplay);

    ItemStack item = killer.getInventory().getItemInMainHand();
    if (item.getType() != Material.AIR) {
      Component weaponComponent = formatWeaponItem(item);
      if (weaponComponent != null) {
        base = base.append(Component.text(" using ", NamedTextColor.GRAY)).append(weaponComponent);
      }
    }

    return base;
  }

  private @Nullable Component formatWeaponItem(@NotNull ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
      return Component.text("[", NamedTextColor.WHITE)
          .append(meta.displayName())
          .append(Component.text("]", NamedTextColor.WHITE));
    }

    String typeName = item.getType().name();
    if (isNotableWeaponOrTool(typeName)) {
      return Component.text(
          "[" + FontUtils.formatEnumTitleCase(typeName) + "]", NamedTextColor.WHITE);
    }

    return null;
  }

  private boolean isNotableWeaponOrTool(@NotNull String typeName) {
    return typeName.endsWith("_SWORD")
        || typeName.endsWith("_AXE")
        || typeName.endsWith("_MACE")
        || typeName.equals("MACE")
        || typeName.equals("BOW")
        || typeName.equals("CROSSBOW")
        || typeName.equals("TRIDENT")
        || typeName.endsWith("_SHOVEL")
        || typeName.endsWith("_PICKAXE")
        || typeName.endsWith("_HOE")
        || typeName.equals("STICK");
  }

  private @NotNull Component buildMobKillMessage(
      @NotNull Component victimDisplay,
      @NotNull Entity causingEntity,
      @Nullable Entity directEntity,
      @Nullable DamageCause cause,
      @NotNull DamageSource damageSource) {
    String entityName = formatEntityName(causingEntity);
    String damageTypeKey = damageSource.getDamageType().getKey().getKey();

    if ("sonic_boom".equalsIgnoreCase(damageTypeKey) || cause == DamageCause.SONIC_BOOM) {
      return Component.empty()
          .append(victimDisplay)
          .append(
              Component.text(
                  " was obliterated by a sonically-charged shriek", NamedTextColor.GRAY));
    }

    if (directEntity instanceof Trident) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was impaled by ", NamedTextColor.GRAY))
          .append(Component.text(entityName, NamedTextColor.GRAY));
    }

    if (directEntity instanceof Arrow || directEntity instanceof SpectralArrow) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was shot by ", NamedTextColor.GRAY))
          .append(Component.text(entityName, NamedTextColor.GRAY));
    }

    if (directEntity instanceof Fireball
        || directEntity instanceof org.bukkit.entity.SmallFireball
        || directEntity instanceof org.bukkit.entity.DragonFireball) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was fireballed by ", NamedTextColor.GRAY))
          .append(Component.text(entityName, NamedTextColor.GRAY));
    }

    if (directEntity instanceof WitherSkull) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was shot by Wither", NamedTextColor.GRAY));
    }

    if (directEntity instanceof ShulkerBullet) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was sniped by Shulker", NamedTextColor.GRAY));
    }

    if (directEntity instanceof TNTPrimed
        || causingEntity.getType().name().equals("CREEPER")
        || cause == DamageCause.ENTITY_EXPLOSION) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was blown up by ", NamedTextColor.GRAY))
          .append(Component.text(entityName, NamedTextColor.GRAY));
    }

    if (cause == DamageCause.THORNS) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" died trying to hurt ", NamedTextColor.GRAY))
          .append(Component.text(entityName, NamedTextColor.GRAY));
    }

    if (cause == DamageCause.MAGIC || cause == DamageCause.POISON) {
      return Component.empty()
          .append(victimDisplay)
          .append(Component.text(" was killed by ", NamedTextColor.GRAY))
          .append(Component.text(entityName, NamedTextColor.GRAY))
          .append(Component.text(" using magic", NamedTextColor.GRAY));
    }

    Component base =
        Component.empty()
            .append(victimDisplay)
            .append(Component.text(" was slain by ", NamedTextColor.GRAY))
            .append(Component.text(entityName, NamedTextColor.GRAY));

    if (causingEntity instanceof LivingEntity living) {
      ItemStack item =
          living.getEquipment() != null ? living.getEquipment().getItemInMainHand() : null;
      if (item != null && item.getType() != Material.AIR) {
        Component weaponComponent = formatWeaponItem(item);
        if (weaponComponent != null) {
          base =
              base.append(Component.text(" using ", NamedTextColor.GRAY)).append(weaponComponent);
        }
      }
    }

    return base;
  }

  private @NotNull Component buildEnvironmentalDeathMessage(
      @NotNull Component victimDisplay,
      @Nullable DamageCause cause,
      @NotNull DamageSource damageSource) {
    String damageTypeKey = damageSource.getDamageType().getKey().getKey();

    if ("sweet_berry_bush".equalsIgnoreCase(damageTypeKey)) {
      return simpleMessage(victimDisplay, " was poked to death by a sweet berry bush");
    }
    if ("stalagmite".equalsIgnoreCase(damageTypeKey)
        || "falling_stalactite".equalsIgnoreCase(damageTypeKey)) {
      return simpleMessage(victimDisplay, " was impaled on a pointed dripstone");
    }
    if ("falling_anvil".equalsIgnoreCase(damageTypeKey)) {
      return simpleMessage(victimDisplay, " was squashed by a falling anvil");
    }
    if ("cactus".equalsIgnoreCase(damageTypeKey)) {
      return simpleMessage(victimDisplay, " was pricked to death");
    }

    if (cause == null) {
      return simpleMessage(victimDisplay, " died");
    }

    return switch (cause) {
      case FALL -> simpleMessage(victimDisplay, " fell from a high place");
      case LAVA -> simpleMessage(victimDisplay, " tried to swim in lava");
      case FIRE, FIRE_TICK -> simpleMessage(victimDisplay, " burned to death");
      case DROWNING -> simpleMessage(victimDisplay, " drowned");
      case SUFFOCATION -> simpleMessage(victimDisplay, " suffocated in a wall");
      case STARVATION -> simpleMessage(victimDisplay, " starved to death");
      case VOID -> simpleMessage(victimDisplay, " fell out of the world");
      case LIGHTNING -> simpleMessage(victimDisplay, " was struck by lightning");
      case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> simpleMessage(victimDisplay, " blew up");
      case CONTACT -> simpleMessage(victimDisplay, " was pricked to death");
      case HOT_FLOOR -> simpleMessage(victimDisplay, " discovered the floor was lava");
      case FREEZE -> simpleMessage(victimDisplay, " froze to death");
      case FLY_INTO_WALL -> simpleMessage(victimDisplay, " experienced kinetic energy");
      case FALLING_BLOCK -> simpleMessage(victimDisplay, " was squashed by a falling block");
      case MAGIC, POISON -> simpleMessage(victimDisplay, " was killed by magic");
      case WITHER -> simpleMessage(victimDisplay, " withered away");
      case SONIC_BOOM ->
          simpleMessage(victimDisplay, " was obliterated by a sonically-charged shriek");
      case CAMPFIRE -> simpleMessage(victimDisplay, " stepped into a campfire");
      case CRAMMING -> simpleMessage(victimDisplay, " was squished too much");
      case DRYOUT -> simpleMessage(victimDisplay, " died of dehydration");
      case DRAGON_BREATH -> simpleMessage(victimDisplay, " was roasted in dragon breath");
      case WORLD_BORDER -> simpleMessage(victimDisplay, " left the confines of this world");
      case SUICIDE, KILL -> simpleMessage(victimDisplay, " died");
      default -> simpleMessage(victimDisplay, " died");
    };
  }

  private @NotNull Component simpleMessage(@NotNull Component victimDisplay, @NotNull String text) {
    return Component.empty()
        .append(victimDisplay)
        .append(Component.text(text, NamedTextColor.GRAY));
  }

  private @NotNull String resolvePlayerName(@NotNull Player player) {
    return player.getName();
  }

  private @NotNull String formatEntityName(@NotNull Entity entity) {
    if (entity.customName() != null) {
      return ComponentUtils.toPlainText(entity.customName());
    }
    return FontUtils.formatEnumTitleCase(entity.getType().name());
  }

  private @Nullable Entity resolveCausingEntity(
      @NotNull DamageSource damageSource, @Nullable EntityDamageEvent lastDamage) {
    if (damageSource.getCausingEntity() != null) {
      return damageSource.getCausingEntity();
    }
    if (lastDamage instanceof EntityDamageByEntityEvent edbe) {
      if (edbe.getDamager() instanceof Projectile proj
          && proj.getShooter() instanceof Entity shooter) {
        return shooter;
      }
      return edbe.getDamager();
    }
    return null;
  }

  private @Nullable Entity resolveDirectEntity(
      @NotNull DamageSource damageSource, @Nullable EntityDamageEvent lastDamage) {
    if (damageSource.getDirectEntity() != null) {
      return damageSource.getDirectEntity();
    }
    if (lastDamage instanceof EntityDamageByEntityEvent edbe) {
      return edbe.getDamager();
    }
    return null;
  }
}
