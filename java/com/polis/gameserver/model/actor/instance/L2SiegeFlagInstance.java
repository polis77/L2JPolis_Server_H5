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

import com.polis.gameserver.ThreadPoolManager;
import com.polis.gameserver.ai.CtrlIntention;
import com.polis.gameserver.enums.InstanceType;
import com.polis.gameserver.instancemanager.CHSiegeManager;
import com.polis.gameserver.instancemanager.FortSiegeManager;
import com.polis.gameserver.instancemanager.SiegeManager;
import com.polis.gameserver.instancemanager.TerritoryWarManager;
import com.polis.gameserver.model.L2Clan;
import com.polis.gameserver.model.L2SiegeClan;
import com.polis.gameserver.model.actor.L2Character;
import com.polis.gameserver.model.actor.L2Npc;
import com.polis.gameserver.model.actor.status.SiegeFlagStatus;
import com.polis.gameserver.model.actor.templates.L2NpcTemplate;
import com.polis.gameserver.model.entity.Siegable;
import com.polis.gameserver.model.skills.Skill;
import com.polis.gameserver.network.SystemMessageId;
import com.polis.gameserver.network.serverpackets.ActionFailed;
import com.polis.gameserver.network.serverpackets.SystemMessage;

public class L2SiegeFlagInstance extends L2Npc
{
	private L2Clan _clan;
	private L2PcInstance _player;
	private Siegable _siege;
	private final boolean _isAdvanced;
	private boolean _canTalk;
	
	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template, boolean advanced, boolean outPost)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2SiegeFlagInstance);
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			_clan = player.getClan();
			_player = player;
			_canTalk = false;
			if (_clan == null)
			{
				deleteMe();
			}
			if (outPost)
			{
				_isAdvanced = false;
				setIsInvul(true);
			}
			else
			{
				_isAdvanced = advanced;
				setIsInvul(false);
			}
			getStatus();
			return;
		}
		_clan = player.getClan();
		_player = player;
		_canTalk = true;
		_siege = SiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		if (_siege == null)
		{
			_siege = FortSiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		}
		if (_siege == null)
		{
			_siege = CHSiegeManager.getInstance().getSiege(player);
		}
		if ((_clan == null) || (_siege == null))
		{
			throw new NullPointerException(getClass().getSimpleName() + ": Initialization failed.");
		}
		
		L2SiegeClan sc = _siege.getAttackerClan(_clan);
		if (sc == null)
		{
			throw new NullPointerException(getClass().getSimpleName() + ": Cannot find siege clan.");
		}
		
		sc.addFlag(this);
		_isAdvanced = advanced;
		getStatus();
		setIsInvul(false);
	}
	
	@Override
	public boolean canBeAttacked()
	{
		return !isInvul();
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !isInvul();
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if ((_siege != null) && (_clan != null))
		{
			L2SiegeClan sc = _siege.getAttackerClan(_clan);
			if (sc != null)
			{
				sc.removeFlag(this);
			}
		}
		else if (_clan != null)
		{
			TerritoryWarManager.getInstance().removeClanFlag(_clan);
		}
		return true;
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if ((player == null) || !canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && (Math.abs(player.getZ() - getZ()) < 100))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	public boolean isAdvancedHeadquarter()
	{
		return _isAdvanced;
	}
	
	@Override
	public SiegeFlagStatus getStatus()
	{
		return (SiegeFlagStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new SiegeFlagStatus(this));
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if (canTalk())
		{
			if (((getCastle() != null) && getCastle().getSiege().isInProgress()) || ((getFort() != null) && getFort().getSiege().isInProgress()) || ((getConquerableHall() != null) && getConquerableHall().isInSiege()))
			{
				if (_clan != null)
				{
					// send warning to owners of headquarters that theirs base is under attack
					_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			}
		}
	}
	
	private class ScheduleTalkTask implements Runnable
	{
		
		public ScheduleTalkTask()
		{
		}
		
		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}
	
	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}
	
	private boolean canTalk()
	{
		return _canTalk;
	}
}
