package com.bendude56.goldenapple.warp;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

/**
 * This class, previously known as a "Home" warp,
 * has an owner (Long) and an index (int) (for support for multiple homes).
 * @author Deaboy
 *
 */
public class PrivateWarp extends Warp{
	private Long owner;
	private final int index;
	
	/**
	 * 
	 * @param ID The final ID number for the warp
	 * @param location The location of the warp
	 * @param owner The owner of the warp
	 * @param index The index of the warp (for multiple warps owned by the same user)
	 */
	public PrivateWarp(Long ID, Location location, IPermissionUser owner, int index){
		super(ID, location);
		this.owner = owner.getId();
		this.index = index;
	}
	
	/**
	 * Method to determine if a given user is this warp's owner.
	 * @param user The user in question
	 * @return True if the user IDs match, false if not.
	 */
	public boolean ownedBy(IPermissionUser user){
		return getOwner() == user.getId();
	}
	
	public void setOwner(IPermissionUser owner){
		this.owner = owner.getId();
	}
	public Long getOwner(){
		return owner;
	}
	public int getIndex(){
		return index;
	}
}
