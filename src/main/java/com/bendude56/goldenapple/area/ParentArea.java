package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.List;

public class ParentArea extends Area {
	private List<ChildArea> children = new ArrayList<ChildArea>();
	
	public void addChild(ChildArea child) {
		if (!children.contains(child)) {
			children.add(child);
			child.setParent(this);
		}
	}
	
	public void removeChild(ChildArea child) {
		if (children.contains(child)) {
			children.remove(child);
		}
	}
	
	public List<ChildArea> getChildren() {
		return children;
	}
}