package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class AreaManager {
	private HashMap<Long, Area> areas;
	
	public void AddArea(Area area) {
		if (!areas.containsValue(area) && area.noID()) {
			Long ID = generateID();
			area.setID(ID);
			areas.put(ID, area);
		}
	}
	
	public PrivateArea newPrivateArea(Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner) {
		Long ID = generateID();
		PrivateArea privateArea = new PrivateArea(corner1, corner2, ignoreY, owner);
		privateArea.setID(ID);
		areas.put(ID, privateArea);
		return privateArea;
	}
	
	public PvpArea newPvpArea(Location corner1, Location corner2, boolean ignoreY) {
		Long ID = generateID();
		PvpArea pvpArea = new PvpArea(corner1, corner2, ignoreY);
		pvpArea.setID(ID);
		areas.put(ID, pvpArea);
		return pvpArea;
	}

	public SafetyArea newSafetyArea(Location corner1, Location corner2, boolean ignoreY) {
		Long ID = generateID();
		SafetyArea safetyArea = new SafetyArea(corner1, corner2, ignoreY);
		safetyArea.setID(ID);
		areas.put(ID, safetyArea);
		return safetyArea;
	}
	
	public TownArea newTownArea(Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner) {
		Long ID = generateID();
		TownArea townArea = new TownArea(corner1, corner2, ignoreY, owner);
		townArea.setID(ID);
		areas.put(ID, townArea);
		return townArea;
	}
	
	public ChildArea newChildArea(Location corner1, Location corner2, boolean ignoreY, Long ParentID) {
		if (getArea(ParentID) == null || !(getArea(ParentID) instanceof ParentArea)) return null;
		Long ID = generateID();
		ChildArea childArea = new ChildArea(ID, corner1, corner2, ignoreY, ParentID);
		((ParentArea) getArea(ParentID)).addChild(childArea);
		areas.put(ID, childArea);
		return childArea;
	}
	
	public Long generateID() {
		Long i;
		for (i = (long) 0; true; i++) {
			if (!areas.containsKey(i)) return i;
		}
	}

	/**
	 * This method deletes an Area of any type.
	 * @param AreaID
	 */
	public void deleteArea(Long AreaID) {
		if (this.areas.containsKey(AreaID)) {
			areas.remove(AreaID);
		}
	}
	
	/**
	 * This method deletes the ParentArea, plus ALL children.
	 * @param AreaID
	 */
	public void deleteParentAreaDeleteChildren(Long AreaID) {
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
	
	/**
	 * This method "deletes" a parent area, and transfers all data to it's first child.
	 * @param AreaID
	 */
	public void deleteParentPreserveChildren(Long AreaID) {
		if (!this.areas.containsKey(AreaID)) return;
		if (this.getArea(AreaID) instanceof ParentArea) {
			ParentArea area = (ParentArea) this.getArea(AreaID);
			if (area.getChildren().isEmpty()) {
				this.deleteArea(area.getID());
			} else {
				ChildArea child = area.getChildren().get(0);
				this.deleteChildArea(child.getID());
				area.setID(child.getID());
				area.setCorner1(child.getCorner1());
				area.setCorner2(child.getCorner2());
				area.ignoreY(child.ignoreY());
			}
		}
	}
	
	/**
	 * Deletes a ChildArea and removes it from it's parent's list of children
	 * @param AreaID
	 */
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
	
	/**
	 * Gets an ArrayList of areas at the given location.
	 * @param location The location to check.
	 * @param includeChildren If set to true, will allow ChildAreas in the list. If set to false, will only include the ChildArea's parent.
	 * @return List of areas at the given location.
	 */
	public List<Area> getAreasAtLocation(Location location, boolean includeChildren) {
		List<Area> areas = new ArrayList<Area>();
		for (Area area : this.areas.values()) {
			if (area.contains(location)) {
				if (area instanceof ChildArea && !includeChildren) {
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
		for (Area area : this.getAreasAtLocation(location, false)) {
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