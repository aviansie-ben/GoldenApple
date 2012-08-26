package com.bendude56.goldenapple.warp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

/**
 * this class' responsibility is to manage ALL warps,
 * public, private, or otherwise.
 * @author Deaboy
 *
 */
public class WarpManager {
	public static PermissionNode warpNode;
	public static Permission warpAdd;
	public static Permission warpRemove;
	public static Permission warpEdit;
	public static Permission warpWarpAll;
	
	public static PermissionNode homeNode;
	public static PermissionNode homeNodeOwn;
	public static PermissionNode homeNodeAll;
	
	public static Permission homeAddOwn;
	public static Permission homeRemoveOwn;
	public static Permission homeEditOwn;
	public static Permission homeWarpOwn;
	
	public static Permission homeAddAll;
	public static Permission homeRemoveAll;
	public static Permission homeEditAll;
	public static Permission homeWarpAll;
	
	public static PermissionNode checkpointNode;
	public static PermissionNode checkpointNodeOwn;
	public static PermissionNode checkpointNodeAll;
	
	public static Permission checkpointAddOwn;
	public static Permission checkpointRemoveOwn;
	public static Permission checkpointWarpOwn;
	
	public static Permission checkpointAddAll;
	public static Permission checkpointRemoveAll;
	public static Permission checkpointWarpAll;
	
	private static HashMap<Long, PublicWarp> publicWarps;
	private static HashMap<Long, PrivateWarp> privateWarps;
	private static HashMap<Long, CheckpointWarp> checkpointWarps;
	
	
	//---------------GENERIC METHODS------------------
	
	/**
	 * Assembles all types of Warps into a single List<Warp>
	 * @return An ArrayList of all stored Warps.
	 */
	public List<Warp> getAllWarps(){
		List<Warp> warps = new ArrayList<Warp>();
		warps.addAll(publicWarps.values());
		warps.addAll(privateWarps.values());
		warps.addAll(checkpointWarps.values());
		return warps;
	}
	
	
	//----------------PUBLIC WARPS--------------------
	
	/**
	 * Creates a new PublicWarp and stores it in the static
	 * publicWarps HashMap.
	 * @param location The location of the warp
	 * @param label The label for the warp
	 * @param group The group allowed to use the warp.
	 * @return Returns the created PublicWarp
	 */
	public PublicWarp newPublicWarp(Location location, String label, PermissionGroup group){
		for (PublicWarp warp : publicWarps.values()){
			if (warp.getLabel().equalsIgnoreCase(label))
				return null;
		}
		PublicWarp warp = new PublicWarp(generatePublicWarpId(), location, label, group);
		publicWarps.put(warp.getId(), warp);
		return warp;
	}
	
	/**
	 * Creates a new PublicWarp and stores it in the static
	 * publicWarps HashMap.
	 * @param location The location of the warp
	 * @param label The label for the warp
	 * @return Returns the created PublicWarp
	 */
	public PublicWarp newPublicWarp(Location location, String label){
		return newPublicWarp(location, label, null);
	}
	
	/**
	 * Deletes a PublicWarp, searched for by the warp's ID.
	 * @param ID The ID of the warp to be deleted
	 * @return Returns the deleted warp so that details like the location and label can be retrieved, or to verify if the warp was deleted. Returns null if it was not deleted for whatever reason.
	 */
	public PublicWarp deletePublicWarp(Long ID){
		PublicWarp warp = getPublicWarp(ID);
		if (warp != null)
			publicWarps.remove(ID);
		return warp;
	}
	
	/**
	 * @return Returns an ArrayList<PublicWarps> of all stored PublicWarps.
	 */
	public List<PublicWarp> getAllPublicWarps(){
		List<PublicWarp> warps = new ArrayList<PublicWarp>();
		warps.addAll(publicWarps.values());
		return warps;
	}
	
	/**
	 * Use to retrieve a stored PublicWarp using the ID.
	 * @param Id ID of the public warp.
	 * @return Returns the public warp if it exists. If not, returns null.
	 */
	public PublicWarp getPublicWarp(Long Id){
		if (publicWarps.containsKey(Id))
			return publicWarps.get(Id);
		else
			return null;
	}
	
	/**
	 * Use to retrieve a stored PublicWarp using the warp's
	 * label (name), ignoring case.
	 * @param label The label to search for.
	 * @return Returns the PublicWarp if found. If not found, returns null.
	 */
	public PublicWarp getPublicWarp(String label){
		for (PublicWarp warp : publicWarps.values()){
			if(warp.getLabel().equalsIgnoreCase(label))
				return warp;
		}
		return null;
	}
	
	private Long generatePublicWarpId(){
		Long index = (long) 0;
		while (publicWarps.containsKey(index))
			index++;
		return index;
	}
	
	
	//----------------PRIVATE WARPS--------------------
	
	/**
	 * Creates a new PrivateWarp and stores it in the static
	 * privateWarps HashMap. If one already exists with the same
	 * owner and index, changes the original warp's location and
	 * returns that one instead.
	 * @param location The location of the warp
	 * @param owner The owner of the warp
	 * @param index The index of the warp (for multiple homes)
	 * @return Returns a PrivateWarp.
	 */
	public PrivateWarp newPrivateWarp(Location location, IPermissionUser owner, int index){
		for (PrivateWarp warp : privateWarps.values()) {
			if (warp.getOwner() == owner.getId()
					&& warp.getIndex() == index){
				warp.setLocation(location);
				return warp;
			}
		}
		PrivateWarp warp = new PrivateWarp(generatePrivateWarpId(), location, owner, index);
		privateWarps.put(warp.getId(), warp);
		return warp;
	}
	
	/**
	 * Creates a new PrivateWarp and stores it in the static
	 * privateWarps HashMap. If one already exists with the same
	 * owner and index, changes the original warp's location and
	 * returns that one instead. Index defaulted to 1.
	 * @param location The location of the warp
	 * @param owner The owner of the warp
	 * @return Returns a PrivateWarp.
	 */
	public PrivateWarp newPrivateWarp(Location location, IPermissionUser owner){
		return newPrivateWarp(location, owner, 1);
	}
	
	/**
	 * Deletes a PrivateWarp, searched for by the warp's ID.
	 * @param ID The ID of the warp to be deleted
	 * @return Returns the deleted warp so that details like the location and label can be retrieved, or to verify if the warp was deleted. Returns null if it was not deleted for whatever reason.
	 */
	public PrivateWarp deletePrivateWarp(Long ID){
		PrivateWarp warp = getPrivateWarp(ID);
		if (warp != null)
			privateWarps.remove(ID);
		return warp;
	}
	
	/**
	 * @return Returns an ArrayList<PrivateWarps> of all stored PrivateWarps.
	 */
	public List<PrivateWarp> getAllPrivateWarps(){
		List<PrivateWarp> warps = new ArrayList<PrivateWarp>();
		warps.addAll(privateWarps.values());
		return warps;
	}
	
	/**
	 * Gets a stored PrivateWarp by the warp's ID.
	 * @param Id The ID to search for.
	 * @return A PrivateWarp if it finds it. Null if it doesn't.
	 */
	public PrivateWarp getPrivateWarp(Long Id){
		if (privateWarps.containsKey(Id))
			return privateWarps.get(Id);
		else
			return null;
	}
	
	/**
	 * Gets a stored PrivateWarp by the owner and the warp's index.
	 * @param owner The owner to search for
	 * @param index The index to search for
	 * @return The PrivateWarp if found. If not, returns null;
	 */
	public PrivateWarp getPrivateWarp(IPermissionUser owner, int index){
		for (PrivateWarp warp : privateWarps.values())
			if (warp.getOwner() == owner.getId() && warp.getIndex() == index)
				return warp;
		return null;
	}
	
	/**
	 * Gets a stored PrivateWarp by the owner with index 1.
	 * @param owner The owner to search for
	 * @return The PrivateWarp if found. If not, returns null;
	 */
	public PrivateWarp getPrivateWarp(IPermissionUser owner){
		return getPrivateWarp(owner, 1);
	}
	
	private Long generatePrivateWarpId(){
		Long index = (long) 0;
		while (privateWarps.containsKey(index))
			index++;
		return index;
	}
	

	//----------------CHECKPOINT WARPS--------------------
	
	/**
	 * Creates a new CheckpointWarp for a user at a specific location.
	 * Also increments the indecies of all previous checkpoints owned
	 * by the same user and sets the new checkpoint's index to 0, then
	 * deletes any checkpoints who's index is larger than the limit.
	 * @param location Location of the new CheckpointWarp
	 * @param owner Owner of the CheckpointWarp
	 * @return Returns the newly created checkpoint warp
	 */
	public CheckpointWarp newCheckpointWarp(Location location, IPermissionUser owner){
		shiftCheckpointWarpIndexes(owner);
		CheckpointWarp warp = new CheckpointWarp(generateCheckpointWarpId(), location, owner, 0);
		checkpointWarps.put(warp.getId(), warp);
		return warp;
	}
	
	/**
	 * Deletes a stored CheckpointWarp by ID
	 * @param ID The ID to search for
	 * @return Returns the deleted CheckpointArea, null if not found.
	 */
	public CheckpointWarp deleteCheckpointWarp(Long ID){
		CheckpointWarp warp = getCheckpointWarp(ID);
		if (warp!=null)
			checkpointWarps.remove(ID);
		return warp;
	}
	
	/**
	 * Deletes a stored CheckpointWarp by owner and index.
	 * @param owner The owner to search for
	 * @param index The index to search for
	 * @return Returns the deleted CheckpointWarp, null if not found.
	 */
	public CheckpointWarp deleteCheckpointWarp(IPermissionUser owner, int index){
		return deleteCheckpointWarp(getCheckpointWarp(owner, index).getId());
	}
	
	/**
	 * @return Returns an ArrayList<CheckpointWarps> of all stored CheckpointWarps.
	 */
	public List<CheckpointWarp> getAllCheckpointWarps(){
		List<CheckpointWarp> warps = new ArrayList<CheckpointWarp>();
		warps.addAll(checkpointWarps.values());
		return warps;
	}
	
	/**
	 * Gets a stored CheckpointWarp by the warp's ID.
	 * @param Id The ID to search for.
	 * @return Returns the CheckpointWarp if found. If not, returns null.
	 */
	public CheckpointWarp getCheckpointWarp(Long Id){
		if (checkpointWarps.containsKey(Id))
			return checkpointWarps.get(Id);
		else
			return null;
	}
	
	/**
	 * Gets a stored CheckpointWarp by the owner and index. Returns null
	 * if it is not found.
	 * @param owner The user to check against
	 * @param index The index of the checkpoint (a per-user index, ranging from 0 to the specified limit)
	 * @return Returns the CheckpointWarp if found. If not found, returns null.
	 */
	public CheckpointWarp getCheckpointWarp(IPermissionUser owner, int index){
		for (CheckpointWarp warp : checkpointWarps.values()){
			if (warp.getOwner() == owner.getId()
					&& warp.getIndex()== index)
				return warp;
		}
		return null;
	}
	
	/**
	 * Gets a stored CheckpointWarp by the owner with index 0. Returns null
	 * if it is not found.
	 * @param owner The user to check against
	 * @return Returns the CheckpointWarp if found. If not found, returns null.
	 */
	public CheckpointWarp getCheckpointWarp(IPermissionUser owner){
		return getCheckpointWarp(owner, 0);
	}
	
	public Long generateCheckpointWarpId(){
		Long index = (long) 0;
		while (checkpointWarps.containsKey(index))
			index++;
		return index;
	}
	
	/**
	 * Increments all stored CheckpointWarps owned by the given owner.
	 * @param owner The user to check against.
	 */
	private void shiftCheckpointWarpIndexes(IPermissionUser owner){
		for (CheckpointWarp warp : checkpointWarps.values()) {
			if (warp.getOwner() == owner.getId())
				warp.setIndex(warp.getIndex()+1);
		}
	}
	
}
