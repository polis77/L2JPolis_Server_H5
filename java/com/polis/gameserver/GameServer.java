/*
 * ���� ���α׷��� l2j������ �����ϴ� ���� ����Ʈ���� �̸�
 * Police Team�� �ѱ��� �̿��ڿ� �µ��� �����Ͽ� ����� �մϴ�.
 * 
 * ���Ǵ� �Ʒ� ����Ʈ������ ���ֽñ� �ٶ��ϴ�.
 * 
 * ������ ����Ʈ��: policecorps@nate.com
 * �׽�Ʈ ����: http://polis.esy.es/
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
		
		_log.info(getClass().getSimpleName() + ": ���� �޸�: " + getUsedMemoryMB() + "MB");
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe(getClass().getSimpleName() + ": DB ���� object ID ȭ�� ����  ������ �����ϴ� . �����͸� �ٽ� �ѹ� Ȯ�� �ϼ���.");
			throw new Exception("ID factory �� �ʱ�ȭ ���� ���߽��ϴ�.");
		}
		
		ThreadPoolManager.getInstance();
		EventDispatcher.getInstance();
		
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("��  ��");
		L2ScriptEngineManager.getInstance();
		
		printSection("��  ��");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("����Ÿ");
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		
		printSection("��  ų");
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillData.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("������");
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
		
		printSection("ĳ����");
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
		
		printSection("Ŭ  ��");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("��  ��");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			PathFinding.getInstance();
		}
		
		printSection("���Ǿ�");
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
		
		printSection("��");
		SiegeScheduleData.getInstance();
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		ManorData.getInstance();
		
		printSection("�ø��Ǿ�");
		Olympiad.getInstance();
		Hero.getInstance();
		
		printSection("�������");
		SevenSigns.getInstance();
		
		// Call to load caches
		printSection("ĳ  ��");
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
		
		printSection("��ũ��Ʈ");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		MerchantPriceConfigTable.getInstance().updateReferences();
		
		try
		{
			_log.info("������Ʈ ��������" + ": ���� ��ũ��Ʈ ȭ���� �ε�:");
			final File scripts = new File(Config.DATAPACK_ROOT, "data/scripts.cfg");
			if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
			{
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
			}
		}
		catch (IOException ioe)
		{
			_log.severe(getClass().getSimpleName() + ": scripts.cfg ȭ���� �ε� ���� ���߽��ϴ�. �ε��� ��ũ��Ʈ�� �����ϴ�!");
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
		
		_log.info("���佺���ڵ鷯: " + AutoSpawnHandler.getInstance().size() + " ���� �ε��!");
		
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
		_log.info(": �������� ���Ӽ��� �޸� " + totalMem + " Mb�� " + freeMem + " Mb�����˴ϴ�. ");
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
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": ���: ��� ��� ������ Ip�� ����Ͽ�: " + e1.getMessage() + "" + e1 + "GameServer bind �ּҰ� �߸� �Ǿ����ϴ�");
				
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
			_selectorThread.start();
			// _log.log(Level.INFO, getClass().getSimpleName() + ": is now listening on: " + Config.GAMESERVER_HOSTNAME + ":" + Config.PORT_GAME);
			_log.log(Level.INFO, " ������ ������ ���Ӽ��� ��Ʈ" + Config.GAMESERVER_HOSTNAME + ":" + Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": ���: ���� ������ " + e.getMessage() + "" + e + "������ ���� ���߽��ϴ�.");
			System.exit(1);
		}
		
		// _log.log(Level.INFO, getClass().getSimpleName() + ": Maximum numbers of connected players: " + Config.MAXIMUM_ONLINE_USERS);
		// _log.log(Level.INFO, getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		_log.log(Level.INFO, " ������ ���� �ִ� ������� �ο� : " + Config.MAXIMUM_ONLINE_USERS + "��!");
		_log.log(Level.INFO, " ������ ���� ���� ���� �ð���  " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + "�ʳ� �ɷȾ��!");
		
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
		printSection("������");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info("������Ʈ ��������" + ": ������ ������ �ڳ� ���� ��Ȱ��!");
			printSection("��  ��");
			_log.info("������Ʈ ��������: ������(Police, Polis)");
			_log.info("������Ʈ �������� �ҽ��� l2j�� �������̺��� ������� �����Ǿ����ϴ�.");
			_log.info("������Ʈ �������� �������� ����������� ����� �� ������, ����Ʈ�¿��� ����� �������Դϴ�.");
			_log.info("-----------------------------------------------------------");
			_log.info("Ŀ�´�Ƽ ����Ʈ1: http://polis.esy.es/");
			_log.info("Ŀ�´�Ƽ ����Ʈ2: http://��õ�޽��ϴ�. ^^");
			_log.info("Ŀ�´�Ƽ ����Ʈ3: http://��õ�޽��ϴ�. ^^");
			_log.info("������Ʈ ������ ����Ʈ��: policecorps@nate.com");
			_log.info("������Ʈ ������ ����: 2017�� 2�� 21��");
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
