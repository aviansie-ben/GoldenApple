package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

/**
 * Represents a group in the GoldenApple permissions database.
 * <p>
 * <em><strong>Note:</strong> Do not store direct references to this class. Store the
 * ID of the instance instead!</em>
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class PermissionGroup implements IPermissionObject {
	private long	id;
	private String	name;

	protected PermissionGroup(long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Pushes any changes made to this group to the SQL database
	 */
	public void save() {
		try {
			GoldenApple.getInstance().database.execute("UPDATE Groups SET Name=? WHERE ID=?", name, id);
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to save changes to group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	@Override
	public long getId() {
		return id;
	}

	/**
	 * Gets the name of the group represented by this instance.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets a list of user IDs for users that inherit this group's permissions.
	 */
	public List<Long> getUsers() {
		try {
			List<Long> users = new ArrayList<Long>();
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT MemberID FROM GroupUserMembers WHERE GroupID=?", id);
				while (r.next()) {
					users.add(r.getLong("MemberID"));
				}
			} finally {
				if (r != null)
					r.close();
			}
			
			return users;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to retrieve users for group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}
	
	public List<Long> getAllUsers() {
		try {
			List<Long> users = getUsers();
			for (long g : getAllGroups()) {
				ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT MemberID FROM GroupUserMembers WHERE GroupID=?", g);
				try {
					while (r.next())
						users.add(r.getLong("MemberID"));
				} finally {
					r.close();
				}
			}
			return users;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to retrieve users for group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}

	public void addUser(IPermissionUser user) {
		if (!isMember(user, true)) {
			try {
				GoldenApple.getInstance().database.execute("INSERT INTO GroupUserMembers (GroupID, MemberID) VALUES (?, ?)", id, user.getId());
				if (user instanceof User)
					((User)user).registerBukkitPermissions();
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to add user '" + user.getName() + "' to group '" + name + "':");
				GoldenApple.log(Level.SEVERE, e);
			}
		}
	}

	public void removeUser(IPermissionUser user) {
		if (isMember(user, true)) {
			try {
				GoldenApple.getInstance().database.execute("DELETE FROM GroupUserMembers WHERE GroupID=? AND MemberID=?", id, user.getId());
				if (user instanceof User)
					((User)user).registerBukkitPermissions();
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to remove user '" + user.getName() + "' from group '" + name + "':");
				GoldenApple.log(Level.SEVERE, e);
			}
		}
	}

	public boolean isMember(IPermissionUser user, boolean directOnly) {
		try {
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM GroupUserMembers WHERE GroupID=? AND MemberID=?", id, user.getId());
				if (r.next())
					return true;
			} finally {
				if (r != null)
					r.close();
			}
			
			if (!directOnly) {
				for (Long g : getParentGroups(true)) {
					r = null;
					
					try {
						r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM GroupUserMembers WHERE GroupID=? AND MemberID=?", g, user.getId());
						if (r.next())
							return true;
					} finally {
						if (r != null)
							r.close();
					}
				}
			}
			
			return false;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to determine if user '" + user.getName() + "' is in group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return false;
		}
	}

	/**
	 * Gets a list of group IDs for groups that inherit this group's
	 * permissions.
	 */
	public List<Long> getGroups() {
		try {
			List<Long> groups = new ArrayList<Long>();
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT MemberID FROM GroupGroupMembers WHERE GroupID=?", id);
				while (r.next()) {
					groups.add(r.getLong("MemberID"));
				}
			} finally {
				if (r != null)
					r.close();
			}
			
			return groups;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to retrieve sub-groups for group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}
	
	public List<Long> getAllGroups() {
		try {
			List<Long> groups = getGroups();
			for (int i = 0; i < groups.size(); i++) {
				ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT MemberID FROM GroupGroupMembers WHERE GroupID=?", groups.get(i));
				try {
					while (r.next())
						groups.add(r.getLong("MemberID"));
				} finally {
					r.close();
				}
			}
			return groups;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to retrieve sub-groups for group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}

	public void addGroup(PermissionGroup group) {
		if (!isMember(group, true)) {
			try {
				GoldenApple.getInstance().database.execute("INSERT INTO GroupGroupMembers (GroupID, MemberID) VALUES (?, ?)", id, group.getId());
				for (long id : group.getAllUsers())
					User.refreshPermissions(id);
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to add group '" + group.getName() + "' to group '" + name + "':");
				GoldenApple.log(Level.SEVERE, e);
			}
		}
	}

	public void removeGroup(PermissionGroup group) {
		if (isMember(group, true)) {
			try {
				GoldenApple.getInstance().database.execute("DELETE FROM GroupGroupMembers WHERE GroupID=? AND MemberID=?", id, group.getId());
				for (long id : group.getAllUsers())
					User.refreshPermissions(id);
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to remove group '" + group.getName() + "' from group '" + name + "':");
				GoldenApple.log(Level.SEVERE, e);
			}
		}
	}
	
	public boolean isMember(PermissionGroup group, boolean directOnly) {
		try {
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM GroupGroupMembers WHERE GroupID=? AND MemberID=?", id, group.getId());
				if (r.next())
					return true;
			} finally {
				if (r != null)
					r.close();
			}
			
			if (!directOnly) {
				for (Long g : getParentGroups(true)) {
					r = null;
					
					try {
						r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM GroupGroupMembers WHERE GroupID=? AND MemberID=?", g, group.getId());
						if (r.next())
							return true;
					} finally {
						if (r != null)
							r.close();
					}
				}
			}
			
			return false;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to determine if group '" + group.getName() + "' is in group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return false;
		}
	}

	@Override
	public List<Permission> getPermissions(boolean inherited) {
		try {
			List<Long> gr = getParentGroups(inherited);
			List<Permission> permissions = new ArrayList<Permission>();
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT Permission FROM GroupPermissions WHERE GroupID=?", id);
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
			GoldenApple.log(Level.SEVERE, "Failed to calculate permissions for group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}

	@Override
	public void addPermission(Permission permission) {
		if (!hasPermissionSpecific(permission)) {
			try {
				GoldenApple.getInstance().database.execute("INSERT INTO GroupPermissions (GroupID, Permission) VALUES (?, ?)", id, permission.getFullName());
				for (long id : getAllUsers()) {
					User u = User.getUser(id);
					if (u != null)
						u.getPermissionAttachment().setPermission(permission.getFullName(), true);
				}
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Error while adding permission '" + permission.getFullName() + "' to group '" + name + "':");
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
				GoldenApple.getInstance().database.execute("DELETE FROM GroupPermissions WHERE GroupID=? AND Permission=?", id, permission.getFullName());
				for (long id : getAllUsers()) {
					User u = User.getUser(id);
					if (u != null)
						u.getPermissionAttachment().unsetPermission(permission.getFullName());
				}
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Error while removing permission '" + permission.getFullName() + "' from group '" + name + "':");
				GoldenApple.log(Level.SEVERE, e);
			}
		}
	}

	@Override
	public void removePermission(String permission) {
		removePermission(GoldenApple.getInstance().permissions.registerPermission(permission));
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the group has the specified permission, false otherwise.
	 */
	public boolean hasPermission(String permission) {
		return hasPermission(permission, false);
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the group has the specified permission, false otherwise.
	 */
	public boolean hasPermission(Permission permission) {
		return hasPermission(permission, false);
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this group will be checked. If false, all permissions
	 *            (including indirect permissions) will be considered.
	 * @return True if the group has the specified permission, false otherwise.
	 */
	public boolean hasPermission(String permission, boolean specific) {
		return hasPermission(GoldenApple.getInstance().permissions.registerPermission(permission), specific);
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this group will be checked. If false, all permissions
	 *            (including indirect permissions) will be considered.
	 * @return True if the group has the specified permission, false otherwise.
	 */
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
				r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM GroupPermissions WHERE GroupID=? AND Permission=?", id, permission.getFullName());
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
	public List<Long> getParentGroups(boolean directOnly) {
		try {
			List<Long> gr = new ArrayList<Long>();
			ResultSet r = null;
			try {
				r = GoldenApple.getInstance().database.executeQuery("SELECT GroupID FROM GroupGroupMembers WHERE MemberID=?", id);
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
			GoldenApple.log(Level.SEVERE, "An error occurred while calculating group inheritance for group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}
}
