package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

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
	private boolean				complexCommands;
	private boolean				autoLock;

	protected PermissionUser(long id, String name, String preferredLocale, boolean complexCommands, boolean autoLock) {
		this.id = id;
		this.name = name;
		this.preferredLocale = preferredLocale;
		this.complexCommands = complexCommands;
		this.autoLock = autoLock;
	}

	/**
	 * Pushes any changes made to this user to the SQL database
	 */
	public void save() {
		try {
			GoldenApple.getInstance().database.execute("UPDATE Users SET Locale=?, ComplexCommands=?, AutoLock=? WHERE ID=?", new Object[] { preferredLocale, complexCommands, autoLock, id });
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
		try {
			List<Long> gr = getParentGroups(inherited);
			List<Permission> permissions = new ArrayList<Permission>();
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT Permission FROM UserPermissions WHERE UserID=?", id);
				while (r.next()) {
					permissions.add(GoldenApple.getInstance().permissions.registerPermission(r.getString("Permission")));
				}
			} finally {
				if (r != null)
					r.close();
			}
			
			if (inherited) {
				for (Long g : gr) {
					r = null;
					try {
						r = GoldenApple.getInstance().database.executeQuery("SELECT Permission FROM GroupPermissions WHERE GroupID=?", g);
						while (r.next()) {
							permissions.add(GoldenApple.getInstance().permissions.registerPermission(r.getString("Permission")));
						}
					} finally {
						if (r != null)
							r.close();
					}
				}
			}
			
			return permissions;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to calculate permissions for user '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
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
	public boolean hasPermission(String permission, boolean inherited) {
		return hasPermission(GoldenApple.getInstance().permissions.getPermissionByName(permission), inherited);
	}
	
	@Override
	public boolean hasPermission(Permission permission, boolean inherited) {
		List<Long> parentGroups = getParentGroups(true);
		if (hasPermissionSpecificInheritance(permission, parentGroups, inherited))
			return true;
		PermissionNode node = permission.getNode();
		while (node != null) {
			for (Permission p : node.getPermissions()) {
				if (p.getName().equals("*") && hasPermissionSpecificInheritance(p, parentGroups, inherited))
					return true;
			}
			node = node.getNode();
		}
		return false;
	}
	
	private boolean hasPermissionSpecificInheritance(Permission permission, List<Long> groups, boolean inherited) {
		if (hasPermissionSpecific(permission)) {
			return true;
		} else if (inherited) {
			for (Long g : groups) {
				PermissionGroup gr = GoldenApple.getInstance().permissions.getGroup(g);
				if (gr.hasPermissionSpecific(permission)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean hasPermissionSpecific(Permission permission) {
		ResultSet r = null;
		try {
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM UserPermissions WHERE UserID=? AND Permission=?", id, permission.getFullName());
				return r.next();
			} finally {
				if (r != null)
					r.close();
			}
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public String getPreferredLocale() {
		return preferredLocale;
	}

	@Override
	public void addPermission(Permission permission) {
		if (!hasPermissionSpecific(permission)) {
			try {
				GoldenApple.getInstance().database.execute("INSERT INTO UserPermissions (UserID, Permission) VALUES (?, ?)", id, permission.getFullName());
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Error while adding permission '" + permission.getFullName() + "' to user '" + name + "':");
				GoldenApple.log(Level.SEVERE, e);
			}
		}
	}

	@Override
	public void addPermission(String permission) {
		addPermission(GoldenApple.getInstance().permissions.getPermissionByName(permission));
	}

	@Override
	public void removePermission(Permission permission) {
		if (hasPermissionSpecific(permission)) {
			try {
				GoldenApple.getInstance().database.execute("DELETE FROM UserPermissions WHERE UserID=? AND Permission=?", id, permission.getFullName());
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Error while removing permission '" + permission.getFullName() + "' from user '" + name + "':");
				GoldenApple.log(Level.SEVERE, e);
			}
		}
	}

	@Override
	public void removePermission(String permission) {
		removePermission(GoldenApple.getInstance().permissions.registerPermission(permission));
	}
	
	@Override
	public List<Long> getParentGroups(boolean directOnly) {
		try {
			List<Long> gr = new ArrayList<Long>();
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT GroupID FROM GroupUserMembers WHERE MemberID=?", id);
				while (r.next())
					gr.add(r.getLong("GroupID"));
			} finally {
				if (r != null)
					r.close();
			}
			
			if (!directOnly) {
				for (int i = 0; i < gr.size(); i++) {
					r = null;
					try {
						r = GoldenApple.getInstance().database.executeQuery("SELECT GroupID FROM GroupGroupMembers WHERE MemberID=?", gr.get(i));
						while (r.next())
							gr.add(r.getLong("GroupID"));
					} finally {
						if (r != null)
							r.close();
					}
				}
			}
			
			return gr;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "An error occurred while calculating group inheritance for user '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
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
