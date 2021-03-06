package com.bendude56.goldenapple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public class User implements IPermissionUser {
    private static HashMap<Long, User> activeUsers = new HashMap<Long, User>();
    private static User consoleUser = new User(-1, Bukkit.getConsoleSender(), false);
    
    private static List<String> globalNegative = new ArrayList<String>();
    
    public static void setGlobalNegative(String permission) {
        if (!globalNegative.contains(permission)) {
            globalNegative.add(permission);
            
            for (Map.Entry<Long, User> u : activeUsers.entrySet()) {
                u.getValue().registerBukkitPermissions();
            }
        }
    }
    
    public static void unsetGlobalNegative(String permission) {
        if (globalNegative.contains(permission)) {
            globalNegative.remove(permission);
            
            for (Map.Entry<Long, User> u : activeUsers.entrySet()) {
                u.getValue().registerBukkitPermissions();
            }
        }
    }
    
    public static void resetGlobalNegative() {
        while (globalNegative.size() > 0) {
            globalNegative.remove(0);
        }
        
        for (Map.Entry<Long, User> u : activeUsers.entrySet()) {
            u.getValue().registerBukkitPermissions();
        }
    }
    
    public static List<User> getOnlineUsers() {
        return Collections.unmodifiableList(new ArrayList<User>(activeUsers.values()));
    }
    
    public static User getConsoleUser() {
        return consoleUser;
    }
    
    /**
     * Gets a user instance from a {@link org.bukkit.command.CommandSender} for
     * use with other GoldenApple functions. If that
     * {@link org.bukkit.command.CommandSender} doesn't have a user instance
     * already associated with it, one will be automatically created.
     * 
     * @param sender The instance that the returned user should be based upon
     */
    public static User getUser(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return consoleUser;
        } else if (sender instanceof BlockCommandSender) {
            return consoleUser;
        } else if (PermissionManager.getInstance() == null) {
            // Assign each user a temporary id in the event of a permissions
            // system failure
            long id = -1;
            for (Entry<Long, User> cached : activeUsers.entrySet()) {
                if (cached.getKey() > id) {
                    id = cached.getKey() + 1;
                }
                if (cached.getValue().getHandle().equals(sender)) {
                    return cached.getValue();
                }
            }
            User u;
            activeUsers.put(id, u = new User(id, sender, false));
            return u;
        }
        long id = PermissionManager.getInstance().getUserId(((Player) sender).getUniqueId());
        if (id == -1) {
            return null;
        } else if (activeUsers.containsKey(id)) {
            return activeUsers.get(id);
        } else {
            User u;
            activeUsers.put(id, u = new User(id, sender, true));
            PermissionManager.getInstance().setUserSticky(u.getId(), true);
            return u;
        }
    }
    
    /**
     * @deprecated {@link com.bendude56.goldenapple.User#findUser(String)} is
     * preferred for commands and
     * {@link com.bendude56.goldenapple.User#getUser(CommandSender)} elsewhere.
     */
    @Deprecated
    public static User getUser(String name) {
        if (PermissionManager.getInstance() == null) {
            for (Entry<Long, User> cached : activeUsers.entrySet()) {
                if (cached.getValue().getName().equals(name)) {
                    return cached.getValue();
                }
            }
            return null;
        } else {
            return getUser(PermissionManager.getInstance().getUserId(name));
        }
    }
    
    public static User findUser(String name) {
        User found = null;
        
        for (Entry<Long, User> cached : activeUsers.entrySet()) {
            if (cached.getValue().getName().toLowerCase().startsWith(name.toLowerCase())) {
                if (found != null) {
                    return null;
                }
                
                found = cached.getValue();
            }
        }
        
        return found;
    }
    
    public static User getUser(long id) {
        if (id == -1) {
            return consoleUser;
        } else if (activeUsers.containsKey(id)) {
            return activeUsers.get(id);
        } else {
            return null;
        }
    }
    
    /**
     * Unloads an instance of a User from memory. This method is designed for
     * use when a player is logging off <strong>only</strong>. If used
     * otherwise, unwanted side effects may occur.
     * 
     * @param user The user that should be unloaded
     */
    public static void unloadUser(User user) {
        activeUsers.remove(user.getId());
        if (PermissionManager.getInstance() != null) {
            PermissionManager.getInstance().setUserSticky(user.getId(), false);
        }
    }
    
    /**
     * Clears all User instances from memory. This should only be used if the
     * status of the permissions module is changing. Clearing the cache at other
     * times could have unintended side effects.
     */
    public static void clearCache() {
        activeUsers.clear();
    }
    
    public static void refreshPermissions(long id) {
        if (activeUsers.containsKey(id)) {
            activeUsers.get(id).registerBukkitPermissions();
        }
    }
    
    public static boolean hasUserInstance(long id) {
        return activeUsers.containsKey(id);
    }
    
    private IPermissionUser permissions;
    private PermissionAttachment bukkitPermissions;
    private CommandSender handle;
    private long id;
    
    private User(long id, CommandSender handle, boolean loadPermissions) {
        this.id = id;
        if (!loadPermissions) {
            permissions = null;
        } else {
            permissions = PermissionManager.getInstance().getUser(id);
            PermissionManager.getInstance().setUserSticky(id, true);
        }
        this.handle = handle;
        if (permissions != null && handle != null && handle instanceof Permissible) {
            registerBukkitPermissions();
        }
    }
    
    public void registerBukkitPermissions() {
        if (!(handle instanceof Permissible) || permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            if (bukkitPermissions != null) {
                bukkitPermissions.remove();
            }
            bukkitPermissions = handle.addAttachment(GoldenApple.getInstance());
            
            for (Permission p : getPermissions(true)) {
                bukkitPermissions.setPermission(p.getFullName(), true);
            }
            
            for (String p : globalNegative) {
                bukkitPermissions.setPermission(p, false);
            }
        }
    }
    
    public PermissionAttachment getPermissionAttachment() {
        return bukkitPermissions;
    }
    
    @Override
    public String getName() {
        if (handle instanceof ConsoleCommandSender) {
            return "Server";
        } else {
            return permissions.getName();
        }
    }
    
    @Override
    public String getLogName() {
        if (handle instanceof ConsoleCommandSender) {
            return "Server";
        } else {
            return permissions.getLogName();
        }
    }
    
    @Override
    public UUID getUuid() {
        if (handle instanceof ConsoleCommandSender) {
            throw new UnsupportedOperationException();
        } else if (permissions == null) {
            return getPlayerHandle().getUniqueId();
        } else {
            return permissions.getUuid();
        }
    }
    
    public String getDisplayName() {
        return getName();
    }
    
    public String getChatDisplayName() {
        String prefix = getPrefix();
        return (prefix == null) ? (getChatColor() + getName()) : (getChatColor() + "[" + prefix + "] " + getName());
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public List<Permission> getPermissions(boolean inherited) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            return permissions.getPermissions(inherited);
        }
    }
    
    @Override
    public boolean hasPermission(String permission) {
        if (handle instanceof ConsoleCommandSender) {
            return true;
        } else if (permissions == null) {
            return handle.isOp();
        } else {
            return permissions.hasPermission(permission);
        }
    }
    
    @Override
    public boolean hasPermission(Permission permission) {
        if (handle instanceof ConsoleCommandSender) {
            return true;
        } else if (permissions == null) {
            return handle.isOp();
        } else {
            return permissions.hasPermission(permission);
        }
    }
    
    @Override
    public boolean hasPermission(String permission, boolean inherited) {
        if (handle instanceof ConsoleCommandSender) {
            return !inherited;
        } else if (permissions == null) {
            return !inherited && handle.isOp();
        } else {
            return permissions.hasPermission(permission, inherited);
        }
    }
    
    @Override
    public boolean hasPermission(Permission permission, boolean inherited) {
        if (handle instanceof ConsoleCommandSender) {
            return !inherited;
        } else if (permissions == null) {
            return !inherited && handle.isOp();
        } else {
            return permissions.hasPermission(permission, inherited);
        }
    }
    
    @Override
    @Deprecated
    public String getPreferredLocale() {
        if (permissions == null) {
            return "";
        } else {
            return permissions.getPreferredLocale();
        }
    }
    
    @Override
    @Deprecated
    public void setPreferredLocale(String locale) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            permissions.setPreferredLocale(locale);
        }
    }
    
    /**
     * Gets the {@link org.bukkit.command.CommandSender} that is represented by
     * this instance.
     */
    public CommandSender getHandle() {
        return handle;
    }
    
    public void setHandle(CommandSender handle) {
        this.handle = handle;
        this.registerBukkitPermissions();
    }
    
    /**
     * Gets the {@link org.bukkit.entity.Player} that is represented by this
     * instance, if this instance represents a player who is currently online.
     * 
     * @throws UnsupportedOperationException Thrown if the user represented by
     * this instance is not a Player.
     */
    public Player getPlayerHandle() {
        if (handle instanceof Player) {
            return (Player) handle;
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void addPermission(Permission permission) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        }
        
        bukkitPermissions.setPermission(permission.getFullName(), true);
        permissions.addPermission(permission);
    }
    
    @Override
    public void addPermission(String permission) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        }
        
        bukkitPermissions.setPermission(permission, true);
        permissions.addPermission(permission);
    }
    
    @Override
    public void removePermission(Permission permission) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        }
        
        bukkitPermissions.unsetPermission(permission.getFullName());
        permissions.removePermission(permission);
    }
    
    @Override
    public void removePermission(String permission) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        }
        
        bukkitPermissions.unsetPermission(permission);
        permissions.removePermission(permission);
    }
    
    @Override
    @Deprecated
    public boolean isUsingComplexCommands() {
        if (permissions == null) {
            return true;
        }
        return permissions.isUsingComplexCommands();
    }
    
    @Override
    @Deprecated
    public void setUsingComplexCommands(boolean useComplex) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        }
        permissions.setUsingComplexCommands(useComplex);
    }
    
    @Override
    @Deprecated
    public boolean isAutoLockEnabled() {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        }
        return permissions.isAutoLockEnabled();
    }
    
    @Override
    @Deprecated
    public void setAutoLockEnabled(boolean autoLock) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        }
        permissions.setAutoLockEnabled(autoLock);
    }
    
    @Override
    public boolean hasPermissionSpecific(Permission permission) {
        if (permissions == null) {
            return false;
        } else {
            return permissions.hasPermissionSpecific(permission);
        }
    }
    
    @Override
    public List<Long> getParentGroups(boolean directOnly) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            return permissions.getParentGroups(directOnly);
        }
    }
    
    @Override
    public ChatColor getChatColor() {
        if (permissions == null) {
            return ChatColor.DARK_BLUE;
        } else {
            return permissions.getChatColor();
        }
    }
    
    @Override
    public String getPrefix() {
        if (permissions == null) {
            return "The Almighty";
        } else {
            return permissions.getPrefix();
        }
    }
    
    public boolean isServer() {
        return permissions == null;
    }
    
    public String getLocalizedMessage(String message, Object... args) {
        return GoldenApple.getInstance().getLocalizationManager().getLocale(this).getMessage(message, args);
    }
    
    public void sendMessage(String message) {
        if (isServer()) {
            GoldenApple.log(Level.INFO, message);
        } else {
            getPlayerHandle().sendMessage(message);
        }
    }
    
    public void sendLocalizedMessage(String message, Object... args) {
        sendMessage(getLocalizedMessage(message, args));
    }
    
    @Deprecated
    public void sendLocalizedMultilineMessage(String message, Object... args) {
        sendLocalizedMessage(message, args);
    }
    
    @Override
    public void reloadFromDatabase() {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            permissions.reloadFromDatabase();
        }
    }
    
    @Override
    public Map<String, String> getDefinedVariables() {
        if (permissions == null) {
            return Collections.unmodifiableMap(new HashMap<String, String>());
        } else {
            return permissions.getDefinedVariables();
        }
    }
    
    @Override
    public String getVariableString(String variableName) {
        if (permissions == null) {
            return getVariableSpecificString(variableName);
        } else {
            return permissions.getVariableString(variableName);
        }
    }
    
    @Override
    public Boolean getVariableBoolean(String variableName) {
        if (permissions == null) {
            return getVariableSpecificBoolean(variableName);
        } else {
            return permissions.getVariableBoolean(variableName);
        }
    }
    
    @Override
    public Integer getVariableInteger(String variableName) {
        if (permissions == null) {
            return getVariableSpecificInteger(variableName);
        } else {
            return permissions.getVariableInteger(variableName);
        }
    }
    
    @Override
    public String getVariableSpecificString(String variableName) {
        if (permissions == null) {
            return PermissionManager.getInstance().getVariableDefaultValue(variableName);
        } else {
            return permissions.getVariableSpecificString(variableName);
        }
    }
    
    @Override
    public Boolean getVariableSpecificBoolean(String variableName) {
        if (permissions == null) {
            if (PermissionManager.getInstance().getVariableDefaultValue(variableName) != null) {
                return PermissionManager.getInstance().getVariableDefaultValue(variableName).equalsIgnoreCase("true");
            } else {
                return null;
            }
        } else {
            return permissions.getVariableSpecificBoolean(variableName);
        }
    }
    
    @Override
    public Integer getVariableSpecificInteger(String variableName) {
        if (permissions == null) {
            if (PermissionManager.getInstance().getVariableDefaultValue(variableName) != null) {
                try {
                    return Integer.parseInt(PermissionManager.getInstance().getVariableDefaultValue(variableName));
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return permissions.getVariableSpecificInteger(variableName);
        }
    }
    
    @Override
    public void deleteVariable(String variableName) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            permissions.deleteVariable(variableName);
        }
    }
    
    @Override
    public void setVariable(String variableName, String value) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            permissions.setVariable(variableName, value);
        }
    }
    
    @Override
    public void setVariable(String variableName, Boolean value) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            permissions.setVariable(variableName, value);
        }
    }
    
    @Override
    public void setVariable(String variableName, Integer value) {
        if (permissions == null) {
            throw new UnsupportedOperationException();
        } else {
            permissions.setVariable(variableName, value);
        }
    }
}
