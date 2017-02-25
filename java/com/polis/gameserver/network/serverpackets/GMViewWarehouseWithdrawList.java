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
package com.polis.gameserver.network.serverpackets;

import com.polis.gameserver.model.L2Clan;
import com.polis.gameserver.model.actor.instance.L2PcInstance;
import com.polis.gameserver.model.items.instance.L2ItemInstance;

public class GMViewWarehouseWithdrawList extends AbstractItemPacket
{
	private final L2ItemInstance[] _items;
	private final String _playerName;
	private L2PcInstance _activeChar;
	private final long _money;
	
	public GMViewWarehouseWithdrawList(L2PcInstance cha)
	{
		_activeChar = cha;
		_items = _activeChar.getWarehouse().getItems();
		_playerName = _activeChar.getName();
		_money = _activeChar.getWarehouse().getAdena();
	}
	
	public GMViewWarehouseWithdrawList(L2Clan clan)
	{
		_playerName = clan.getLeaderName();
		_items = clan.getWarehouse().getItems();
		_money = clan.getWarehouse().getAdena();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeS(_playerName);
		writeQ(_money);
		writeH(_items.length);
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
			writeD(item.getObjectId());
		}
	}
}
