package com.bendude56.goldenapple.area;

import org.bukkit.Location;

import com.bendude56.goldenapple.area.AreaManager.AreaType;

public class PvpArea extends ParentArea implements IArea
{
	private LootAction lootAction = LootAction.KEEPALL;
	
	public PvpArea(Long ID, Location corner1, Location corner2, boolean ignoreY) {
		super(ID, AreaType.PVP, corner1, corner2, ignoreY);
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
	
	public AreaType getType()
	{
		return AreaType.PVP;
	}
}