/*
 * 현재 프로그램은 l2j팀에서 제공하는 무료 소프트웨어 이며
 * Police Team이 한국인 이용자에 맞도록 수정하여 재배포 합니다.
 * 
 * 문의는 아래 네이트온으로 해주시기 바랍니다.
 * 
 * 폴리스 네이트온: policecorps@nate.com
 * 테스트 서버: http://polis.esy.es/
 */
package com.polis.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import com.polis.Config;
import com.polis.L2DatabaseFactory;
import com.polis.Server;
import com.polis.UPnPService;
import com.polis.gameserver.cache.HtmCache;
import com.polis.gameserver.datatables.AdminTable;
import com.polis.gameserver.datatables.ArmorSetsData;
import com.polis.gameserver.datatables.AugmentationData;
import com.polis.gameserver.datatables.BotReportTable;
import com.polis.gameserver.datatables.BuyListData;
import com.polis.gameserver.datatables.CategoryData;
import com.polis.gameserver.datatables.CharNameTable;
import com.polis.gameserver.datatables.CharSummonTable;
import com.polis.gameserver.datatables.CharTemplateTable;
import com.polis.gameserver.datatables.ClanTable;
import com.polis.gameserver.datatables.ClassListData;
import com.polis.gameserver.datatables.CrestTable;
import com.polis.gameserver.datatables.DoorTable;
import com.polis.gameserver.datatables.EnchantItemData;
import com.polis.gameserver.datatables.EnchantItemGroupsData;
import com.polis.gameserver.datatables.EnchantItemHPBonusData;
import com.polis.gameserver.datatables.EnchantItemOptionsData;
import com.polis.gameserver.datatables.EnchantSkillGroupsData;
import com.polis.gameserver.datatables.EventDroplist;
import com.polis.gameserver.datatables.ExperienceTable;
import com.polis.gameserver.datatables.FishData;
import com.polis.gameserver.datatables.FishingMonstersData;
import com.polis.gameserver.datatables.FishingRodsData;
import com.polis.gameserver.datatables.HennaData;
import com.polis.gameserver.datatables.HitConditionBonus;
import com.polis.gameserver.datatables.InitialEquipmentData;
import com.polis.gameserver.datatables.InitialShortcutData;
import com.polis.gameserver.datatables.ItemTable;
import com.polis.gameserver.datatables.KarmaData;
import com.polis.gameserver.datatables.ManorData;
import com.polis.gameserver.datatables.MerchantPriceConfigTable;
import com.polis.gameserver.datatables.MultisellData;
import com.polis.gameserver.datatables.NpcBufferTable;
import com.polis.gameserver.datatables.NpcData;
import com.polis.gameserver.datatables.OfflineTradersTable;
import com.polis.gameserver.datatables.OptionsData;
import com.polis.gameserver.datatables.PetDataTable;
import com.polis.gameserver.datatables.RecipeData;
import com.polis.gameserver.datatables.SecondaryAuthData;
import com.polis.gameserver.datatables.SiegeScheduleData;
import com.polis.gameserver.datatables.SkillData;
import com.polis.gameserver.datatables.SkillLearnData;
import com.polis.gameserver.datatables.SkillTreesData;
import com.polis.gameserver.datatables.SpawnTable;
import com.polis.gameserver.datatables.StaticObjects;
import com.polis.gameserver.datatables.SummonSkillsTable;
import com.polis.gameserver.datatables.TeleportLocationTable;
import com.polis.gameserver.datatables.TransformData;
import com.polis.gameserver.datatables.UIData;
import com.polis.gameserver.geoeditorcon.GeoEditorListener;
import com.polis.gameserver.handler.EffectHandler;
import com.polis.gameserver.idfactory.IdFactory;
import com.polis.gameserver.instancemanager.AirShipManager;
import com.polis.gameserver.instancemanager.AntiFeedManager;
import com.polis.gameserver.instancemanager.AuctionManager;
import com.polis.gameserver.instancemanager.BoatManager;
import com.polis.gameserver.instancemanager.CHSiegeManager;
import com.polis.gameserver.instancemanager.CastleManager;
import com.polis.gameserver.instancemanager.CastleManorManager;
import com.polis.gameserver.instancemanager.ClanHallManager;
import com.polis.gameserver.instancemanager.CoupleManager;
import com.polis.gameserver.instancemanager.CursedWeaponsManager;
import com.polis.gameserver.instancemanager.DayNightSpawnManager;
import com.polis.gameserver.instancemanager.DimensionalRiftManager;
import com.polis.gameserver.instancemanager.FortManager;
import com.polis.gameserver.instancemanager.FortSiegeManager;
import com.polis.gameserver.instancemanager.FourSepulchersManager;
import com.polis.gameserver.instancemanager.GlobalVariablesManager;
import com.polis.gameserver.instancemanager.GraciaSeedsManager;
import com.polis.gameserver.instancemanager.GrandBossManager;
import com.polis.gameserver.instancemanager.InstanceManager;
import com.polis.gameserver.instancemanager.ItemAuctionManager;
import com.polis.gameserver.instancemanager.ItemsOnGroundManager;
import com.polis.gameserver.instancemanager.MailManager;
import com.polis.gameserver.instancemanager.MapRegionManager;
import com.polis.gameserver.instancemanager.MercTicketManager;
import com.polis.gameserver.instancemanager.PetitionManager;
import com.polis.gameserver.instancemanager.PunishmentManager;
import com.polis.gameserver.instancemanager.QuestManager;
import com.polis.gameserver.instancemanager.RaidBossPointsManager;
import com.polis.gameserver.instancemanager.RaidBossSpawnManager;
import com.polis.gameserver.instancemanager.SiegeManager;
import com.polis.gameserver.instancemanager.TerritoryWarManager;
import com.polis.gameserver.instancemanager.WalkingManager;
import com.polis.gameserver.instancemanager.ZoneManager;
import com.polis.gameserver.model.AutoSpawnHandler;
import com.polis.gameserver.model.L2World;
import com.polis.gameserver.model.PartyMatchRoomList;
import com.polis.gameserver.model.PartyMatchWaitingList;
import com.polis.gameserver.model.entity.Hero;
import com.polis.gameserver.model.entity.TvTManager;
import com.polis.gameserver.model.events.EventDispatcher;
import com.polis.gameserver.model.olympiad.Olympiad;
import com.polis.gameserver.network.L2GameClient;
import com.polis.gameserver.network.L2GamePacketHandler;
import com.polis.gameserver.network.communityserver.CommunityServerThread;
import com.polis.gameserver.pathfinding.PathFinding;
import com.polis.gameserver.script.faenor.FaenorScriptEngine;
import com.polis.gameserver.scripting.L2ScriptEngineManager;
import com.polis.gameserver.taskmanager.AutoAnnounceTaskManager;
import com.polis.gameserver.taskmanager.KnownListUpdateTaskManager;
import com.polis.gameserver.taskmanager.TaskManager;
import com.polis.status.Status;
import com.polis.util.DeadLockDetector;
import com.polis.util.IPv4Filter;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		gameServer = this;
		long serverLoadStart = System.currentTimeMillis();
		
		_log.info(getClass().getSimpleName() + ": 사용된 메모리: " + getUsedMemoryMB() + "MB");
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe(getClass().getSimpleName() + ": DB 에서 object ID 화일 들을  읽을수 없습니다 . 데이터를 다시 한번 확인 하세요.");
			throw new Exception("ID factory 를 초기화 하지 못했습니다.");
		}
		
		ThreadPoolManager.getInstance();
		EventDispatcher.getInstance();
		
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("엔  진");
		L2ScriptEngineManager.getInstance();
		
		printSection("월  드");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("데이타");
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		
		printSection("스  킬");
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillData.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("아이템");
		ItemTable.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		OptionsData.getInstance();
		EnchantItemHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		BuyListData.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		
		printSection("캐릭터");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceTable.getInstance();
		KarmaData.getInstance();
		HitConditionBonus.getInstance();
		CharTemplateTable.getInstance();
		CharNameTable.getInstance();
		AdminTable.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance().init();
		
		printSection("클  랜");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("지  오");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			PathFinding.getInstance();
		}
		
		printSection("엔피씨");
		SkillLearnData.getInstance();
		NpcData.getInstance();
		WalkingManager.getInstance();
		StaticObjects.getInstance();
		ZoneManager.getInstance();
		DoorTable.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		SpawnTable.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("성");
		SiegeScheduleData.getInstance();
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		ManorData.getInstance();
		
		printSection("올림피아");
		Olympiad.getInstance();
		Hero.getInstance();
		
		printSection("세븐사인");
		SevenSigns.getInstance();
		
		// Call to load caches
		printSection("캐  시");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportLocationTable.getInstance();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();
		
		printSection("스크립트");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		MerchantPriceConfigTable.getInstance().updateReferences();
		
		try
		{
			_log.info("프로젝트 폴리스팀" + ": 서버 스크립트 화일을 로딩:");
			final File scripts = new File(Config.DATAPACK_ROOT, "data/scripts.cfg");
			if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
			{
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
			}
		}
		catch (IOException ioe)
		{
			_log.severe(getClass().getSimpleName() + ": scripts.cfg 화일을 로드 하지 못했습니다. 로드할 스크립트가 없습니다!");
		}
		
		QuestManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("오토스폰핸들러: " + AutoSpawnHandler.getInstance().size() + " 개가 로드됨!");
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		
		PunishmentManager.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		TvTManager.getInstance();
		KnownListUpdateTaskManager.getInstance();
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info(": 폴리스팩 게임서버 메모리 " + totalMem + " Mb중 " + freeMem + " Mb가사용됩니다. ");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		CommunityServerThread.initialize();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": 경고: 모든 사용 가능한 Ip를 사용하여: " + e1.getMessage() + "" + e1 + "GameServer bind 주소가 잘못 되었습니다");
				
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
			_selectorThread.start();
			// _log.log(Level.INFO, getClass().getSimpleName() + ": is now listening on: " + Config.GAMESERVER_HOSTNAME + ":" + Config.PORT_GAME);
			_log.log(Level.INFO, " 폴리스 서버팩 게임서버 포트" + Config.GAMESERVER_HOSTNAME + ":" + Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": 경고: 서버 소켓을 " + e.getMessage() + "" + e + "때문에 열지 못했습니다.");
			System.exit(1);
		}
		
		// _log.log(Level.INFO, getClass().getSimpleName() + ": Maximum numbers of connected players: " + Config.MAXIMUM_ONLINE_USERS);
		// _log.log(Level.INFO, getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		_log.log(Level.INFO, " 폴리스 서버 최대 접속허용 인원 : " + Config.MAXIMUM_ONLINE_USERS + "명!");
		_log.log(Level.INFO, " 폴리스 게임 서버 구동 시간은  " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + "초나 걸렸어요!");
		
		printSection("UPnP");
		UPnPService.getInstance();
		
		AutoAnnounceTaskManager.getInstance();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		printSection("데이터");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info("프로젝트 폴리스팀" + ": 폴리스 서버팩 텔넷 서버 비활성!");
			printSection("정  보");
			_log.info("프로젝트 폴리스팀: 폴리스(Police, Polis)");
			_log.info("프로젝트 폴리스팀 소스는 l2j팀 하이파이브팩 기반으로 수정되었습니다.");
			_log.info("프로젝트 폴리스팀 서버팩은 상업목적으로 사용할 수 없으며, 네이트온에서 무료로 배포중입니다.");
			_log.info("-----------------------------------------------------------");
			_log.info("커뮤니티 사이트1: http://polis.esy.es/");
			_log.info("커뮤니티 사이트2: http://추천받습니다. ^^");
			_log.info("커뮤니티 사이트3: http://추천받습니다. ^^");
			_log.info("프로젝트 폴리스 네이트온: policecorps@nate.com");
			_log.info("프로젝트 폴리스 빌드: 2017년 2월 21일");
			_log.info("-----------------------------------------------------------");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
}
