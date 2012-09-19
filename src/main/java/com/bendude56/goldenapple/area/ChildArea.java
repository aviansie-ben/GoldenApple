package com.bendude56.goldenapple.area;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.area.AreaManager.AreaType;

/**
 * This class inherits the traits of it's parent area.
 * 
 * @author Deaboy
 */
public class ChildArea extends Area implements IArea
{
	private final Long parent;
	
	private int index;
	
	public ChildArea(Long ID, Location corner1, Location corner2, boolean ignoreY, ParentArea parent, int index)
	{
		super(ID, AreaType.CHILD, corner1, corner2, ignoreY);
		this.parent = parent.getID();
		this.index = index;
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	public int getIndex()
	{
		return index;
	}
	
	public Long getParentID()
	{
		return parent;
	}
	
	public ParentArea getParent() {
		if (GoldenApple.getInstance().areas.getArea(parent) != null) {
			return (ParentArea) GoldenApple.getInstance().areas.getArea(parent);
		} else {
			return null;
		}
	}

	public AreaType getType()
	{
		return AreaType.CHILD;
	}
}