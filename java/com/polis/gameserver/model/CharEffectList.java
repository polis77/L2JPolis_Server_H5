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
package com.polis.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.polis.Config;
import com.polis.gameserver.model.actor.L2Character;
import com.polis.gameserver.model.actor.L2Summon;
import com.polis.gameserver.model.actor.instance.L2PcInstance;
import com.polis.gameserver.model.effects.AbstractEffect;
import com.polis.gameserver.model.effects.EffectFlag;
import com.polis.gameserver.model.effects.L2EffectType;
import com.polis.gameserver.model.olympiad.OlympiadGameManager;
import com.polis.gameserver.model.olympiad.OlympiadGameTask;
import com.polis.gameserver.model.skills.AbnormalType;
import com.polis.gameserver.model.skills.BuffInfo;
import com.polis.gameserver.model.skills.EffectScope;
import com.polis.gameserver.model.skills.Skill;
import com.polis.gameserver.network.serverpackets.AbnormalStatusUpdate;
import com.polis.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import com.polis.gameserver.network.serverpackets.PartySpelled;
import com.polis.gameserver.network.serverpackets.ShortBuffStatusUpdate;

/**
 * Effect lists.<br>
 * Holds all the buff infos that are affecting a character.<br>
 * Manages the logic that controls whether a buff is added, remove, replaced or set inactive.<br>
 * Uses maps with skill ID as key and buff info DTO as value to avoid iterations.<br>
 * Uses Double-Checked Locking to avoid useless initialization and synchronization issues and overhead.<br>
 * Methods may resemble List interface, although it doesn't implement such interface.
 * @author Zoey76
 */
public final class CharEffectList
{
	private static final Logger _log = Logger.getLogger(CharEffectList.class.getName());
	/** Map containing all effects from buffs for this effect list. */
	private volatile FastMap<Integer, BuffInfo> _buffs;
	/** Map containing all triggered skills for this effect list. */
	private volatile FastMap<Integer, BuffInfo> _triggered;
	/** Map containing all dances/songs for this effect list. */
	private volatile FastMap<Integer, BuffInfo> _dances;
	/** Map containing all toggle for this effect list. */
	private volatile FastMap<Integer, BuffInfo> _toggles;
	/** Map containing all debuffs for this effect list. */
	private volatile FastMap<Integer, BuffInfo> _debuffs;
	/** They bypass most of the actions, they are not included in most operations. */
	private volatile FastMap<Integer, BuffInfo> _passives;
	/** Map containing the all stacked effect in progress for each abnormal type. */
	private volatile Map<AbnormalType, BuffInfo> _stackedEffects;
	/** Set containing all abnormal types that shouldn't be added to this character effect list. */
	private volatile Set<AbnormalType> _blockedBuffSlots = null;
	/** Short buff skill ID. */
	private BuffInfo _shortBuff = null;
	/** If {@code true} this effect list has buffs removed on any action. */
	private volatile boolean _hasBuffsRemovedOnAnyAction = false;
	/** If {@code true} this effect list has buffs removed on damage. */
	private volatile boolean _hasBuffsRemovedOnDamage = false;
	/** If {@code true} this effect list has debuffs removed on damage. */
	private volatile boolean _hasDebuffsRemovedOnDamage = false;
	/** Effect flags. */
	private int _effectFlags;
	/** If {@code true} only party icons need to be updated. */
	private boolean _partyOnly = false;
	/** The owner of this effect list. */
	private final L2Character _owner;
	/** Hidden buffs count, prevents iterations. */
	private final AtomicInteger _hiddenBuffs = new AtomicInteger();
	
	/**
	 * Constructor for effect list.
	 * @param owner the character that owns this effect list
	 */
	public CharEffectList(L2Character owner)
	{
		_owner = owner;
	}
	
	/**
	 * Gets buff skills.
	 * @return the buff skills
	 */
	public Map<Integer, BuffInfo> getBuffs()
	{
		if (_buffs == null)
		{
			synchronized (this)
			{
				if (_buffs == null)
				{
					_buffs = new FastMap<>();
					_buffs.shared();
				}
			}
		}
		return _buffs;
	}
	
	/**
	 * Gets triggered skill skills.
	 * @return the triggered skill skills
	 */
	public Map<Integer, BuffInfo> getTriggered()
	{
		if (_triggered == null)
		{
			synchronized (this)
			{
				if (_triggered == null)
				{
					_triggered = new FastMap<>();
					_triggered.shared();
				}
			}
		}
		return _triggered;
	}
	
	/**
	 * Gets dance/song skills.
	 * @return the dance/song skills
	 */
	public Map<Integer, BuffInfo> getDances()
	{
		if (_dances == null)
		{
			synchronized (this)
			{
				if (_dances == null)
				{
					_dances = new FastMap<>();
					_dances.shared();
				}
			}
		}
		return _dances;
	}
	
	/**
	 * Gets toggle skills.
	 * @return the toggle skills
	 */
	public Map<Integer, BuffInfo> getToggles()
	{
		if (_toggles == null)
		{
			synchronized (this)
			{
				if (_toggles == null)
				{
					_toggles = new FastMap<>();
					_toggles.shared();
				}
			}
		}
		return _toggles;
	}
	
	/**
	 * Gets debuff skills.
	 * @return the debuff skills
	 */
	public Map<Integer, BuffInfo> getDebuffs()
	{
		if (_debuffs == null)
		{
			synchronized (this)
			{
				if (_debuffs == null)
				{
					_debuffs = new FastMap<>();
					_debuffs.shared();
				}
			}
		}
		return _debuffs;
	}
	
	/**
	 * Gets passive skills.
	 * @return the passive skills
	 */
	public Map<Integer, BuffInfo> getPassives()
	{
		if (_passives == null)
		{
			synchronized (this)
			{
				if (_passives == null)
				{
					_passives = new FastMap<>();
					_passives.shared();
				}
			}
		}
		return _passives;
	}
	
	/**
	 * Gets all the effects on this effect list.
	 * @return all the effects on this effect list
	 */
	public List<BuffInfo> getEffects()
	{
		if (isEmpty())
		{
			return Collections.<BuffInfo> emptyList();
		}
		
		final List<BuffInfo> buffs = new ArrayList<>();
		if (hasBuffs())
		{
			buffs.addAll(getBuffs().values());
		}
		
		if (hasTriggered())
		{
			buffs.addAll(getTriggered().values());
		}
		
		if (hasDances())
		{
			buffs.addAll(getDances().values());
		}
		
		if (hasToggles())
		{
			buffs.addAll(getToggles().values());
		}
		
		if (hasDebuffs())
		{
			buffs.addAll(getDebuffs().values());
		}
		return buffs;
	}
	
	/**
	 * Gets the effect list where the skill effects should be.
	 * @param skill the skill
	 * @return the effect list
	 */
	private Map<Integer, BuffInfo> getEffectList(Skill skill)
	{
		if (skill == null)
		{
			return null;
		}
		
		final Map<Integer, BuffInfo> effects;
		if (skill.isPassive())
		{
			effects = getPassives();
		}
		else if (skill.isDebuff())
		{
			effects = getDebuffs();
		}
		else if (skill.isTriggeredSkill())
		{
			effects = getTriggered();
		}
		else if (skill.isDance())
		{
			effects = getDances();
		}
		else if (skill.isToggle())
		{
			effects = getToggles();
		}
		else
		{
			effects = getBuffs();
		}
		return effects;
	}
	
	/**
	 * Gets the first effect for the given effect type.<br>
	 * Prevents initialization.<br>
	 * TODO: Remove this method after all the effect types gets replaced by abnormal skill types.
	 * @param type the effect type
	 * @return the first effect matching the given effect type
	 */
	public BuffInfo getFirstEffect(L2EffectType type)
	{
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							return info;
						}
					}
				}
			}
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							return info;
						}
					}
				}
			}
		}
		
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							return info;
						}
					}
				}
			}
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							return info;
						}
					}
				}
			}
		}
		
		if (hasDebuffs())
		{
			for (BuffInfo info : getDebuffs().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							return info;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Verifies if this effect list contains the given skill ID.<br>
	 * Prevents initialization.
	 * @param skillId the skill ID to verify
	 * @return {@code true} if the skill ID is present in the effect list, {@code false} otherwise
	 */
	public boolean isAffectedBySkill(int skillId)
	{
		return (hasBuffs() && getBuffs().containsKey(skillId)) || (hasDebuffs() && getDebuffs().containsKey(skillId)) || (hasTriggered() && getTriggered().containsKey(skillId)) || (hasDances() && getDances().containsKey(skillId)) || (hasToggles() && getToggles().containsKey(skillId)) || (hasPassives() && getPassives().containsKey(skillId));
	}
	
	/**
	 * Gets the buff info by skill ID.<br>
	 * Prevents initialization.
	 * @param skillId the skill ID
	 * @return the buff info
	 */
	public BuffInfo getBuffInfoBySkillId(int skillId)
	{
		BuffInfo info = null;
		if (isAffectedBySkill(skillId))
		{
			if (hasBuffs() && getBuffs().containsKey(skillId))
			{
				info = getBuffs().get(skillId);
			}
			else if (hasTriggered() && getTriggered().containsKey(skillId))
			{
				info = getTriggered().get(skillId);
			}
			else if (hasDances() && getDances().containsKey(skillId))
			{
				info = getDances().get(skillId);
			}
			else if (hasToggles() && getToggles().containsKey(skillId))
			{
				info = getToggles().get(skillId);
			}
			else if (hasDebuffs() && getDebuffs().containsKey(skillId))
			{
				info = getDebuffs().get(skillId);
			}
			else if (hasPassives() && getPassives().containsKey(skillId))
			{
				info = getPassives().get(skillId);
			}
		}
		return info;
	}
	
	/**
	 * Gets a buff info by abnormal type.<br>
	 * It's O(1) for every buff in this effect list.
	 * @param type the abnormal skill type
	 * @return the buff info if it's present, {@code null} otherwise
	 */
	public BuffInfo getBuffInfoByAbnormalType(AbnormalType type)
	{
		return (_stackedEffects != null) ? _stackedEffects.get(type) : null;
	}
	
	/**
	 * Adds abnormal types to the blocked buff slot set.
	 * @param blockedBuffSlots the blocked buff slot set to add
	 */
	public void addBlockedBuffSlots(Set<AbnormalType> blockedBuffSlots)
	{
		if (_blockedBuffSlots == null)
		{
			synchronized (this)
			{
				if (_blockedBuffSlots == null)
				{
					_blockedBuffSlots = new CopyOnWriteArraySet<>();
				}
			}
		}
		_blockedBuffSlots.addAll(blockedBuffSlots);
	}
	
	/**
	 * Removes abnormal types from the blocked buff slot set.
	 * @param blockedBuffSlots the blocked buff slot set to remove
	 * @return {@code true} if the blocked buff slots set has been modified, {@code false} otherwise
	 */
	public boolean removeBlockedBuffSlots(Set<AbnormalType> blockedBuffSlots)
	{
		if (_blockedBuffSlots != null)
		{
			return _blockedBuffSlots.removeAll(blockedBuffSlots);
		}
		return false;
	}
	
	/**
	 * Gets all the blocked abnormal types for this character effect list.
	 * @return the current blocked buff slots set
	 */
	public Set<AbnormalType> getAllBlockedBuffSlots()
	{
		return _blockedBuffSlots;
	}
	
	/**
	 * Gets the Short Buff info.
	 * @return the short buff info
	 */
	public BuffInfo getShortBuff()
	{
		return _shortBuff;
	}
	
	/**
	 * Sets the Short Buff data and sends an update if the effected is a player.
	 * @param info the buff info
	 */
	public void shortBuffStatusUpdate(BuffInfo info)
	{
		if (_owner.isPlayer())
		{
			_shortBuff = info;
			if (info == null)
			{
				_owner.sendPacket(ShortBuffStatusUpdate.RESET_SHORT_BUFF);
			}
			else
			{
				_owner.sendPacket(new ShortBuffStatusUpdate(info.getSkill().getId(), info.getSkill().getLevel(), info.getTime()));
			}
		}
	}
	
	/**
	 * Checks if the given skill stacks with an existing one.
	 * @param skill the skill to verify
	 * @return {@code true} if this effect stacks with the given skill, {@code false} otherwise
	 */
	private boolean doesStack(Skill skill)
	{
		final AbnormalType type = skill.getAbnormalType();
		if (type.isNone() || isEmpty())
		{
			return false;
		}
		
		final Map<Integer, BuffInfo> effects = getEffectList(skill);
		if ((effects == null) || effects.isEmpty())
		{
			return false;
		}
		
		for (BuffInfo info : effects.values())
		{
			if ((info != null) && (info.getSkill().getAbnormalType() == type))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the buffs count without including the hidden buffs (after getting an Herb buff).<br>
	 * Prevents initialization.
	 * @return the number of buffs in this character effect list
	 */
	public int getBuffCount()
	{
		return hasBuffs() ? getBuffs().size() - _hiddenBuffs.get() - (getShortBuff() != null ? 1 : 0) : 0;
	}
	
	/**
	 * Gets the Songs/Dances count.<br>
	 * Prevents initialization.
	 * @return the number of Songs/Dances in this character effect list
	 */
	public int getDanceCount()
	{
		return hasDances() ? getDances().size() : 0;
	}
	
	/**
	 * Gets the triggered buffs count.<br>
	 * Prevents initialization.
	 * @return the number of triggered buffs in this character effect list
	 */
	public int getTriggeredBuffCount()
	{
		return hasTriggered() ? getTriggered().size() : 0;
	}
	
	/**
	 * Gets the hidden buff count.
	 * @return the number of hidden buffs
	 */
	public int getHiddenBuffsCount()
	{
		return _hiddenBuffs.get();
	}
	
	/**
	 * Auxiliary method to stop all effects from a buff info and remove it from an effect list and stacked effects.
	 * @param removed {@code true} if the effect is removed, {@code false} otherwise
	 * @param info the buff info
	 * @param effects the effect list
	 */
	private void stopAndRemove(boolean removed, BuffInfo info, Map<Integer, BuffInfo> effects)
	{
		// Stop the buff effects.
		info.stopAllEffects(removed);
		
		// If it's a hidden buff that ends, then decrease hidden buff count.
		if (!info.isInUse())
		{
			_hiddenBuffs.decrementAndGet();
		}
		// Removes the buff from the stack.
		else if (_stackedEffects != null)
		{
			_stackedEffects.remove(info.getSkill().getAbnormalType());
		}
		
		// Removes the buff from the given effect list.
		effects.remove(info.getSkill().getId());
		
		// If it's an herb that ends, check if there are hidden buffs.
		if (info.getSkill().isAbnormalInstant() && hasBuffs())
		{
			for (BuffInfo buff : getBuffs().values())
			{
				if ((buff != null) && (buff.getSkill().getAbnormalType() == info.getSkill().getAbnormalType()) && !buff.isInUse())
				{
					// Sets the buff in use again.
					buff.setInUse(true);
					// Adds the stats.
					buff.addStats();
					// Adds the buff to the stack.
					if (_stackedEffects != null)
					{
						_stackedEffects.put(buff.getSkill().getAbnormalType(), buff);
					}
					// If it's a hidden buff that gets activated, then decrease hidden buff count.
					_hiddenBuffs.decrementAndGet();
					break;
				}
			}
		}
		
		if (!removed)
		{
			info.getSkill().applyEffectScope(EffectScope.END, info, true, false);
		}
	}
	
	/**
	 * Auxiliary method to stop all effects from a buff info and remove it from an effect list and stacked effects.
	 * @param info the buff info
	 * @param effects the effect list
	 */
	private void stopAndRemove(BuffInfo info, Map<Integer, BuffInfo> effects)
	{
		stopAndRemove(true, info, effects);
	}
	
	/**
	 * Exits all effects in this effect list.<br>
	 * Stops all the effects, clear the effect lists and updates the effect flags and icons.
	 */
	public void stopAllEffects()
	{
		boolean update = false;
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if (info != null)
				{
					info.stopAllEffects(false);
				}
			}
			getBuffs().clear();
			_hiddenBuffs.set(0);
			update = true;
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if (info != null)
				{
					info.stopAllEffects(false);
				}
			}
			getTriggered().clear();
			update = true;
		}
		
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if (info != null)
				{
					info.stopAllEffects(false);
				}
			}
			getDances().clear();
			update = true;
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if (info != null)
				{
					info.stopAllEffects(false);
				}
			}
			getToggles().clear();
			update = true;
		}
		
		if (hasDebuffs())
		{
			for (BuffInfo info : getDebuffs().values())
			{
				if (info != null)
				{
					info.stopAllEffects(false);
				}
			}
			getDebuffs().clear();
			update = true;
		}
		
		if (_stackedEffects != null)
		{
			_stackedEffects.clear();
		}
		
		// Update effect flags and icons.
		updateEffectList(update);
	}
	
	/**
	 * Stops all effects in this effect list except debuffs and those that last through death.
	 */
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		boolean update = false;
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if ((info != null) && !info.getSkill().isStayAfterDeath())
				{
					stopAndRemove(info, getBuffs());
					update = true;
				}
			}
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if ((info != null) && !info.getSkill().isStayAfterDeath())
				{
					stopAndRemove(info, getTriggered());
					update = true;
				}
			}
		}
		
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if ((info != null) && !info.getSkill().isStayAfterDeath())
				{
					stopAndRemove(info, getDances());
					update = true;
				}
			}
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if ((info != null) && !info.getSkill().isStayAfterDeath())
				{
					stopAndRemove(info, getToggles());
					update = true;
				}
			}
		}
		
		// Update effect flags and icons.
		updateEffectList(update);
	}
	
	/**
	 * Stop all effects that doesn't stay on sub-class change.
	 */
	public void stopAllEffectsNotStayOnSubclassChange()
	{
		boolean update = false;
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if ((info != null) && !info.getSkill().isStayOnSubclassChange())
				{
					stopAndRemove(info, getBuffs());
					update = true;
				}
			}
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if ((info != null) && !info.getSkill().isStayOnSubclassChange())
				{
					stopAndRemove(info, getTriggered());
					update = true;
				}
			}
		}
		
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if ((info != null) && !info.getSkill().isStayOnSubclassChange())
				{
					stopAndRemove(info, getDances());
					update = true;
				}
			}
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if ((info != null) && !info.getSkill().isStayOnSubclassChange())
				{
					stopAndRemove(info, getToggles());
					update = true;
				}
			}
		}
		
		// Update effect flags and icons.
		updateEffectList(update);
	}
	
	/**
	 * Stops all active toggle skills.
	 */
	public void stopAllToggles()
	{
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if (info != null)
				{
					stopAndRemove(info, getToggles());
				}
			}
			// Update effect flags and icons.
			updateEffectList(true);
		}
	}
	
	/**
	 * Stops all active dances/songs skills.
	 */
	public void stopAllDances()
	{
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if (info != null)
				{
					stopAndRemove(info, getDances());
				}
			}
			// Update effect flags and icons.
			updateEffectList(true);
		}
	}
	
	/**
	 * Exit all effects having a specified type.<br>
	 * TODO: Remove after all effect types are replaced by abnormal skill types.
	 * @param type the type of the effect to stop
	 */
	public void stopEffects(L2EffectType type)
	{
		boolean update = false;
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							stopAndRemove(info, getBuffs());
							update = true;
							break;
						}
					}
				}
			}
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							stopAndRemove(info, getTriggered());
							update = true;
							break;
						}
					}
				}
			}
		}
		
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							stopAndRemove(info, getDances());
							update = true;
							break;
						}
					}
				}
			}
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							stopAndRemove(info, getToggles());
							update = true;
							break;
						}
					}
				}
			}
		}
		
		if (hasDebuffs())
		{
			for (BuffInfo info : getDebuffs().values())
			{
				if (info != null)
				{
					for (AbstractEffect effect : info.getEffects())
					{
						if ((effect != null) && (effect.getEffectType() == type))
						{
							stopAndRemove(info, getDebuffs());
							update = true;
							break;
						}
					}
				}
			}
		}
		
		// Update effect flags and icons.
		updateEffectList(update);
	}
	
	/**
	 * Exits all effects created by a specific skill ID.<br>
	 * Removes the effects from the effect list.<br>
	 * Removes the stats from the character.<br>
	 * Updates the effect flags and icons.<br>
	 * Presents two overloads:<br>
	 * {@link #stopSkillEffects(boolean, Skill)}<br>
	 * {@link #stopSkillEffects(boolean, AbnormalType)}
	 * @param removed {@code true} if the effect is removed, {@code false} otherwise
	 * @param skillId the skill ID
	 */
	public void stopSkillEffects(boolean removed, int skillId)
	{
		final BuffInfo info = getBuffInfoBySkillId(skillId);
		if (info != null)
		{
			stopSkillEffects(removed, info.getSkill());
		}
	}
	
	/**
	 * Exits all effects created by a specific skill.<br>
	 * Removes the effects from the effect list.<br>
	 * Removes the stats from the character.<br>
	 * Updates the effect flags and icons.<br>
	 * Presents two overloads:<br>
	 * {@link #stopSkillEffects(boolean, int)}<br>
	 * {@link #stopSkillEffects(boolean, AbnormalType)}
	 * @param removed {@code true} if the effect is removed, {@code false} otherwise
	 * @param skill the skill
	 */
	public void stopSkillEffects(boolean removed, Skill skill)
	{
		if ((skill == null) || !isAffectedBySkill(skill.getId()))
		{
			return;
		}
		
		final Map<Integer, BuffInfo> effects = getEffectList(skill);
		if (effects != null)
		{
			remove(removed, effects.get(skill.getId()));
		}
	}
	
	/**
	 * Exits all effects created by a specific skill abnormal type.<br>
	 * It's O(1) for every effect in this effect list except passive effects.<br>
	 * Presents two overloads:<br>
	 * {@link #stopSkillEffects(boolean, int)}<br>
	 * {@link #stopSkillEffects(boolean, Skill)}
	 * @param removed {@code true} if the effect is removed, {@code false} otherwise
	 * @param type the skill abnormal type
	 * @return {@code true} if there was a buff info with the given abnormal type
	 */
	public boolean stopSkillEffects(boolean removed, AbnormalType type)
	{
		if (_stackedEffects != null)
		{
			final BuffInfo old = _stackedEffects.remove(type);
			if (old != null)
			{
				stopSkillEffects(removed, old.getSkill());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.<br>
	 * Called on any action except movement (attack, cast).
	 */
	public void stopEffectsOnAction()
	{
		if (_hasBuffsRemovedOnAnyAction)
		{
			boolean update = false;
			if (hasBuffs())
			{
				for (BuffInfo info : getBuffs().values())
				{
					if ((info != null) && info.getSkill().isRemovedOnAnyActionExceptMove())
					{
						stopAndRemove(info, getBuffs());
						update = true;
					}
				}
			}
			
			if (hasTriggered())
			{
				for (BuffInfo info : getTriggered().values())
				{
					if ((info != null) && info.getSkill().isRemovedOnAnyActionExceptMove())
					{
						stopAndRemove(info, getTriggered());
						update = true;
					}
				}
			}
			
			if (hasToggles())
			{
				for (BuffInfo info : getToggles().values())
				{
					if ((info != null) && info.getSkill().isRemovedOnAnyActionExceptMove())
					{
						stopAndRemove(info, getToggles());
						update = true;
					}
				}
			}
			
			if (hasDebuffs())
			{
				for (BuffInfo info : getDebuffs().values())
				{
					if ((info != null) && info.getSkill().isRemovedOnAnyActionExceptMove())
					{
						stopAndRemove(info, getDebuffs());
						update = true;
					}
				}
			}
			
			// Update effect flags and icons.
			updateEffectList(update);
		}
	}
	
	public void stopEffectsOnDamage(boolean awake)
	{
		if (awake)
		{
			boolean update = false;
			if (_hasBuffsRemovedOnDamage)
			{
				if (hasBuffs())
				{
					for (BuffInfo info : getBuffs().values())
					{
						if ((info != null) && info.getSkill().isRemovedOnDamage())
						{
							stopAndRemove(info, getBuffs());
							update = true;
						}
					}
				}
				
				if (hasTriggered())
				{
					for (BuffInfo info : getTriggered().values())
					{
						if ((info != null) && info.getSkill().isRemovedOnDamage())
						{
							stopAndRemove(info, getTriggered());
							update = true;
						}
					}
				}
				
				if (hasToggles())
				{
					for (BuffInfo info : getToggles().values())
					{
						if ((info != null) && info.getSkill().isRemovedOnDamage())
						{
							stopAndRemove(info, getToggles());
							update = true;
						}
					}
				}
			}
			
			if (_hasDebuffsRemovedOnDamage && hasDebuffs())
			{
				for (BuffInfo info : getDebuffs().values())
				{
					if ((info != null) && info.getSkill().isRemovedOnDamage())
					{
						stopAndRemove(info, getDebuffs());
						update = true;
					}
				}
			}
			
			// Update effect flags and icons.
			updateEffectList(update);
		}
	}
	
	/**
	 * @param partyOnly
	 */
	public void updateEffectIcons(boolean partyOnly)
	{
		if (partyOnly)
		{
			_partyOnly = true;
		}
		// Update effect flags and icons.
		updateEffectList(true);
	}
	
	/**
	 * Verify if this effect list is empty.<br>
	 * Prevents initialization.
	 * @return {@code true} if this effect list contains any skills
	 */
	public boolean isEmpty()
	{
		return !hasBuffs() && !hasTriggered() && !hasDances() && !hasDebuffs();
	}
	
	/**
	 * Verify if this effect list has buffs skills.<br>
	 * Prevents initialization.
	 * @return {@code true} if {@link #_buffs} is not {@code null} and is not empty
	 */
	public boolean hasBuffs()
	{
		return (_buffs != null) && !_buffs.isEmpty();
	}
	
	/**
	 * Verify if this effect list has triggered skills.<br>
	 * Prevents initialization.
	 * @return {@code true} if {@link #_triggered} is not {@code null} and is not empty
	 */
	public boolean hasTriggered()
	{
		return (_triggered != null) && !_triggered.isEmpty();
	}
	
	/**
	 * Verify if this effect list has dance/song skills.<br>
	 * Prevents initialization.
	 * @return {@code true} if {@link #_dances} is not {@code null} and is not empty
	 */
	public boolean hasDances()
	{
		return (_dances != null) && !_dances.isEmpty();
	}
	
	/**
	 * Verify if this effect list has toggle skills.<br>
	 * Prevents initialization.
	 * @return {@code true} if {@link #_toggles} is not {@code null} and is not empty
	 */
	public boolean hasToggles()
	{
		return (_toggles != null) && !_toggles.isEmpty();
	}
	
	/**
	 * Verify if this effect list has debuffs skills.<br>
	 * Prevents initialization.
	 * @return {@code true} if {@link #_debuffs} is not {@code null} and is not empty
	 */
	public boolean hasDebuffs()
	{
		return (_debuffs != null) && !_debuffs.isEmpty();
	}
	
	/**
	 * Verify if this effect list has passive skills.<br>
	 * Prevents initialization.
	 * @return {@code true} if {@link #_passives} is not {@code null} and is not empty
	 */
	public boolean hasPassives()
	{
		return (_passives != null) && !_passives.isEmpty();
	}
	
	/**
	 * Executes a procedure for all effects.<br>
	 * Prevents initialization.
	 * @param function the function to execute
	 * @param dances if {@code true} dances/songs will be included
	 * @return {@code true} if the procedure is executed successfully for every element, {@code false} otherwise
	 */
	public boolean forEach(Function<BuffInfo, Boolean> function, boolean dances)
	{
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if (!function.apply(info))
				{
					return false;
				}
			}
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if (!function.apply(info))
				{
					return false;
				}
			}
		}
		
		if (dances && hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if (!function.apply(info))
				{
					return false;
				}
			}
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if (!function.apply(info))
				{
					return false;
				}
			}
		}
		
		if (hasDebuffs())
		{
			for (BuffInfo info : getDebuffs().values())
			{
				if (!function.apply(info))
				{
					return false;
				}
			}
		}
		
		// Update effect flags and icons.
		updateEffectList(true);
		return true;
	}
	
	/**
	 * Removes a set of effects from this effect list.
	 * @param removed {@code true} if the effect is removed, {@code false} otherwise
	 * @param info the effects to remove
	 */
	public void remove(boolean removed, BuffInfo info)
	{
		if ((info == null) || !isAffectedBySkill(info.getSkill().getId()))
		{
			return;
		}
		
		// Remove the effect from character effects.
		stopAndRemove(removed, info, getEffectList(info.getSkill()));
		// Update effect flags and icons.
		updateEffectList(true);
	}
	
	/**
	 * Adds a set of effects to this effect list.
	 * @param info the buff info
	 */
	public void add(BuffInfo info)
	{
		if (info == null)
		{
			return;
		}
		
		// Support for blocked buff slots.
		final Skill skill = info.getSkill();
		if ((_blockedBuffSlots != null) && _blockedBuffSlots.contains(skill.getAbnormalType()))
		{
			return;
		}
		
		// Passive effects are treated specially
		if (skill.isPassive())
		{
			// Passive effects don't need stack type!
			if (!skill.getAbnormalType().isNone())
			{
				_log.warning("Passive " + skill + " with abnormal type: " + skill.getAbnormalType() + "!");
			}
			
			// Check for passive skill conditions.
			if (!skill.checkCondition(info.getEffector(), info.getEffected(), false))
			{
				return;
			}
			
			// Puts the effects in the list.
			final BuffInfo infoToRemove = getPassives().put(skill.getId(), info);
			if (infoToRemove != null)
			{
				// Removes the old stats from the character if the skill was present.
				infoToRemove.setInUse(false);
				infoToRemove.removeStats();
			}
			
			// Initialize effects.
			info.initializeEffects();
			return;
		}
		
		// The old effect is removed using Map#remove(key) instead of Map#put(key, value) (that would be the wisest choice),
		// Because order matters and put method would insert in the same place it was before, instead of, at the end of the effect list
		// Where new buff should be placed
		if (skill.getAbnormalType().isNone())
		{
			stopSkillEffects(false, skill);
		}
		// Verify stacked skills.
		else
		{
			if (_stackedEffects == null)
			{
				synchronized (this)
				{
					if (_stackedEffects == null)
					{
						_stackedEffects = new ConcurrentHashMap<>();
					}
				}
			}
			
			if (_stackedEffects.containsKey(skill.getAbnormalType()))
			{
				BuffInfo stackedInfo = _stackedEffects.get(skill.getAbnormalType());
				// Skills are only replaced if the incoming buff has greater or equal abnormal level.
				if ((stackedInfo != null) && (skill.getAbnormalLvl() >= stackedInfo.getSkill().getAbnormalLvl()))
				{
					// If it is an herb, set as not in use the lesser buff.
					// Effect will be present in the effect list.
					// Effects stats are removed and onActionTime() is not called.
					// But finish task continues to run, and ticks as well.
					if (skill.isAbnormalInstant())
					{
						if (stackedInfo.getSkill().isAbnormalInstant())
						{
							stopSkillEffects(false, skill.getAbnormalType());
							stackedInfo = _stackedEffects.get(skill.getAbnormalType());
						}
						
						if (stackedInfo != null)
						{
							stackedInfo.setInUse(false);
							// Remove stats
							stackedInfo.removeStats();
							_hiddenBuffs.incrementAndGet();
						}
					}
					// Remove buff that will stack with the abnormal type.
					else
					{
						if (stackedInfo.getSkill().isAbnormalInstant())
						{
							stopSkillEffects(false, skill.getAbnormalType());
						}
						stopSkillEffects(false, skill.getAbnormalType());
					}
				}
				// If the new buff is a lesser buff, then don't add it.
				else
				{
					return;
				}
			}
			_stackedEffects.put(skill.getAbnormalType(), info);
		}
		
		// Select the map that holds the effects related to this skill.
		final Map<Integer, BuffInfo> effects = getEffectList(skill);
		// Remove first buff when buff list is full.
		if (!skill.isDebuff() && !skill.isToggle() && !skill.is7Signs() && !doesStack(skill))
		{
			int buffsToRemove = -1;
			if (skill.isDance())
			{
				buffsToRemove = getDanceCount() - Config.DANCES_MAX_AMOUNT;
			}
			else if (skill.isTriggeredSkill())
			{
				buffsToRemove = getTriggeredBuffCount() - Config.TRIGGERED_BUFFS_MAX_AMOUNT;
			}
			else if (!skill.isHealingPotionSkill())
			{
				buffsToRemove = getBuffCount() - _owner.getStat().getMaxBuffCount();
			}
			
			for (Entry<Integer, BuffInfo> entry : effects.entrySet())
			{
				if (buffsToRemove < 0)
				{
					break;
				}
				
				if (!entry.getValue().isInUse())
				{
					continue;
				}
				
				stopAndRemove(entry.getValue(), effects);
				
				buffsToRemove--;
			}
		}
		
		// After removing old buff (same ID) or stacked buff (same abnormal type),
		// Add the buff to the end of the effect list.
		effects.put(skill.getId(), info);
		// Initialize effects.
		info.initializeEffects();
		// Update effect flags and icons.
		updateEffectList(true);
	}
	
	/**
	 * Update effect icons.<br>
	 * Prevents initialization.
	 */
	private void updateEffectIcons()
	{
		if (_owner == null)
		{
			return;
		}
		
		updateEffectFlags();
		
		if (!_owner.isPlayable())
		{
			return;
		}
		
		AbnormalStatusUpdate asu = null;
		PartySpelled ps = null;
		PartySpelled psSummon = null;
		ExOlympiadSpelledInfo os = null;
		boolean isSummon = false;
		
		if (_owner.isPlayer())
		{
			if (_partyOnly)
			{
				_partyOnly = false;
			}
			else
			{
				asu = new AbnormalStatusUpdate();
			}
			
			if (_owner.isInParty())
			{
				ps = new PartySpelled(_owner);
			}
			
			if (_owner.getActingPlayer().isInOlympiadMode() && _owner.getActingPlayer().isOlympiadStart())
			{
				os = new ExOlympiadSpelledInfo(_owner.getActingPlayer());
			}
		}
		else if (_owner.isSummon())
		{
			isSummon = true;
			ps = new PartySpelled(_owner);
			psSummon = new PartySpelled(_owner);
		}
		
		// Buffs.
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if (info.getSkill().isHealingPotionSkill())
				{
					shortBuffStatusUpdate(info);
				}
				else
				{
					addIcon(info, asu, ps, psSummon, os, isSummon);
				}
			}
		}
		
		// Triggered buffs.
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				addIcon(info, asu, ps, psSummon, os, isSummon);
			}
		}
		
		// Songs and dances.
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				addIcon(info, asu, ps, psSummon, os, isSummon);
			}
		}
		
		// Songs and dances.
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				addIcon(info, asu, ps, psSummon, os, isSummon);
			}
		}
		
		// Debuffs.
		if (hasDebuffs())
		{
			for (BuffInfo info : getDebuffs().values())
			{
				addIcon(info, asu, ps, psSummon, os, isSummon);
			}
		}
		
		if (asu != null)
		{
			_owner.sendPacket(asu);
		}
		
		if (ps != null)
		{
			if (_owner.isSummon())
			{
				final L2PcInstance summonOwner = ((L2Summon) _owner).getOwner();
				if (summonOwner != null)
				{
					if (summonOwner.isInParty())
					{
						summonOwner.getParty().broadcastToPartyMembers(summonOwner, psSummon); // send to all member except summonOwner
						summonOwner.sendPacket(ps); // now send to summonOwner
					}
					else
					{
						summonOwner.sendPacket(ps);
					}
				}
			}
			else if (_owner.isPlayer() && _owner.isInParty())
			{
				_owner.getParty().broadcastPacket(ps);
			}
		}
		
		if (os != null)
		{
			final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(_owner.getActingPlayer().getOlympiadGameId());
			if ((game != null) && game.isBattleStarted())
			{
				game.getZone().broadcastPacketToObservers(os);
			}
		}
	}
	
	private void addIcon(BuffInfo info, AbnormalStatusUpdate asu, PartySpelled ps, PartySpelled psSummon, ExOlympiadSpelledInfo os, boolean isSummon)
	{
		// Avoid null and not in use buffs.
		if ((info == null) || !info.isInUse())
		{
			return;
		}
		
		final Skill skill = info.getSkill();
		if (asu != null)
		{
			asu.addSkill(info);
		}
		
		if ((ps != null) && (isSummon || !skill.isToggle()))
		{
			ps.addSkill(info);
		}
		
		if ((psSummon != null) && !skill.isToggle())
		{
			psSummon.addSkill(info);
		}
		
		if (os != null)
		{
			os.addSkill(info);
		}
	}
	
	/**
	 * Wrapper to update abnormal icons and effect flags.
	 * @param update if {@code true} performs an update
	 */
	private void updateEffectList(boolean update)
	{
		if (update)
		{
			updateEffectIcons();
			computeEffectFlags();
		}
	}
	
	/**
	 * Updates effect flags.<br>
	 * TODO: Rework it to update in real time (add/remove/stop/activate/deactivate operations) and avoid iterations.
	 */
	private void updateEffectFlags()
	{
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if (info == null)
				{
					continue;
				}
				
				if (info.getSkill().isRemovedOnAnyActionExceptMove())
				{
					_hasBuffsRemovedOnAnyAction = true;
				}
				
				if (info.getSkill().isRemovedOnDamage())
				{
					_hasBuffsRemovedOnDamage = true;
				}
			}
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if (info == null)
				{
					continue;
				}
				
				if (info.getSkill().isRemovedOnAnyActionExceptMove())
				{
					_hasBuffsRemovedOnAnyAction = true;
				}
				
				if (info.getSkill().isRemovedOnDamage())
				{
					_hasBuffsRemovedOnDamage = true;
				}
			}
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if (info == null)
				{
					continue;
				}
				
				if (info.getSkill().isRemovedOnAnyActionExceptMove())
				{
					_hasBuffsRemovedOnAnyAction = true;
				}
				
				if (info.getSkill().isRemovedOnDamage())
				{
					_hasBuffsRemovedOnDamage = true;
				}
			}
		}
		
		if (hasDebuffs())
		{
			for (BuffInfo info : getDebuffs().values())
			{
				if ((info != null) && info.getSkill().isRemovedOnDamage())
				{
					_hasDebuffsRemovedOnDamage = true;
				}
			}
		}
	}
	
	/**
	 * Recalculate effect bits flag.<br>
	 * TODO: Rework to update in real time and avoid iterations.
	 */
	private void computeEffectFlags()
	{
		int flags = 0;
		if (hasBuffs())
		{
			for (BuffInfo info : getBuffs().values())
			{
				if (info != null)
				{
					for (AbstractEffect e : info.getEffects())
					{
						flags |= e.getEffectFlags();
					}
				}
			}
		}
		
		if (hasTriggered())
		{
			for (BuffInfo info : getTriggered().values())
			{
				if (info != null)
				{
					for (AbstractEffect e : info.getEffects())
					{
						flags |= e.getEffectFlags();
					}
				}
			}
		}
		
		if (hasDances())
		{
			for (BuffInfo info : getDances().values())
			{
				if (info != null)
				{
					for (AbstractEffect e : info.getEffects())
					{
						flags |= e.getEffectFlags();
					}
				}
			}
		}
		
		if (hasToggles())
		{
			for (BuffInfo info : getToggles().values())
			{
				if (info != null)
				{
					for (AbstractEffect e : info.getEffects())
					{
						flags |= e.getEffectFlags();
					}
				}
			}
		}
		
		if (hasDebuffs())
		{
			for (BuffInfo info : getDebuffs().values())
			{
				if (info != null)
				{
					for (AbstractEffect e : info.getEffects())
					{
						flags |= e.getEffectFlags();
					}
				}
			}
		}
		_effectFlags = flags;
	}
	
	/**
	 * Check if target is affected with special buff
	 * @param flag of special buff
	 * @return boolean true if affected
	 */
	public boolean isAffected(EffectFlag flag)
	{
		return (_effectFlags & flag.getMask()) != 0;
	}
}
