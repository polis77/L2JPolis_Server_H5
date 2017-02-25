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

import java.util.logging.Logger;

import com.polis.gameserver.enums.InstanceType;
import com.polis.gameserver.model.L2Object;
import com.polis.gameserver.model.actor.instance.L2PcInstance;

public interface IActionHandler
{
	public static Logger _log = Logger.getLogger(IActionHandler.class.getName());
	
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact);
	
	public InstanceType getInstanceType();
}