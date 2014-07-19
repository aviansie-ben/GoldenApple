package com.bendude56.goldenapple.chat;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class DatabaseChatChannel extends BaseAclChatChannel implements IPersistentChatChannel {
    
    public DatabaseChatChannel(ResultSet r) throws SQLException {
        super(r.getString("Identifier"));
        
        super.setDisplayName(r.getString("DisplayName"));
        super.setMotd(r.getString("MOTD"));
        super.setDefaultAccessLevel(ChatChannelAccessLevel.fromLevelId(r.getInt("DefaultLevel")));
        super.setCensor((r.getBoolean("StrictCensor")) ? SimpleChatCensor.strictChatCensor : SimpleChatCensor.defaultChatCensor);
        
        this.loadGroupLevels();
        this.loadUserLevels();
    }
    
    private void loadGroupLevels() {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM ChannelGroups WHERE Channel=?", getName());
            
            try {
                while (r.next()) {
                    super.setExplicitAccessLevel(PermissionManager.getInstance().getGroup(r.getLong("GroupID")), ChatChannelAccessLevel.fromLevelId(r.getInt("AccessLevel")));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load groups for channel '" + getName() + "'", e);
        }
    }
    
    private void loadUserLevels() {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM ChannelUsers WHERE Channel=?", getName());
            
            try {
                while (r.next()) {
                    super.setExplicitAccessLevel(PermissionManager.getInstance().getUser(r.getLong("UserID")), ChatChannelAccessLevel.fromLevelId(r.getInt("AccessLevel")));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users for channel '" + getName() + "'", e);
        }
    }
    
    @Override
    public void save() {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Channels SET DisplayName=?, MOTD=?, StrictCensor=?, DefaultLevel=? WHERE Identifier=?", getDisplayName(), getMotd(), getCensor() == SimpleChatCensor.strictChatCensor, getDefaultAccessLevel().getLevelId(), getName());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save channel '" + getName() + "'", e);
        }
    }
    
    @Override
    public void setExplicitAccessLevel(IPermissionObject obj, ChatChannelAccessLevel level) {
        try {
            if (obj instanceof IPermissionUser) {
                if (level != null && level != ChatChannelAccessLevel.NO_ACCESS && level.getLevelId() < calculateMinimumAccessLevel((IPermissionUser) obj).getLevelId()) {
                    throw new IllegalArgumentException("Requested access level is below minimum access level!");
                }
                
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM ChannelUsers WHERE Channel=? AND UserID=?", getName(), obj.getId());
                
                if (level != null && level != ChatChannelAccessLevel.NO_ACCESS) {
                    GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO ChannelUsers (Channel, UserID, AccessLevel) VALUES (?, ?, ?)", getName(), obj.getId(), level.getLevelId());
                }
                
                super.setExplicitAccessLevel(obj, level);
            } else if (obj instanceof IPermissionGroup) {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM ChannelGroups WHERE Channel=? AND GroupID=?", getName(), obj.getId());
                
                if (level != null && level != ChatChannelAccessLevel.NO_ACCESS) {
                    GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO ChannelGroups (Channel, GroupID, AccessLevel) VALUES (?, ?, ?)", getName(), obj.getId(), level.getLevelId());
                }
                
                super.setExplicitAccessLevel(obj, level);
            } else {
                throw new IllegalArgumentException("Unrecognized permission object type!");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update permissions for channel '" + getName() + "'", e);
        }
    }
    
    @Override
    public void delete() {
        super.delete();
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Channels WHERE Identifier=?", getName());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete channel '" + getName() + "'", e);
        }
    }
}
