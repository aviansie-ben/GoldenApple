package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.List;

import com.bendude56.goldenapple.GoldenApple;

/**
 * This class is the parent of all classes who support child classes.
 * 
 * @author Deaboy
 */
public class ParentArea extends Area {
	private List<Long> childrenIDs = new ArrayList<Long>();
	
	public void addChild(ChildArea child) {
		if (!childrenIDs.contains(child.getID())) {
			childrenIDs.add(child.getID());
			child.setParent(this);
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