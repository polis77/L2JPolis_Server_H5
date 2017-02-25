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
package com.polis.gameserver.network.clientpackets;

import com.polis.Config;
import com.polis.gameserver.model.actor.instance.L2PcInstance;
import com.polis.gameserver.network.serverpackets.ExShowContactList;

/**
 * Format: (ch)
 * @author mrTJO & UnAfraid
 */
public final class RequestExShowContactList extends L2GameClientPacket
{
	private static final String _C__D0_86_REQUESTEXSHOWCONTACTLIST = "[C] D0:86 RequestExShowContactList";
	
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.sendPacket(new ExShowContactList(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_86_REQUESTEXSHOWCONTACTLIST;
	}
}