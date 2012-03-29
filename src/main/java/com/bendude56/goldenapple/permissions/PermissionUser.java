package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

/**
 * Represents a user in the GoldenApple permissions database.
 * <p>
 * <em><strong>Note 1:</strong> Do not store direct references to this class. Store the
 * ID of the instance instead! This instance is simply a cached image, and thus may not
 * update correctly if the cache is cleared.</em>
 * <p>
 * <em><strong>Note 2:</strong> It is recommended that you refrain from accepting this
 * class as an argument to a function. Use {@link IPermissionUser} instead in order to
 * support the use of {@link com.bendude56.goldenapple.User} objects.</em>
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class PermissionUser implements IPermissionUser {
	private long				id;
	private String				name;
	private String				preferredLocale;
	private List<Permission>	permissions	= new ArrayList<Permission>();

	protected PermissionUser(long id, String name, String preferredLocale, String permissions) {
		this.id = id;
		this.name = name;
		this.preferredLocale = preferredLocale;
		for (String s : permissions.split("/")) {
			this.permissions.add(GoldenApple.getInstance().permissions.registerPermission(s));
		}
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public List<Permission> getPermissions(boolean inherited) {
		List<Permission> returnPermissions = permissions;
		if (inherited) {
			List<Long> previousGroups = new ArrayList<Long>();
			for (Long groupID : GoldenApple.getInstance().getPermissions().getGroups().keySet()) {
				if (!previousGroups.contains(groupID)) {
					for (Long checkedGroupID : previousGroups) {
						if (GoldenApple.getInstance().getPermissions().getGroup(groupID).getSubGroups().contains(checkedGroupID)) {
							for (Permission perm : GoldenApple.getInstance().getPermissions().getGroup(groupID).getPermissions(false)) {
								if (!returnPermissions.contains(perm)) {
									returnPermissions.add(perm);
								}
							}
						}
					}
				}
			}
		}
		return returnPermissions;
	}
	
	@Override
	public boolean hasPermission(Permission permission) {
		return hasPermission(permission, false);
	}
	
	@Override
	public boolean hasPermission(String permission) {
		return hasPermission(permission, false);
	}
	
	@Override
	public boolean hasPermission(Permission permission, boolean specific) {
		return getPermissions(specific).contains(permission);
	}
	
	@Override
	public boolean hasPermission(String permission, boolean specific) {
		return hasPermission(GoldenApple.getInstance().permissions.registerPermission(permission), specific);
	}

	@Override
	public String getPreferredLocale() {
		return preferredLocale;
	}
}
