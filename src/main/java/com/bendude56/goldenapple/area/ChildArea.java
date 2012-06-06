package com.bendude56.goldenapple.area;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;

/**
 * This class inherits the traits of it's parent area.
 * 
 * @author Deaboy
 */
public class ChildArea extends Area {
	private Long parentID;
	private Long subID;
	
	public ChildArea(Long ID, Location corner1, Location corner2, boolean ignoreY, Long parentID) {
		this.setID(ID);
		this.setCorner1(corner1);
		this.setCorner2(corner2);
		this.ignoreY(ignoreY);
		this.parentID = parentID;
	}
	
	public Long getSubID() {
		return subID;
	}
	
	public void setSubID(Long subID) {
		this.subID = subID;
	}
	
	public Long getParentID() {
		return parentID;
	}
	
	public ParentArea getParent() {
		if (GoldenApple.getInstance().areas.getArea(parentID) != null) {
			return (ParentArea) GoldenApple.getInstance().areas.getArea(parentID);
		} else {
			return null;
		}
	}
	
	public void setParent(ParentArea parent) {
		if (this.parentID != parent.getID()) {
			this.parentID = parent.getID();
		}
	}
}