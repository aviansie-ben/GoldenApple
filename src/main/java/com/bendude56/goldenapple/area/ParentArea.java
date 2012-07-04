package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;

/**
 * This class is the parent of all classes who support child classes.
 * 
 * @author Deaboy
 */
public class ParentArea extends Area {
	private List<Long> childrenIDs = new ArrayList<Long>();
	
	public ParentArea(Long ID, Location corner1, Location corner2, boolean ignoreY){
		super(ID, corner1, corner2, ignoreY);
	}
	
	public void addChild(ChildArea child) {
		if (!childrenIDs.contains(child.getID())) {
			childrenIDs.add(child.getID());
		}
	}
	
	public void removeChild(ChildArea child) {
		if (childrenIDs.contains(child.getID())) {
			childrenIDs.remove(child.getID());
		}
	}
	
	public List<ChildArea> getChildren() {
		List<ChildArea> children = new ArrayList<ChildArea>();
		for(Long childID : childrenIDs) {
			if (GoldenApple.getInstance().areas.getArea(childID) != null) {
				children.add((ChildArea) GoldenApple.getInstance().areas.getArea(childID));
			}
		}
		return children;
	}
}