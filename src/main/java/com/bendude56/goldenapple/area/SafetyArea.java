package com.bendude56.goldenapple.area;

import org.bukkit.Location;

import com.bendude56.goldenapple.area.AreaManager.AreaType;

public class SafetyArea extends ParentArea implements IArea
{
	boolean allowPvp;
	boolean hostileMobs;
	boolean regenHealth;
	
	public SafetyArea(Long ID, Location corner1, Location corner2, boolean ignoreY) {
		super(ID, AreaType.SAFETY, corner1, corner2, ignoreY);
	}
	
	public boolean allowPvp() {
		return allowPvp; 
	}
	
	public void allowPvp(boolean allowPvp) {
		this.allowPvp = allowPvp;
	}
	
	public boolean hostileMobs() {
		return hostileMobs;
	}
	
	public void hostileMobs(boolean hostileMobs) {
		this.hostileMobs = hostileMobs;
	}
	
	public boolean regenHealth() {
		return regenHealth;
	}
	
	public void regenHealth(boolean regenHealth) {
		this.regenHealth = regenHealth;
	}
	
	public AreaType getType()
	{
		return AreaType.SAFETY;
	}
}