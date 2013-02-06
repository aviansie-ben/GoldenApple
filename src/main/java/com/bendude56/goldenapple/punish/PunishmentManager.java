package com.bendude56.goldenapple.punish;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class PunishmentManager {
	// goldenapple.punish
	public static PermissionNode		punishNode;
	
	// goldenapple.punish.mute
	public static PermissionNode        muteNode;
	public static Permission            muteTempNode;
	public static Permission            muteTempOverrideNode;
	public static Permission            mutePermNode;
	
	// goldenapple.punish.globalmute
	public static PermissionNode        globalMuteNode;
	public static Permission            globalMuteTempNode;
	public static Permission            globalMuteTempOverrideNode;
	public static Permission            globalMutePermNode;
	
	// goldenapple.punish.ban
	public static PermissionNode        banNode;
	public static Permission            banTempNode;
	public static Permission            banTempOverrideNode;
	public static Permission            banPermNode;

	

	public PunishmentManager() {
		
	}
}
