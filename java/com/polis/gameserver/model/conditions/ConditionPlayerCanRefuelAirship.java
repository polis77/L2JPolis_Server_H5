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
package com.polis.gameserver.model.conditions;

import com.polis.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import com.polis.gameserver.model.actor.instance.L2PcInstance;
import com.polis.gameserver.model.stats.Env;

/**
 * Player Can Refuel Airship condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCanRefuelAirship extends Condition
{
	private final int _val;
	
	public ConditionPlayerCanRefuelAirship(int val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canRefuelAirship = true;
		final L2PcInstance player = env.getPlayer();
		if ((player == null) || (player.getAirShip() == null) || !(player.getAirShip() instanceof L2ControllableAirShipInstance) || ((player.getAirShip().getFuel() + _val) > player.getAirShip().getMaxFuel()))
		{
			canRefuelAirship = false;
		}
		return canRefuelAirship;
	}
}