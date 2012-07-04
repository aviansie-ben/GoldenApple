package com.bendude56.goldenapple.warps;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

/**
 * This class is meant for checkpoints. For example,
 * the /back command. Stores an owner and an index
 * for support for multiple backs. Example: /back 3
 * will warp the player 3 checkpoints back.
 * @author Deaboy
 *
 */
public class CheckpointWarp extends Warp{
	private final Long owner;
	private int index;
	
	/**
	 * 
	 * @param ID The final ID for the Warp
	 * @param location The location of the Warp
	 * @param owner The final owner of the checkpoint
	 * @param index The index of the checkpoint
	 */
	public CheckpointWarp(Long ID, Location location, IPermissionUser owner, int index){
		super(ID, location);
		this.owner = owner.getId();
		this.index = index;
	}
	
	public Long getOwner(){
		return owner;
	}
	public void setIndex(int index){
		this.index = index;
	}
	public int getIndex(){
		return index;
	}
}
