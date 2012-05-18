package com.bendude56.goldenapple.area;

/**
 * This class inherits the traits of it's parent area.
 * 
 * @author Deaboy
 */
public class ChildArea extends Area {
	private Long parentID;
	private ParentArea parent;
	
	public Long getParentID() {
		return parentID;
	}
	
	public ParentArea getParent() {
		return parent;
	}
	
	public void setParent(ParentArea parent) {
		if (this.parent != parent) {
			this.parent = parent;
		}
		if (!parent.getChildren().contains(this)) {
			parent.addChild(this);
		}
	}
}