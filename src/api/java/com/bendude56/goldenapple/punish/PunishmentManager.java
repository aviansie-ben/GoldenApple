package com.bendude56.goldenapple.punish;

import java.util.ArrayList;

import com.bendude56.goldenapple.chat.ChatChannel;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;

public abstract class PunishmentManager {
	// goldenapple.punish
	public static PermissionNode		punishNode;
	public static Permission            whoisPermission;
	
	// goldenapple.punish.globalmute
	public static PermissionNode        globalMuteNode;
	public static Permission            globalMuteInfoPermission;
	public static Permission            globalMuteTempPermission;
	public static Permission            globalMuteTempOverridePermission;
	public static Permission            globalMutePermPermission;
	public static Permission            globalMuteVoidPermission;
	public static Permission            globalMuteVoidAllPermission;
	
	// goldenapple.punish.ban
	public static PermissionNode        banNode;
	public static Permission            banInfoPermission;
	public static Permission            banTempPermission;
	public static Permission            banTempOverridePermission;
	public static Permission            banPermPermission;
	public static Permission            banVoidPermission;
	public static Permission            banVoidAllPermission;
	
	protected static PunishmentManager instance;
	
	public static PunishmentManager getInstance() {
		return instance;
	}
	
	public abstract void loadIntoCache(IPermissionUser user);
	public abstract void unloadFromCache(IPermissionUser user);
	
	public abstract void addPunishment(Punishment p, IPermissionUser u);
	public abstract void addMute(IPermissionUser target, IPermissionUser admin, String reason, RemainingTime duration, String channel);
	public abstract void addBan(IPermissionUser target, IPermissionUser admin, String reason, RemainingTime duration);
	
	public abstract boolean isMuted(IPermissionUser u, ChatChannel channel);
	public abstract PunishmentMute getActiveMute(IPermissionUser u, ChatChannel channel);
	
	public abstract Punishment getActivePunishment(IPermissionUser u, Class<? extends Punishment> punishmentType);
	public abstract boolean hasActivePunishment(IPermissionUser u, Class<? extends Punishment> punishmentType);
	
	public abstract ArrayList<Punishment> getPunishments(IPermissionUser u, Class<? extends Punishment> punishmentType);
	
	public abstract void clearCache();
}
