package com.bendude56.goldenapple.chat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class DatabaseChatChannel extends ChatChannel {
	
	public DatabaseChatChannel(ResultSet r) throws SQLException {
		super(r.getString("Identifier"));
		this.displayName = r.getString("DisplayName");
		this.motd = r.getString("MOTD");
		this.defaultLevel = ChatChannelUserLevel.getLevel(r.getInt("DefaultLevel"));
		this.censor = r.getBoolean("StrictCensor") ? ChatCensor.strictChatCensor : ChatCensor.defaultChatCensor;
	}

	@Override
	public boolean isTemporary() {
		return false;
	}
	
	@Override
	public void save() {
		try {
			GoldenApple.getInstance().database.execute("UPDATE Channels SET DisplayName=?, MOTD=?, StrictCensor=?, DefaultLevel=? WHERE Identifier=?", this.displayName, this.motd, this.censor == ChatCensor.strictChatCensor, this.defaultLevel.id, this.name);
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to save channel '" + this.name + "' to database:");
			GoldenApple.log(Level.SEVERE, e);
		}
	}
	
	@Override
	public void delete() {
		try {
			GoldenApple.getInstance().database.execute("DELETE FROM Channels WHERE Identifier=?", this.name);
			super.delete();
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to delete channel '" + this.name + "' from the database:");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	@Override
	public ChatChannelUserLevel getSpecificLevel(User user) {
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT AccessLevel FROM ChannelUsers WHERE Channel=? AND UserID=?", this.name, user.getId());
			try {
				if (r.next()) {
					return ChatChannelUserLevel.getLevel(r.getInt("AccessLevel"));
				} else {
					return ChatChannelUserLevel.UNKNOWN;
				}
			} finally {
				r.close();
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Unable to retrieve channel access level for user '" + user.getName() + "' in channel '" + this.name + "':");
			GoldenApple.log(Level.WARNING, e);
			return ChatChannelUserLevel.UNKNOWN;
		}
	}

	@Override
	public ChatChannelUserLevel getGroupLevel(long group) {
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT AccessLevel FROM ChannelGroups WHERE Channel=? AND GroupID=?", this.name, group);
			try {
				if (r.next()) {
					return ChatChannelUserLevel.getLevel(r.getInt("AccessLevel"));
				} else {
					return ChatChannelUserLevel.UNKNOWN;
				}
			} finally {
				r.close();
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Unable to retrieve channel access level for group '" + group + "' in channel '" + this.name + "':");
			GoldenApple.log(Level.WARNING, e);
			return ChatChannelUserLevel.UNKNOWN;
		}
	}

	@Override
	public void setUserLevel(long user, ChatChannelUserLevel level) {
		try {
			if (level == ChatChannelUserLevel.UNKNOWN) {
				GoldenApple.getInstance().database.execute("DELETE FROM ChannelUsers WHERE Channel=? AND UserID=?", this.name, user);
			} else {
				ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM ChannelUsers WHERE Channel=? AND UserID=?", this.name, user);
				try {
					if (r.next())
						GoldenApple.getInstance().database.execute("UPDATE ChannelUsers SET AccessLevel=? WHERE Channel=? AND UserID=?", level.id, this.name, user);
					else
						GoldenApple.getInstance().database.execute("INSERT INTO ChannelUsers (AccessLevel, Channel, UserID) VALUES (?, ?, ?)", level.id, this.name, user);
				} finally {
					r.close();
				}
				if (User.hasUserInstance(user)) {
					User u = User.getUser(user);
					if (connectedUsers.containsKey(u)) {
						connectedUsers.put(u, level);
					}
				}
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to update user " + user + " in channel '" + this.name + "' in database:");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	@Override
	public void setGroupLevel(long group, ChatChannelUserLevel level) {
		try {
			if (level == ChatChannelUserLevel.UNKNOWN) {
				GoldenApple.getInstance().database.execute("DELETE FROM ChannelGroups WHERE Channel=? AND GroupID=?", this.name, group);
			} else {
				ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM ChannelGroups WHERE Channel=? AND GroupID=?", this.name, group);
				try {
					if (r.next())
						GoldenApple.getInstance().database.execute("UPDATE ChannelGroups SET AccessLevel=? WHERE Channel=? AND GroupID=?", level.id, this.name, group);
					else
						GoldenApple.getInstance().database.execute("INSERT INTO ChannelGroups (AccessLevel, Channel, GroupID) VALUES (?, ?, ?)", level.id, this.name, group);
				} finally {
					r.close();
				}
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to update group " + group + " in channel '" + this.name + "' in database:");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

}
