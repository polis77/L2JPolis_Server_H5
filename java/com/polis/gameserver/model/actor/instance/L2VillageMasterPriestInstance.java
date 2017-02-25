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
package com.polis.gameserver.model.actor.instance;

import com.polis.gameserver.enums.Race;
import com.polis.gameserver.model.actor.templates.L2NpcTemplate;
import com.polis.gameserver.model.base.ClassType;
import com.polis.gameserver.model.base.PlayerClass;

public final class L2VillageMasterPriestInstance extends L2VillageMasterInstance
{
	public L2VillageMasterPriestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected final boolean checkVillageMasterRace(PlayerClass pclass)
	{
		if (pclass == null)
		{
			return false;
		}
		
		return pclass.isOfRace(Race.HUMAN) || pclass.isOfRace(Race.ELF);
	}
	
	@Override
	protected final boolean checkVillageMasterTeachType(PlayerClass pclass)
	{
		if (pclass == null)
		{
			return false;
		}
		
		return pclass.isOfType(ClassType.Priest);
	}
}