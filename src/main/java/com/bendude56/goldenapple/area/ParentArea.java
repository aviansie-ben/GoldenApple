package com.bendude56.goldenapple.area;

import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;

/**
 * This class is the parent of all Area subclasses who support ChildAreas.
 * 
 * @author Deaboy
 */
public class ParentArea extends Area {
	public ParentArea(Long ID, Location corner1, Location corner2, boolean ignoreY){
		super(ID, corner1, corner2, ignoreY);
	}
	
	public List<ChildArea> getChildren() {
		return GoldenApple.getInstance().areas.getChildren(this);
	}
	public void sortChildren(){
		GoldenApple.getInstance().areas.sortChildren(this);
	}
}