package com.bendude56.goldenapple.area;

import org.bukkit.Location;

public class PvpArea extends ParentArea {
	private LootAction lootAction = LootAction.KEEPALL;
	
	public PvpArea(Location corner1, Location corner2, boolean ignoreY) {
		this.setCorner1(corner1);
		this.setCorner2(corner2);
		this.ignoreY(ignoreY);
	}
	
	public void setLootAction(LootAction lootAction) {
		this.lootAction = lootAction;
	}
	
	public LootAction getLootAction() {
		return this.lootAction;
	}
	
	public enum LootAction {
		DROPALL, DROPMONEY, KEEPALL;
	}
}