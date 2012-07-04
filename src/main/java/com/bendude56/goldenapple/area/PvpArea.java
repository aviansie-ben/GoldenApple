package com.bendude56.goldenapple.area;

import org.bukkit.Location;

public class PvpArea extends ParentArea {
	private LootAction lootAction = LootAction.KEEPALL;
	
	public PvpArea(Long ID, Location corner1, Location corner2, boolean ignoreY) {
		super(ID, corner1, corner2, ignoreY);
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