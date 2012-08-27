package com.bendude56.goldenapple.permissions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;
import com.bendude56.goldenapple.util.Serializer;

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
	private boolean				complexCommands;
	private boolean				autoLock;

	protected PermissionUser(long id, String name, String preferredLocale, String permissions, boolean complexCommands, boolean autoLock) {
		this.id = id;
		this.name = name;
		this.preferredLocale = preferredLocale;
		try {
			@SuppressWarnings("unchecked")
			ArrayList<String> p = (ArrayList<String>)Serializer.deserialize(permissions);
			for (String permission : p) {
				this.permissions.add(GoldenApple.getInstance().permissions.registerPermission(permission));
			}
		} catch (Exception e) {
			GoldenApple.log(Level.SEVERE, "Failed to deserialize permissions for user " + name + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
		this.complexCommands = complexCommands;
		this.autoLock = autoLock;
	}

	private String serializePermissions() {
		try {
			ArrayList<String> p = new ArrayList<String>();
			for (Permission permission : permissions) {
				p.add(permission.getFullName());
			}
			return Serializer.serialize(p);
		} catch (Exception e) {
			GoldenApple.log(Level.SEVERE, "Failed to serialize permissions for " + name + ":");
			GoldenApple.log(Level.SEVERE, e);
			return "";
		}
	}

	/**
	 * Pushes any changes made to this user to the SQL database
	 */
	public void save() {
		try {
			GoldenApple.getInstance().database.execute("UPDATE Users SET Locale=?, Permissions=?, ComplexCommands=?, AutoLock=? WHERE ID=?", new Object[] { preferredLocale, serializePermissions(), complexCommands, autoLock, id });
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to save changes to user '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
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
			for (Long groupID : GoldenApple.getInstance().permissions.getGroups().keySet()) {
				if (!previousGroups.contains(groupID)) {
					for (Long checkedGroupID : previousGroups) {
						if (GoldenApple.getInstance().permissions.getGroup(groupID).getSubGroups().contains(checkedGroupID)) {
							for (Permission perm : GoldenApple.getInstance().permissions.getGroup(groupID).getPermissions(false)) {
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
	public boolean hasPermission(String permission) {
		return hasPermission(permission, true);
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return hasPermission(permission, true);
	}
	
	@Override
	public boolean hasPermission(Permission permission, boolean inherited) {
		List<Permission> pl = getPermissions(inherited);
		if (pl.contains(permission))
			return true;
		PermissionNode node = permission.getNode();
		while (node != null) {
			for (Permission p : node.getPermissions()) {
				if (p.getName().equals("*")) {
					if (pl.contains(p))
						return true;
					else
						break;
				}
			}
			node = node.getNode();
		}
		return false;
	}

	@Override
	public boolean hasPermission(String permission, boolean inherited) {
		return hasPermission(GoldenApple.getInstance().permissions.getPermissionByName(permission), inherited);
	}

	@Override
	public String getPreferredLocale() {
		return preferredLocale;
	}

	@Override
	public void addPermission(Permission permission) {
		if (!permissions.contains(permission)) {
			permissions.add(permission);
			save();
		}
	}

	@Override
	public void addPermission(String permission) {
		addPermission(GoldenApple.getInstance().permissions.getPermissionByName(permission));
	}

	@Override
	public void removePermission(Permission permission) {
		if (permissions.contains(permission)) {
			permissions.remove(permission);
			save();
		}
	}

	@Override
	public void removePermission(String permission) {
		removePermission(GoldenApple.getInstance().permissions.registerPermission(permission));
	}

	@Override
	public boolean isUsingComplexCommands() {
		return complexCommands;
	}

	@Override
	public void setUsingComplexCommands(boolean useComplex) {
		complexCommands = useComplex;
		save();
	}

	@Override
	public boolean isAutoLockEnabled() {
		return autoLock;
	}

	@Override
	public void setAutoLockEnabled(boolean autoLock) {
		this.autoLock = autoLock;
		save();
	}
}
