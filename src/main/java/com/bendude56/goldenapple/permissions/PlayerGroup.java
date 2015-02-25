package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.util.List;

public class PlayerGroup extends AbstractGroup implements IPlayerGroup {
    
    protected PlayerGroup(ResultSet r) {
        // TODO Implement this
    }
    
    public PlayerGroup(long id, IPermissionUser creator, String name) {
        // TODO Implement this
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IPermissionUser getCreator() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public long getCreatorId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPartialName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<Long> loadUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<Long> loadGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<Long> loadOwners() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void addUserToDatabase(IPermissionUser user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void removeUserFromDatabase(IPermissionUser user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void addGroupToDatabase(IPermissionGroup group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void removeGroupFromDatabase(IPermissionGroup group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void addOwnerToDatabase(IPermissionUser owner) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void removeOwnerFromDatabase(IPermissionUser owner) {
        // TODO Auto-generated method stub
        
    }
}
