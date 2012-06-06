package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class AreaManager {
	private HashMap<Long, Area> areas;
	
	public PrivateArea newPrivateArea(Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner) {
		Long ID = generateID();
		PrivateArea privateArea = new PrivateArea(ID, corner1, corner2, ignoreY, owner);
		areas.put(ID, privateArea);
		return privateArea;
	}
	
	public ChildArea newChildArea(Location corner1, Location corner2, boolean ignoreY, Long ParentID) {
		if (getArea(ParentID) == null || !(getArea(ParentID) instanceof ParentArea)) return null;
		Long ID = generateID();
		ChildArea childArea = new ChildArea(ID, corner1, corner2, ignoreY, ParentID);
		((ParentArea) getArea(ParentID)).addChild(childArea);
		areas.put(ID, childArea);
		return childArea;
	}
	
	public PvpArea newPvpArea(Location corner1, Location corner2, boolean ignoreY) {
		Long ID = generateID();
		PvpArea pvpArea = new PvpArea(ID, corner1, corner2, ignoreY);
		areas.put(ID, pvpArea);
		return pvpArea;
	}

	public SafetyArea newSafetyArea(Location corner1, Location corner2, boolean ignoreY) {
		Long ID = generateID();
		SafetyArea safetyArea = new SafetyArea(ID, corner1, corner2, ignoreY);
		areas.put(ID, safetyArea);
		return safetyArea;
	}
	
	public Long generateID() {
		Long i;
		for (i = (long) 0; true; i++) {
			if (!areas.containsKey(i)) return i;
		}
	}

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
	
	public Area getArea(Long areaID) {
		if (areas.containsKey(areaID)) {
			return areas.get(areaID);
		} else {
			return null;
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
	
	public List<Area> getAreasAtLocation(Location location) {
		List<Area> areas = new ArrayList<Area>();
		for (Area area : this.areas.values()) {
			if (area.contains(location)) {
				if (area instanceof ChildArea) {
					area = ((ChildArea) area).getParent();
				}
				
				if (!areas.contains(area)) {
					areas.add(area);
				}
			}
		}
		return areas;
	}
	
	public boolean canEditLocation(Location location, IPermissionUser user) {
		for (Area area : this.getAreasAtLocation(location)) {
			if (area instanceof PrivateArea) {
				if (((PrivateArea) area).canEdit(user)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public enum LotType {
		PARENT,CHILD,PRIVATE,PVP,SAFETY;
	}
}