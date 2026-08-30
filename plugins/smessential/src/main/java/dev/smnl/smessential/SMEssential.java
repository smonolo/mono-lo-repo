package dev.smnl.smessential;

import dev.smnl.smessential.command.CommandManager;
import dev.smnl.smessential.command.impl.AfkCommand;
import dev.smnl.smessential.command.impl.AlertCommand;
import dev.smnl.smessential.command.impl.ClearCommand;
import dev.smnl.smessential.command.impl.DatabaseInfoCommand;
import dev.smnl.smessential.command.impl.DifficultyCommand;
import dev.smnl.smessential.command.impl.FeedCommand;
import dev.smnl.smessential.command.impl.FindCommand;
import dev.smnl.smessential.command.impl.FlyCommand;
import dev.smnl.smessential.command.impl.FreezeCommand;
import dev.smnl.smessential.command.impl.GamemodeCommand;
import dev.smnl.smessential.command.impl.GiveCommand;
import dev.smnl.smessential.command.impl.GlobalMuteCommand;
import dev.smnl.smessential.command.impl.HealCommand;
import dev.smnl.smessential.command.impl.HelpCommand;
import dev.smnl.smessential.command.impl.InfoCommand;
import dev.smnl.smessential.command.impl.InvseeCommand;
import dev.smnl.smessential.command.impl.LeaderboardCommand;
import dev.smnl.smessential.command.impl.ListCommand;
import dev.smnl.smessential.command.impl.MsgCommand;
import dev.smnl.smessential.command.impl.PingCommand;
import dev.smnl.smessential.command.impl.PunishCommand;
import dev.smnl.smessential.command.impl.RankCommand;
import dev.smnl.smessential.command.impl.ReplyCommand;
import dev.smnl.smessential.command.impl.SeedCommand;
import dev.smnl.smessential.command.impl.ServerInfoCommand;
import dev.smnl.smessential.command.impl.SpectateCommand;
import dev.smnl.smessential.command.impl.StaffChatCommand;
import dev.smnl.smessential.command.impl.StatsCommand;
import dev.smnl.smessential.command.impl.SupportCommand;
import dev.smnl.smessential.command.impl.TeamCommand;
import dev.smnl.smessential.command.impl.TeleportCommand;
import dev.smnl.smessential.command.impl.TimeCommand;
import dev.smnl.smessential.command.impl.WeatherCommand;
import dev.smnl.smessential.command.impl.WhitelistCommand;
import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.gui.AlertGUIManager;
import dev.smnl.smessential.gui.GUIManager;
import dev.smnl.smessential.gui.InfoGUIManager;
import dev.smnl.smessential.gui.InvseeGUIManager;
import dev.smnl.smessential.gui.PunishGUIManager;
import dev.smnl.smessential.gui.StatsGUIManager;
import dev.smnl.smessential.gui.TeamInviteGUIManager;
import dev.smnl.smessential.manager.AdvancementManager;
import dev.smnl.smessential.manager.BossManager;
import dev.smnl.smessential.manager.ChatManager;
import dev.smnl.smessential.manager.DeathManager;
import dev.smnl.smessential.manager.MotdManager;
import dev.smnl.smessential.manager.RaidManager;
import dev.smnl.smessential.manager.SidebarManager;
import dev.smnl.smessential.manager.SleepManager;
import dev.smnl.smessential.manager.TabListManager;
import dev.smnl.smessential.service.AfkService;
import dev.smnl.smessential.service.AlertService;
import dev.smnl.smessential.service.BanService;
import dev.smnl.smessential.service.FreezeService;
import dev.smnl.smessential.service.LeaderboardService;
import dev.smnl.smessential.service.MessageService;
import dev.smnl.smessential.service.MuteService;
import dev.smnl.smessential.service.RankService;
import dev.smnl.smessential.service.SpectateService;
import dev.smnl.smessential.service.StatisticService;
import dev.smnl.smessential.service.TeamService;
import dev.smnl.smessential.service.UserService;
import dev.smnl.smessential.service.WhitelistService;
import org.bukkit.plugin.java.JavaPlugin;

public final class SMEssential extends JavaPlugin {

  private static SMEssential instance;

  private DatabaseManager databaseManager;
  private UserService userService;
  private RankService rankService;
  private LeaderboardService leaderboardService;
  private AfkService afkService;
  private SpectateService spectateService;
  private dev.smnl.smessential.service.HttpServerService httpServerService;

  public static SMEssential getInstance() {
    return instance;
  }

  public UserService getUserService() {
    return userService;
  }

  public RankService getRankService() {
    return rankService;
  }

  public LeaderboardService getLeaderboardService() {
    return leaderboardService;
  }

  public DatabaseManager getDatabaseManager() {
    return databaseManager;
  }

  public AfkService getAfkService() {
    return afkService;
  }

  public SpectateService getSpectateService() {
    return spectateService;
  }

  @Override
  public void onEnable() {
    instance = this;
    saveDefaultConfig();

    databaseManager = new DatabaseManager(this);
    databaseManager.init();

    userService = new UserService(databaseManager);
    userService.setup(this);

    rankService = new RankService(databaseManager);
    rankService.setup(this, userService);
    dev.smnl.smessential.command.EssentialCommand.setRankService(rankService);

    StatisticService statisticService = new StatisticService(databaseManager, userService);
    statisticService.setup(this, userService);

    afkService = new AfkService(this);
    afkService.setup();

    spectateService = new SpectateService(this);
    spectateService.setup();

    leaderboardService =
        new LeaderboardService(databaseManager, statisticService, userService, rankService);
    leaderboardService.setup(this);

    SidebarManager sidebarManager = new SidebarManager(this);
    sidebarManager.setup();

    TabListManager tabListManager = new TabListManager(this, rankService);
    tabListManager.setup();

    DeathManager deathManager = new DeathManager(this, statisticService);
    deathManager.setup();

    AdvancementManager advancementManager = new AdvancementManager(this);
    advancementManager.setup();

    SleepManager sleepManager = new SleepManager(this);
    sleepManager.setup();

    BossManager bossManager = new BossManager(this);
    bossManager.setup();

    RaidManager raidManager = new RaidManager(this);
    raidManager.setup();

    MotdManager motdManager = new MotdManager(this);
    motdManager.setup();

    AlertService alertService = new AlertService(databaseManager);
    alertService.setup(this);

    ChatManager chatManager = new ChatManager(this, sidebarManager, rankService);
    chatManager.setup();

    GUIManager guiManager = new GUIManager(this);
    guiManager.setup();

    AlertGUIManager alertGUIManager = new AlertGUIManager(alertService);

    MuteService muteService = new MuteService(databaseManager);
    muteService.setup(this);

    BanService banService = new BanService(databaseManager);
    banService.setup(this);

    FreezeService freezeService = new FreezeService(databaseManager);
    freezeService.setup(this);

    PunishGUIManager punishGUIManager =
        new PunishGUIManager(muteService, banService, freezeService, databaseManager);

    TeamService teamService = new TeamService();
    teamService.setup(this, sidebarManager);
    sidebarManager.setTeamService(teamService);

    TeamInviteGUIManager teamInviteGUIManager = new TeamInviteGUIManager(teamService);
    teamService.setInviteGUIManager(teamInviteGUIManager);

    WhitelistService whitelistService =
        new WhitelistService(databaseManager, rankService, userService);
    whitelistService.setup(this);

    MessageService messageService = new MessageService(muteService);

    InvseeGUIManager invseeGUIManager = new InvseeGUIManager();

    StatsGUIManager statsGUIManager =
        new StatsGUIManager(statisticService, userService, rankService);

    InfoGUIManager infoGUIManager =
        new InfoGUIManager(
            freezeService, muteService, punishGUIManager, statisticService, invseeGUIManager);

    CommandManager commandManager = new CommandManager(this);
    commandManager.register(
        new FlyCommand(freezeService),
        new ListCommand(),
        new PingCommand(),
        new StatsCommand(statsGUIManager, userService),
        new AfkCommand(afkService),
        new AlertCommand(alertGUIManager, alertService),
        new PunishCommand(punishGUIManager),
        new FreezeCommand(freezeService),
        new SpectateCommand(spectateService),
        new StaffChatCommand(),
        new SupportCommand(),
        new TeamCommand(teamService),
        new GlobalMuteCommand(muteService),
        new MsgCommand(messageService),
        new ReplyCommand(messageService),
        new TeleportCommand(),
        new FindCommand(),
        new InfoCommand(infoGUIManager),
        new InvseeCommand(invseeGUIManager),
        new WhitelistCommand(whitelistService, rankService, userService),
        new ServerInfoCommand(),
        new DatabaseInfoCommand(databaseManager),
        new WeatherCommand(),
        new TimeCommand(),
        new DifficultyCommand(),
        new SeedCommand(),
        new GamemodeCommand(),
        new GamemodeCommand.QuickCreative(),
        new GamemodeCommand.QuickSurvival(),
        new GamemodeCommand.QuickSpectator(),
        new GamemodeCommand.QuickAdventure(),
        new ClearCommand(),
        new GiveCommand(),
        new FeedCommand(freezeService),
        new HealCommand(freezeService),
        new LeaderboardCommand(leaderboardService),
        new RankCommand(rankService, userService),
        new HelpCommand(commandManager));

    httpServerService =
        new dev.smnl.smessential.service.HttpServerService(
            this, rankService, afkService, userService, statisticService);
    httpServerService.setup();

    getLogger().info("SMEssential v" + getPluginMeta().getVersion() + " has been enabled!");
  }

  @Override
  public void onDisable() {
    for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
      try {
        if (player.getOpenInventory().getTopInventory().getHolder()
            instanceof dev.smnl.smessential.gui.GUIWindow) {
          player.closeInventory();
        }
      } catch (Throwable ignored) {
      }
    }
    if (httpServerService != null) {
      httpServerService.shutdown();
    }
    if (leaderboardService != null) {
      leaderboardService.shutdown();
    }
    if (databaseManager != null) {
      databaseManager.close();
    }
    getLogger().info("SMEssential has been disabled.");
  }
}
