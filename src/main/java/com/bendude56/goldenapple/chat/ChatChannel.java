package com.bendude56.goldenapple.chat;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.util.Serializer;

public class ChatChannel implements IChatChannel {
	private final long							id;
	private String								name;
	private HashMap<Long, ChannelAccessLevel>	userAccess;
	private HashMap<Long, ChannelAccessLevel>	groupAccess;
	private boolean								strictCensor;
	
	public List<User>							spyUsers;
	
	@SuppressWarnings("unchecked")
	public ChatChannel(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
		this.id = r.getLong("ID");
		this.name = r.getString("Name");
		this.userAccess = (HashMap<Long, ChannelAccessLevel>) Serializer.deserialize(r.getString("UserAccess"));
		this.groupAccess = (HashMap<Long, ChannelAccessLevel>) Serializer.deserialize(r.getString("GroupAccess"));
		this.strictCensor = r.getBoolean("StrictCensor");
	}

	public ChatChannel(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}
	
	public boolean isTemporary() {
		return false;
	}

	public ChannelAccessLevel getSpecificAccess(IPermissionObject obj) {
		if (obj instanceof IPermissionUser) {
			return (userAccess.containsKey(obj.getId())) ? userAccess.get(obj.getId()) : ChannelAccessLevel.NONE;
		} else if (obj instanceof PermissionGroup) {
			return (groupAccess.containsKey(obj.getId())) ? groupAccess.get(obj.getId()) : ChannelAccessLevel.NONE;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public ChannelAccessLevel getAccess(IPermissionUser user) {
		return ChannelAccessLevel.NONE;
	}

	public void broadcastMessage(String message) {
		for (Player player : GoldenApple.getInstance().chat.getPlayers(this))
			player.sendMessage(message);
	}

	/**
	 * Represents the amount of access that a user will be granted when
	 * connecting to a specific chat channel
	 * 
	 * @author ben_dude56
	 */
	public enum ChannelAccessLevel {
		/**
		 * This user has been granted administrative permissions in all channels
		 * and can perform any function on any channel
		 */
		SUPER_ADMINISTRATOR(4, 4),
		/**
		 * This user has been granted moderator permissions in all channels, but
		 * has been granted administrative permissions for this channel only
		 */
		ADMINISTRATOR_SUPER_MODERATOR(4, 4),
		/**
		 * This user is an administrator, and can perform all channel functions,
		 * including promotion of other users to moderator, deletion of the
		 * channel, and renaming of the channel
		 */
		ADMINISTRATOR(4, 4),
		/**
		 * This user has been granted moderator permissions in all channels, and
		 * can perform moderator commands on any channel
		 */
		SUPER_MODERATOR(3, 3),
		/**
		 * This user is a moderator and can perform lower-level channel
		 * functions, including banning/muting users, allowing users into the
		 * channel, and activating channel slow mode
		 */
		MODERATOR(3, 3),
		/**
		 * This user can connect and chat in this chat channel, but does not
		 * have any additional privileges
		 */
		NORMAL(2, 2),
		/**
		 * This user can join the channel, but cannot talk in it
		 */
		GUEST(1, 1),
		/**
		 * This user CANNOT join the channel or perform any functions on the
		 * channel
		 */
		NONE(0, 0);

		public int	storageId;
		public int	accessLevel;

		private ChannelAccessLevel(int storageId, int accessLevel) {
			this.storageId = storageId;
			this.accessLevel = accessLevel;
		}
	}
}
