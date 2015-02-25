package com.bendude56.goldenapple.permissions;

public interface IPlayerGroup extends IGroup {
    public IPermissionUser getCreator();
    public long getCreatorId();
    
    public String getPartialName();
}
