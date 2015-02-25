package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public abstract class AbstractGroup implements IGroup {
    private List<Long> users;
    private List<Long> groups;
    private List<Long> owners;
    
    protected abstract List<Long> loadUsers();
    protected abstract List<Long> loadGroups();
    protected abstract List<Long> loadOwners();
    
    protected abstract void addUserToDatabase(IPermissionUser user);
    protected abstract void removeUserFromDatabase(IPermissionUser user);
    
    protected abstract void addGroupToDatabase(IPermissionGroup group);
    protected abstract void removeGroupFromDatabase(IPermissionGroup group);
    
    protected abstract void addOwnerToDatabase(IPermissionUser owner);
    protected abstract void removeOwnerFromDatabase(IPermissionUser owner);
    
    @Override
    public List<Long> getUsers() {
        return Collections.unmodifiableList(users);
    }
    
    @Override
    public List<Long> getAllUsers() {
        ArrayList<Long> users = new ArrayList<Long>();
        users.addAll(getUsers());
        for (long g : getAllGroups()) {
            users.addAll(PermissionManager.getInstance().getGroup(g).getUsers());
        }
        return users;
    }
    
    @Override
    public void addUser(IPermissionUser user) {
        if (!isMember(user, true)) {
            this.addUserToDatabase(user);
            users.add(user.getId());
            
            user.reloadFromDatabase();
            if (user instanceof User) {
                ((User) user).registerBukkitPermissions();
            }
        }
    }
    
    @Override
    public void removeUser(IPermissionUser user) {
        if (isMember(user, true)) {
            this.removeUserFromDatabase(user);
            users.remove(user.getId());
            
            user.reloadFromDatabase();
            if (user instanceof User) {
                ((User) user).registerBukkitPermissions();
            }
        }
    }
    
    @Override
    public boolean isMember(IPermissionUser user, boolean directOnly) {
        if (users.contains(user.getId())) {
            return true;
        }
        
        if (!directOnly) {
            for (Long g : getAllGroups()) {
                if (PermissionManager.getInstance().getGroup(g).isMember(user, true)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public List<Long> getOwners() {
        return Collections.unmodifiableList(owners);
    }
    
    @Override
    public void addOwner(IPermissionUser owner) {
        if (!isOwner(owner)) {
            this.addOwnerToDatabase(owner);
            owners.add(owner.getId());
        }
    }
    
    @Override
    public void removeOwner(IPermissionUser owner) {
        if (isOwner(owner)) {
            this.removeOwnerFromDatabase(owner);
            owners.remove(owner.getId());
        }
    }
    
    @Override
    public boolean isOwner(IPermissionUser user) {
        return owners.contains(user.getId());
    }
    
    @Override
    public List<Long> getGroups() {
        return Collections.unmodifiableList(groups);
    }
    
    @Override
    public List<Long> getAllGroups() {
        ArrayList<Long> groups = new ArrayList<Long>();
        groups.addAll(getGroups());
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i) == getId() && this instanceof IPermissionGroup) {
                GoldenApple.log(Level.WARNING, "Recursive group membership: '" + getName() + "' is a member of itself!");
            } else {
                groups.addAll(PermissionManager.getInstance().getGroup(groups.get(i)).getGroups());
            }
        }
        return groups;
    }
    
    @Override
    public void addGroup(IPermissionGroup group) {
        if (!isMember(group, true)) {
            this.removeGroupFromDatabase(group);
            groups.add(group.getId());
            
            group.reloadFromDatabase();
            for (long id : group.getAllUsers()) {
                User.refreshPermissions(id);
            }
        }
    }
    
    @Override
    public void removeGroup(IPermissionGroup group) {
        if (isMember(group, true)) {
            this.removeGroupFromDatabase(group);
            groups.remove(group.getId());
            
            group.reloadFromDatabase();
            for (long id : group.getAllUsers()) {
                User.refreshPermissions(id);
            }
        }
    }
    
    @Override
    public boolean isMember(IPermissionGroup group, boolean directOnly) {
        if (groups.contains(group.getId())) {
            return true;
        }
        
        if (!directOnly) {
            for (Long g : getAllGroups()) {
                if (PermissionManager.getInstance().getGroup(g).isMember(group, true)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public void reloadFromDatabase() {
        users = loadUsers();
        groups = loadGroups();
        owners = loadOwners();
    }
    
}
