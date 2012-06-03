package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AreaManager {
	private HashMap<Long, Area> areas;
	
	public void deleteArea(Long AreaID) {
		if (this.areas.containsKey(AreaID)) {
			areas.remove(AreaID);
		}
	}
	
	public void deleteParentArea(Long AreaID) {
		if (!this.areas.containsKey(AreaID)) {
			return;
		}
		if (this.areas.get(AreaID) instanceof ParentArea) {
			ParentArea area = (ParentArea) this.areas.get(AreaID);
			for (ChildArea childArea : area.getChildren()) {
				this.deleteArea(childArea.getID());
			}
			this.deleteArea(area.getID());
		}
	}
	
	public void deleteChildArea(Long AreaID) {
		if (!this.areas.containsKey(AreaID)) {
			return;
		}
		if (this.areas.get(AreaID) instanceof ChildArea) {
			ChildArea area = (ChildArea) this.areas.get(AreaID);
			area.getParent().removeChild(area);
			this.deleteArea(AreaID);
		}
	}
	
	public List<Area> getPrivateAreas() {
		List<Area> privateAreas = new ArrayList<Area>();
		for (Area area : this.areas.values()) {
			if (area instanceof PrivateArea) {
				privateAreas.add(area);
			}
		}
		return privateAreas;
	}
	
	public List<Area> getPvpAreas() {
		List<Area> pvpAreas = new ArrayList<Area>();
		for (Area area : this.areas.values()) {
			if (area instanceof PvpArea) {
				pvpAreas.add(area);
			}
		}
		return pvpAreas;
	}
	
	public List<Area> getSafetyAreas() {
		List<Area> safetyAreas = new ArrayList<Area>();
		for (Area area : this.areas.values()) {
			if (area instanceof SafetyArea) {
				safetyAreas.add(area);
			}
		}
		return safetyAreas;
	}
	
	public enum LotType {
		PARENT,CHILD,PRIVATE,PVP,SAFETY;
	}
}
