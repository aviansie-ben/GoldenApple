package com.bendude56.goldenapple.area;

public class SafetyArea extends ParentArea {
	boolean allowPvp;
	boolean hostileMobs;
	boolean regenHealth;
	
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
}