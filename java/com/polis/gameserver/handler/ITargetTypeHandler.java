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
package com.polis.gameserver.handler;

import com.polis.gameserver.model.L2Object;
import com.polis.gameserver.model.actor.L2Character;
import com.polis.gameserver.model.skills.Skill;
import com.polis.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public interface ITargetTypeHandler
{
	static final L2Object[] EMPTY_TARGET_LIST = new L2Object[0];
	
	public L2Object[] getTargetList(Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target);
	
	public Enum<L2TargetType> getTargetType();
}
