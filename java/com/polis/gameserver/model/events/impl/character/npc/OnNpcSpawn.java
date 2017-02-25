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
package com.polis.gameserver.model.events.impl.character.npc;

import com.polis.gameserver.model.actor.L2Npc;
import com.polis.gameserver.model.events.EventType;
import com.polis.gameserver.model.events.impl.IBaseEvent;

/**
 * @author UnAfraid
 */
public class OnNpcSpawn implements IBaseEvent
{
	private final L2Npc _npc;
	private final boolean _isTeleporting;
	
	public OnNpcSpawn(L2Npc npc, boolean isTeleporting)
	{
		_npc = npc;
		_isTeleporting = isTeleporting;
	}
	
	public L2Npc getNpc()
	{
		return _npc;
	}
	
	public boolean isTeleporting()
	{
		return _isTeleporting;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_SPAWN;
	}
}
