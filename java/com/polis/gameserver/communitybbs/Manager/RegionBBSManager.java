/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.polis.gameserver.communitybbs.Manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.polis.Config;
import com.polis.gameserver.GameServer;
import com.polis.gameserver.datatables.ClassListData;
import com.polis.gameserver.datatables.ExperienceTable;
import com.polis.gameserver.model.BlockList;
import com.polis.gameserver.model.L2World;
import com.polis.gameserver.model.actor.instance.L2PcInstance;
import com.polis.gameserver.model.itemcontainer.Inventory;
import com.polis.gameserver.network.SystemMessageId;
import com.polis.gameserver.network.clientpackets.Say2;
import com.polis.gameserver.network.serverpackets.CreatureSay;
import com.polis.gameserver.network.serverpackets.ShowBoard;
import com.polis.gameserver.network.serverpackets.SystemMessage;
import com.polis.util.StringUtil;

public class RegionBBSManager extends BaseBBSManager
{
	private static final Logger _logChat = Logger.getLogger("chat");
	
	private int _onlineCount = 0;
	private int _onlineCountGm = 0;
	private static Map<Integer, List<L2PcInstance>> _onlinePlayers = new FastMap<Integer, List<L2PcInstance>>().shared();
	private static Map<Integer, Map<String, String>> _communityPages = new FastMap<Integer, Map<String, String>>().shared();
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsloc"))
		{
			showOldCommunity(activeChar, 1);
		}
		else if (command.startsWith("_bbsloc;page;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int page = 0;
			try
			{
				page = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException nfe)
			{
			}
			
			showOldCommunity(activeChar, page);
		}
		else if (command.startsWith("_bbsloc;playerinfo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String name = st.nextToken();
			
			showOldCommunityPI(activeChar, name);
		}
		else
		{
			if (Config.COMMUNITY_TYPE == 1)
			{
				showOldCommunity(activeChar, 1);
			}
			else
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
		}
	}
	
	/**
	 * Show old community player info.
	 * @param activeChar the active char
	 * @param name the player name
	 */
	private void showOldCommunityPI(L2PcInstance activeChar, String name)
	{
		final StringBuilder htmlCode = StringUtil.startAppend(1000, "<html><body><br><table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
		L2PcInstance player = L2World.getInstance().getPlayer(name);
		
		if (player != null)
		{
			String sex = "Male";
			if (player.getAppearance().getSex())
			{
				sex = "Female";
			}
			String levelApprox = "low";
			if (player.getLevel() >= 60)
			{
				levelApprox = "very high";
			}
			else if (player.getLevel() >= 40)
			{
				levelApprox = "high";
			}
			else if (player.getLevel() >= 20)
			{
				levelApprox = "medium";
			}
			
			StringUtil.append(htmlCode, "<table border=0><tr><td>", player.getName(), " (", sex, " ", ClassListData.getInstance().getClass(player.getClassId()).getClientCode(), "):</td></tr><tr><td>Level: ", levelApprox, "</td></tr><tr><td><br></td></tr>");
			
			if ((activeChar != null) && (activeChar.isGM() || (player.getObjectId() == activeChar.getObjectId()) || Config.SHOW_LEVEL_COMMUNITYBOARD))
			{
				long nextLevelExp = 0;
				long nextLevelExpNeeded = 0;
				if (player.getLevel() < (ExperienceTable.getInstance().getMaxLevel() - 1))
				{
					nextLevelExp = ExperienceTable.getInstance().getExpForLevel(player.getLevel() + 1);
					nextLevelExpNeeded = nextLevelExp - player.getExp();
				}
				
				StringUtil.append(htmlCode, "<tr><td>Level: ", String.valueOf(player.getLevel()), "</td></tr><tr><td>Experience: ", String.valueOf(player.getExp()), "/", String.valueOf(nextLevelExp), "</td></tr><tr><td>Experience needed for level up: ", String.valueOf(nextLevelExpNeeded), "</td></tr><tr><td><br></td></tr>");
			}
			
			int uptime = (int) player.getUptime() / 1000;
			int h = uptime / 3600;
			int m = (uptime - (h * 3600)) / 60;
			int s = ((uptime - (h * 3600)) - (m * 60));
			
			StringUtil.append(htmlCode, "<tr><td>Uptime: ", String.valueOf(h), "h ", String.valueOf(m), "m ", String.valueOf(s), "s</td></tr><tr><td><br></td></tr>");
			
			if (player.getClan() != null)
			{
				StringUtil.append(htmlCode, "<tr><td>Clan: ", player.getClan().getName(), "</td></tr><tr><td><br></td></tr>");
			}
			
			StringUtil.append(htmlCode, "<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM ", player.getName(), " pm pm pm\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass _bbsloc\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></td></tr></table></body></html>");
			separateAndSend(htmlCode.toString(), activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard(StringUtil.concat("<html><body><br><br><center>No player with name ", name, "</center><br><br></body></html>"), "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	 * Show old community.
	 * @param activeChar the active char
	 * @param page the page
	 */
	private void showOldCommunity(L2PcInstance activeChar, int page)
	{
		separateAndSend(getCommunityPage(page, activeChar.isGM() ? "gm" : "pl"), activeChar);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		if (ar1.equals("PM"))
		{
			final StringBuilder htmlCode = StringUtil.startAppend(500, "<html><body><br><table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
			
			try
			{
				
				L2PcInstance receiver = L2World.getInstance().getPlayer(ar2);
				if (receiver == null)
				{
					StringUtil.append(htmlCode, "Player not found!<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;", ar2, "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
					return;
				}
				if (Config.JAIL_DISABLE_CHAT && receiver.isJailed())
				{
					activeChar.sendMessage("Player is in jail.");
					return;
				}
				if (receiver.isChatBanned())
				{
					activeChar.sendMessage("Player is chat banned.");
					return;
				}
				if (activeChar.isJailed() && Config.JAIL_DISABLE_CHAT)
				{
					activeChar.sendMessage("You can not chat while in jail.");
					return;
				}
				if (activeChar.isChatBanned())
				{
					activeChar.sendMessage("You are banned from using chat");
					return;
				}
				
				if (Config.LOG_CHAT)
				{
					LogRecord record = new LogRecord(Level.INFO, ar3);
					record.setLoggerName("chat");
					record.setParameters(new Object[]
					{
						"TELL",
						"[" + activeChar.getName() + " to " + receiver.getName() + "]"
					});
					_logChat.log(record);
				}
				CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.TELL, activeChar.getName(), ar3);
				if (!receiver.isSilenceMode(activeChar.getObjectId()) && !BlockList.isBlocked(receiver, activeChar))
				{
					receiver.sendPacket(cs);
					activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), Say2.TELL, "->" + receiver.getName(), ar3));
					StringUtil.append(htmlCode, "Message Sent<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;", receiver.getName(), "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					activeChar.sendPacket(sm);
					parsecmd("_bbsloc;playerinfo;" + receiver.getName(), activeChar);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// ignore
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard(StringUtil.concat("<html><body><br><br><center>the command: ", ar1, " is not implemented yet</center><br><br></body></html>"), "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		
	}
	
	/**
	 * Change community board.
	 */
	public void changeCommunityBoard()
	{
		_onlinePlayers.clear();
		_onlineCount = 0;
		_onlineCountGm = 0;
		
		for (L2PcInstance player : L2World.getInstance().getPlayersSortedBy(Comparator.comparing(L2PcInstance::getName, String::compareToIgnoreCase)))
		{
			addOnlinePlayer(player);
		}
		
		_communityPages.clear();
		writeCommunityPages();
	}
	
	/**
	 * Adds the online player.
	 * @param player the player
	 */
	private void addOnlinePlayer(L2PcInstance player)
	{
		boolean added = false;
		
		for (List<L2PcInstance> page : _onlinePlayers.values())
		{
			if (page.size() < Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				if (!page.contains(player))
				{
					page.add(player);
					if (!player.isInvisible())
					{
						_onlineCount++;
					}
					_onlineCountGm++;
				}
				added = true;
				break;
			}
			else if (page.contains(player))
			{
				added = true;
				break;
			}
		}
		
		if (!added)
		{
			List<L2PcInstance> temp = new ArrayList<>();
			int page = _onlinePlayers.size() + 1;
			if (temp.add(player))
			{
				_onlinePlayers.put(page, temp);
				if (!player.isInvisible())
				{
					_onlineCount++;
				}
				_onlineCountGm++;
			}
		}
	}
	
	/**
	 * Write community pages.
	 */
	private void writeCommunityPages()
	{
		final StringBuilder htmlCode = new StringBuilder(2000);
		final String tdClose = "</td>";
		final String tdOpen = "<td align=left valign=top>";
		final String trClose = "</tr>";
		final String trOpen = "<tr>";
		final String colSpacer = "<td FIXWIDTH=15></td>";
		
		for (int page : _onlinePlayers.keySet())
		{
			Map<String, String> communityPage = new FastMap<>();
			htmlCode.setLength(0);
			StringUtil.append(htmlCode, "<html><body><br><table>" + trOpen + "<td align=left valign=top>Server Restarted: ", String.valueOf(GameServer.dateTimeServerStarted.getTime()), tdClose + trClose + "</table><table>" + trOpen + tdOpen + "XP Rate: x", String.valueOf(Config.RATE_XP), tdClose + colSpacer + tdOpen + "Party XP Rate: x", String.valueOf(Config.RATE_XP * Config.RATE_PARTY_XP), tdClose + colSpacer + tdOpen + "XP Exponent: ", String.valueOf(Config.ALT_GAME_EXPONENT_XP), tdClose + trClose + trOpen + tdOpen + "SP Rate: x", String.valueOf(Config.RATE_SP), tdClose + colSpacer + tdOpen + "Party SP Rate: x", String.valueOf(Config.RATE_SP * Config.RATE_PARTY_SP), tdClose + colSpacer + tdOpen + "SP Exponent: ", String.valueOf(Config.ALT_GAME_EXPONENT_SP), tdClose + trClose + trOpen + tdOpen + "Drop Rate: ", String.valueOf(Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER), tdClose + colSpacer + tdOpen + "Spoil Rate: ", String.valueOf(Config.RATE_CORPSE_DROP_CHANCE_MULTIPLIER), tdClose + colSpacer + tdOpen + "Adena Rate: ", String.valueOf(Config.RATE_DROP_AMOUNT_MULTIPLIER.containsKey(Inventory.ADENA_ID) ? Config.RATE_DROP_AMOUNT_MULTIPLIER.get(Inventory.ADENA_ID).floatValue() : 1f), tdClose + trClose + "</table><table>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + trOpen + tdOpen, String.valueOf(L2World.getInstance().getVisibleObjectsCount()), " Object count</td>" + trClose + trOpen + tdOpen, String.valueOf(getOnlineCount("gm")), " Player(s) Online</td>" + trClose + "</table>");
			
			int cell = 0;
			if (Config.BBS_SHOW_PLAYERLIST)
			{
				htmlCode.append("<table border=0><tr><td><table border=0>");
				
				for (L2PcInstance player : getOnlinePlayers(page))
				{
					cell++;
					
					if (cell == 1)
					{
						htmlCode.append(trOpen);
					}
					
					StringUtil.append(htmlCode, "<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;", player.getName(), "\">");
					
					if (player.isGM())
					{
						StringUtil.append(htmlCode, "<font color=\"LEVEL\">", player.getName(), "</font>");
					}
					else
					{
						htmlCode.append(player.getName());
					}
					
					htmlCode.append("</a></td>");
					
					if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						htmlCode.append(colSpacer);
					}
					
					if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
				if ((cell > 0) && (cell < Config.NAME_PER_ROW_COMMUNITYBOARD))
				{
					htmlCode.append(trClose);
				}
				
				htmlCode.append("</table><br></td></tr>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + "</table>");
			}
			
			if (getOnlineCount("gm") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600><tr>");
				if (page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"Prev\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td align=right width=190><button value=\"Prev\" action=\"bypass _bbsloc;page;", String.valueOf(page - 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				
				StringUtil.append(htmlCode, "<td FIXWIDTH=10></td><td align=center valign=top width=200>Displaying ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + 1), " - ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + getOnlinePlayers(page).size()), " player(s)</td><td FIXWIDTH=10></td>");
				if (getOnlineCount("gm") <= (page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD))
				{
					htmlCode.append("<td width=190><button value=\"Next\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td width=190><button value=\"Next\" action=\"bypass _bbsloc;page;", String.valueOf(page + 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				htmlCode.append("</tr></table>");
			}
			
			htmlCode.append("</body></html>");
			
			communityPage.put("gm", htmlCode.toString());
			
			htmlCode.setLength(0);
			StringUtil.append(htmlCode, "<html><body><br><table>" + trOpen + "<td align=left valign=top>Server Restarted: ", String.valueOf(GameServer.dateTimeServerStarted.getTime()), tdClose + trClose + "</table><table>" + trOpen + tdOpen + "XP Rate: ", String.valueOf(Config.RATE_XP), tdClose + colSpacer + tdOpen + "Party XP Rate: ", String.valueOf(Config.RATE_PARTY_XP), tdClose + colSpacer + tdOpen + "XP Exponent: ", String.valueOf(Config.ALT_GAME_EXPONENT_XP), tdClose + trClose + trOpen + tdOpen + "SP Rate: ", String.valueOf(Config.RATE_SP), tdClose + colSpacer + tdOpen + "Party SP Rate: ", String.valueOf(Config.RATE_PARTY_SP), tdClose + colSpacer + tdOpen + "SP Exponent: ", String.valueOf(Config.ALT_GAME_EXPONENT_SP), tdClose + trClose + trOpen + tdOpen + "Drop Rate: ", String.valueOf(Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER), tdClose + colSpacer + tdOpen + "Spoil Rate: ", String.valueOf(Config.RATE_CORPSE_DROP_CHANCE_MULTIPLIER), tdClose + colSpacer + tdOpen + "Adena Rate: ", String.valueOf(Config.RATE_DROP_AMOUNT_MULTIPLIER.containsKey(Inventory.ADENA_ID) ? Config.RATE_DROP_AMOUNT_MULTIPLIER.get(Inventory.ADENA_ID).floatValue() : 1f), tdClose + trClose + "</table><table>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + trOpen + tdOpen, String.valueOf(getOnlineCount("pl")), " Player(s) Online</td>" + trClose + "</table>");
			
			if (Config.BBS_SHOW_PLAYERLIST)
			{
				htmlCode.append("<table border=0><tr><td><table border=0>");
				
				cell = 0;
				for (L2PcInstance player : getOnlinePlayers(page))
				{
					if ((player == null) || (player.isInvisible()))
					{
						continue; // Go to next
					}
					
					cell++;
					
					if (cell == 1)
					{
						htmlCode.append(trOpen);
					}
					
					StringUtil.append(htmlCode, "<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;", player.getName(), "\">");
					
					if (player.isGM())
					{
						StringUtil.append(htmlCode, "<font color=\"LEVEL\">", player.getName(), "</font>");
					}
					else
					{
						htmlCode.append(player.getName());
					}
					
					htmlCode.append("</a></td>");
					
					if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						htmlCode.append(colSpacer);
					}
					
					if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
				if ((cell > 0) && (cell < Config.NAME_PER_ROW_COMMUNITYBOARD))
				{
					htmlCode.append(trClose);
				}
				
				htmlCode.append("</table><br></td></tr>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + "</table>");
			}
			
			if (getOnlineCount("pl") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600><tr>");
				
				if (page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"Prev\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td align=right width=190><button value=\"Prev\" action=\"bypass _bbsloc;page;", String.valueOf(page - 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				
				StringUtil.append(htmlCode, "<td FIXWIDTH=10></td><td align=center valign=top width=200>Displaying ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + 1), " - ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + getOnlinePlayers(page).size()), " player(s)</td><td FIXWIDTH=10></td>");
				
				if (getOnlineCount("pl") <= (page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD))
				{
					htmlCode.append("<td width=190><button value=\"Next\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td width=190><button value=\"Next\" action=\"bypass _bbsloc;page;", String.valueOf(page + 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				
				htmlCode.append("</tr></table>");
			}
			
			htmlCode.append("</body></html>");
			
			communityPage.put("pl", htmlCode.toString());
			
			_communityPages.put(page, communityPage);
		}
	}
	
	/**
	 * Gets the online count.
	 * @param type the type, Game Masters or normal players
	 * @return the online count
	 */
	private int getOnlineCount(String type)
	{
		if (type.equalsIgnoreCase("gm"))
		{
			return _onlineCountGm;
		}
		
		return _onlineCount;
	}
	
	/**
	 * Gets the online players.
	 * @param page the page
	 * @return the online players
	 */
	private List<L2PcInstance> getOnlinePlayers(int page)
	{
		return _onlinePlayers.get(page);
	}
	
	/**
	 * Gets the community page.
	 * @param page the page
	 * @param type the type
	 * @return the community page
	 */
	public String getCommunityPage(int page, String type)
	{
		if (_communityPages.get(page) != null)
		{
			return _communityPages.get(page).get(type);
		}
		
		return null;
	}
	
	/**
	 * Gets the single instance of RegionBBSManager.
	 * @return single instance of RegionBBSManager
	 */
	public static RegionBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RegionBBSManager _instance = new RegionBBSManager();
	}
}