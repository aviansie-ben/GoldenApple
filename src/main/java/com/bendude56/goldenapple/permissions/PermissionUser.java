package com.bendude56.goldenapple.permissions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
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

	protected PermissionUser(long id, String name, String preferredLocale, String permissions) {
		this.id = id;
		this.name = name;
		this.preferredLocale = preferredLocale;
		try {
			@SuppressWarnings("unchecked")
			List<String> p = (List<String>)Serializer.deserialize(permissions);
			for (String permission : p) {
				this.permissions.add(GoldenApple.getInstance().permissions.registerPermission(permission));
			}
		} catch (Exception e) {
			GoldenApple.log(Level.SEVERE, "Failed to deserialize permissions for user " + name + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
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
			GoldenApple.getInstance().database.execute("UPDATE Users SET Locale=?, Permissions=? WHERE ID=?", new Object[] { preferredLocale, serializePermissions(), id });
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
		return getPermissions(!specific).contains(permission);
	}

	@Override
	public boolean hasPermission(String permission, boolean specific) {
		return hasPermission(GoldenApple.getInstance().permissions.registerPermission(permission), specific);
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
		addPermission(GoldenApple.getInstance().permissions.registerPermission(permission));
	}

	@Override
	public void remPermission(Permission permission) {
		if (permissions.contains(permission)) {
			permissions.remove(permission);
			save();
		}
	}

	@Override
	public void remPermission(String permission) {
		remPermission(GoldenApple.getInstance().permissions.registerPermission(permission));
	}
}
