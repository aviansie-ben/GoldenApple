package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

/**
 * This class manages ALL GoldenApple areas.
 * 
 * @author Deaboy
 * 
 */
public class AreaManager {
	public static PermissionNode		areaNode;
	public static Permission			areaCreate;
	public static Permission			areaDelete;
	public static Permission			areaEditAll;
	public static Permission			areaEditOwn;

	private static HashMap<Long, Area>	areas;

	// --------------GENERALIZED METHODS-----------------

	/**
	 * Returns the stored Area by ID number
	 * 
	 * @param ID The ID to search for
	 */
	public Area getArea(Long ID) {
		if (areas.containsKey(ID))
			return areas.get(ID);
		else
			return null;
	}

	/**
	 * Gets an ArrayList of all Areas at the given Location.
	 * 
	 * @param location The Location to check.
	 * @param onlyParents If set to false, will allow ChildAreas in the list. If
	 *            true, will only include the ChildArea's parent.
	 */
	public List<Area> getAreasAtLocation(Location location, boolean onlyParents) {
		List<Area> localAreas = new ArrayList<Area>();
		for (Area area : areas.values()) {
			if (area.contains(location)) {
				if (area instanceof ChildArea && onlyParents) {
					area = ((ChildArea)area).getParent();
				}

				if (!localAreas.contains(area)) {
					localAreas.add(area);
				}
			}
		}
		return localAreas;
	}

	/**
	 * Generates a unique area ID.
	 * 
	 * @return
	 */
	private Long generateId() {
		Long id = 0L;
		while (areas.containsKey(id))
			id++;
		return id;
	}

	/**
	 * Verifies that an Area is one that is stored in this official areas
	 * HashMap.
	 * 
	 * @param area The area to be verify
	 */
	public boolean verifyArea(Area area) {
		return (areas.containsValue(area));
	}

	/**
	 * Deletes a single ChildArea from storage.
	 * 
	 * @param ID The ID of the ChildArea;
	 * @return Returns the deleted ChildArea, null if it never existed
	 */
	public ChildArea deleteChildArea(Long ID) {
		ChildArea area = getChildArea(ID);
		if (area != null)
			areas.remove(ID);
		return area;
	}

	// ----------------PARENT METHODS--------------------

	/**
	 * Assembles and returns a List of all stored ChildrenAreas who's parent is
	 * the given ParentArea
	 * 
	 * @param parent The ParentArea to search for
	 */
	public List<ChildArea> getChildren(ParentArea parent) {
		List<ChildArea> children = new ArrayList<ChildArea>();
		for (ChildArea child : getAllChildAreas())
			if (child.getParent() == parent)
				children.add(child);
		return children;
	}

	/**
	 * Deletes a ParentArea and any ChildAreas associated with it.
	 * 
	 * @param ID The ID of the ParentArea to delete
	 * @return Returnes the deleted ParentArea, or null if it never existed.
	 */
	public ParentArea deleteParentAreaAndChildren(Long ID) {
		ParentArea area = getParentArea(ID);
		if (area != null) {
			for (ChildArea child : getChildren(area))
				deleteChildArea(child.getID());
			areas.remove(ID);
		}
		return area;
	}

	/**
	 * This method does <strong>NOT</strong> actually delete the ParentArea and
	 * keep the children. <strong>INSTEAD</strong>, the ParentArea will inherit
	 * corner1, corner2, and ignoreY from its first child, then delete the first
	 * child. <u>If the ParentArea does not have any children, then it will
	 * delete itself.</u>
	 * 
	 * @param ID The ID of the ParentArea to "delete"
	 * @return Returns the same ParentArea, or null if it didn't exist
	 */
	public ParentArea deleteParentAreaKeepChildren(Long ID) {
		ParentArea area = getParentArea(ID);
		if (area != null) {
			if (!area.getChildren().isEmpty()) {
				area.sortChildren();
				ChildArea child = area.getChildren().get(0);
				area.setCorner1(child.getCorner1());
				area.setCorner2(child.getCorner2());
				area.ignoreY(child.ignoreY());
				deleteChildArea(child.getID());
				area.sortChildren();
			}
		}
		return area;
	}

	/**
	 * Returns any stored ParentArea by ID
	 * 
	 * @param ID The ID to search for
	 */
	public ParentArea getParentArea(Long ID) {
		if (areas.containsKey(ID) && areas.get(ID) instanceof ParentArea)
			return (ParentArea)areas.get(ID);
		return null;
	}

	/**
	 * This method sorts a ParentArea's children's indexes such that their
	 * indexes will start at 0 and go up sequentially from there in order
	 * WITHOUT skipping over any number. This method preserves the numerical
	 * order of the children's indexes.
	 * 
	 * @param parent The ParentArea who's children are to be sorted
	 */
	public void sortChildren(ParentArea parent) {
		List<ChildArea> children = getChildren(parent);
		Integer lowest = null;

		for (int index = 0; index < children.size(); index++) {
			for (ChildArea child : children) {
				if (lowest == null)
					lowest = child.getIndex();
				else if (child.getIndex() < lowest && child.getIndex() >= index)
					lowest = child.getIndex();
			}
			for (ChildArea child : children) {
				if (child.getIndex() == lowest) {
					child.setIndex(index);
					break;
				}
			}
		}
	}

	// -----------------PRIVATE AREAS--------------------

	/**
	 * Creates a new PrivateArea and stores it in the areas HashMap
	 * 
	 * @param corner1 Location of the first corner
	 * @param corner2 Location of the second corner
	 * @param ignoreY If true, ignores the Y-coordinate
	 * @param owner The owner of the area
	 * @return Returns the newly created PrivateArea
	 */
	public PrivateArea newPrivateArea(Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner) {
		PrivateArea area = new PrivateArea(generateId(), corner1, corner2, ignoreY, owner);
		areas.put(area.getID(), area);
		return area;
	}

	/**
	 * Returns the stored PrivateArea by ID number
	 * 
	 * @param ID The ID to search for
	 */
	public PrivateArea getPrivateArea(Long ID) {
		Area area = getArea(ID);
		if (area != null && area instanceof PrivateArea)
			return (PrivateArea)area;
		else
			return null;
	}

	/**
	 * Assembles and returns an ArrayList of all stored PrivateAreas
	 * 
	 * @param includeChildren If true, will include ChildAreas who's parents are
	 *            instances of PrivateAreas
	 * @return Returns an ArrayList of stored PrivateAreas
	 */
	public List<PrivateArea> getAllPrivateAreas(boolean includeChildren) {
		List<PrivateArea> privateAreas = new ArrayList<PrivateArea>();
		for (Area area : areas.values())
			if (area instanceof PrivateArea)
				privateAreas.add((PrivateArea)area);
			else if (includeChildren && area instanceof ChildArea && ((ChildArea)area).getParent() instanceof PrivateArea)
				privateAreas.add((PrivateArea)((ChildArea)area).getParent());
		return privateAreas;
	}

	// -------------------PVP AREAS----------------------

	/**
	 * Creates a new PvpArea and stores it in the areas HashMap
	 * 
	 * @param corner1 Location of the first corner
	 * @param corner2 Location of the second corner
	 * @param ignoreY If true, ignores the Y-coordinate
	 * @return Returns the newly created PvpArea
	 */
	public PvpArea newPvpArea(Location corner1, Location corner2, boolean ignoreY) {
		PvpArea area = new PvpArea(generateId(), corner1, corner2, ignoreY);
		areas.put(area.getID(), area);
		return area;
	}

	/**
	 * Returns the stored PvpArea by ID number
	 * 
	 * @param ID The ID to search for
	 */
	public PvpArea getPvpArea(Long ID) {
		Area area = getArea(ID);
		if (area != null && area instanceof PvpArea)
			return (PvpArea)area;
		else
			return null;
	}

	/**
	 * Assembles and returns an ArrayList of all stored PvpAreas
	 * 
	 * @param includeChildren If true, will include ChildAreas who's parents are
	 *            instances of PvpAreas
	 * @return Returns an ArrayList of stored PvpAreas
	 */
	public List<PvpArea> getAllPvpAreas(boolean includeChildren) {
		List<PvpArea> pvpAreas = new ArrayList<PvpArea>();
		for (Area area : areas.values())
			if (area instanceof PvpArea)
				pvpAreas.add((PvpArea)area);
			else if (includeChildren && area instanceof ChildArea && ((ChildArea)area).getParent() instanceof PvpArea)
				pvpAreas.add((PvpArea)((ChildArea)area).getParent());
		return pvpAreas;
	}

	// -----------------SAFETY AREAS---------------------

	/**
	 * Creates a new SafetyArea and stores it in the areas HashMap
	 * 
	 * @param corner1 Location of the first corner
	 * @param corner2 Location of the second corner
	 * @param ignoreY If true, ignores the Y-coordinate
	 * @return Returns the newly created SafetyArea
	 */
	public SafetyArea newSafetyArea(Location corner1, Location corner2, boolean ignoreY) {
		SafetyArea area = new SafetyArea(generateId(), corner1, corner2, ignoreY);
		areas.put(area.getID(), area);
		return area;
	}

	/**
	 * Returns the stored SafetyArea by ID number
	 * 
	 * @param ID The ID to search for
	 */
	public SafetyArea getSafetyArea(Long ID) {
		Area area = getArea(ID);
		if (area != null && area instanceof SafetyArea)
			return (SafetyArea)area;
		else
			return null;
	}

	/**
	 * Assembles and returns an ArrayList of all stored SafetyAreas
	 * 
	 * @param includeChildren If true, will include ChildAreas who's parents are
	 *            instances of SafetyAreas
	 * @return Returns an ArrayList of stored SafetyAreas
	 */
	public List<SafetyArea> getAllSafetyAreas(boolean includeChildren) {
		List<SafetyArea> safetyAreas = new ArrayList<SafetyArea>();
		for (Area area : areas.values())
			if (area instanceof SafetyArea)
				safetyAreas.add((SafetyArea)area);
			else if (includeChildren && area instanceof ChildArea && ((ChildArea)area).getParent() instanceof SafetyArea)
				safetyAreas.add((SafetyArea)((ChildArea)area).getParent());
		return safetyAreas;
	}

	// ------------------TOWN AREAS----------------------

	/**
	 * Creates a new TownArea and stores it in the areas HashMap
	 * 
	 * @param corner1 Location of the first corner
	 * @param corner2 Location of the second corner
	 * @param ignoreY If true, ignores the Y-coordinate
	 * @param owner The owner of the area
	 * @return Returns the newly created TownArea
	 */
	public TownArea newTownArea(Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner) {
		TownArea area = new TownArea(generateId(), corner1, corner2, ignoreY, owner);
		areas.put(area.getID(), area);
		return area;
	}

	/**
	 * Returns the stored TownArea by ID number
	 * 
	 * @param ID The ID to search for
	 */
	public TownArea getTownArea(Long ID) {
		Area area = getArea(ID);
		if (area != null && area instanceof TownArea)
			return (TownArea)area;
		else
			return null;
	}

	/**
	 * Assembles and returns an ArrayList of all stored TownAreas
	 * 
	 * @param includeChildren If true, will include ChildAreas who's parents are
	 *            instances of TownAreas
	 * @return Returns an ArrayList of stored TownAreas
	 */
	public List<TownArea> getAllTownAreas(boolean includeChildren) {
		List<TownArea> townAreas = new ArrayList<TownArea>();
		for (Area area : areas.values())
			if (area instanceof TownArea)
				townAreas.add((TownArea)area);
			else if (includeChildren && area instanceof ChildArea && ((ChildArea)area).getParent() instanceof TownArea)
				townAreas.add((TownArea)((ChildArea)area).getParent());
		return townAreas;
	}

	// ------------------CHILD AREAS---------------------

	/**
	 * Creates a new ChildArea and stores it in the areas HashMap
	 * 
	 * @param corner1 Location of the first corner
	 * @param corner2 Location of the second corner
	 * @param ignoreY If true, ignores the Y-coordinate
	 * @param parent The parent of the child
	 * @return Returns the newly created ChildArea
	 */
	public ChildArea newChildArea(Location corner1, Location corner2, boolean ignoreY, ParentArea parent) {
		if (parent == null || !verifyArea(parent))
			return null;
		ChildArea area = new ChildArea(generateId(), corner1, corner2, ignoreY, parent, generateChildIndex(parent));
		areas.put(area.getID(), area);
		return area;
	}

	/**
	 * Returns the stored ChildArea by ID number
	 * 
	 * @param ID The ID to search for
	 */
	public ChildArea getChildArea(Long ID) {
		Area area = getArea(ID);
		if (area != null && area instanceof ChildArea)
			return (ChildArea)area;
		else
			return null;
	}

	/**
	 * Assembles and returns an ArrayList of all stored ChildAreas
	 * 
	 * @return Returns an ArrayList of stored ChildAreas
	 */
	public List<ChildArea> getAllChildAreas() {
		List<ChildArea> childAreas = new ArrayList<ChildArea>();
		for (Area area : areas.values())
			if (area instanceof ChildArea)
				childAreas.add((ChildArea)area);
		return childAreas;
	}

	/**
	 * Generates an index number for a ChildArea
	 * 
	 * @param parent The ParentArea of the ChildArea
	 */
	private int generateChildIndex(ParentArea parent) {
		sortChildren(parent);
		return getChildren(parent).size();
	}

	public boolean canEditLocation(Location location, IPermissionUser user) {
		for (Area area : this.getAreasAtLocation(location, false)) {
			if (area instanceof PrivateArea) {
				if (((PrivateArea)area).canEdit(user)) {
					return true;
				}
			}
		}
		return false;
	}
}
