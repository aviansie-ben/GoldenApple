package com.bendude56.goldenapple.punish;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class SimplePunishmentMute extends PunishmentMute {
	
	protected String channel;
	
	public SimplePunishmentMute(ResultSet r) throws SQLException {
		this.id = r.getLong("ID");
		this.targetId = r.getLong("Target");
		this.adminId = (r.getObject("Admin") == null) ? -1 : r.getLong("Admin");
		this.reason = r.getString("Reason");
		this.startTime = r.getTimestamp("StartTime");
		this.length = new RemainingTime(r.getLong("Duration"));
		this.voided = r.getBoolean("Voided");
		this.permanent = r.getObject("Duration") == null;
		this.channel = r.getString("Channel");
	}
	
	public SimplePunishmentMute(IPermissionUser target, IPermissionUser admin, String reason, RemainingTime duration, String channel) {
		this.targetId = target.getId();
		this.adminId = admin.getId();
		this.reason = reason;
		this.startTime = new Timestamp(System.currentTimeMillis());
		this.length = duration;
		this.voided = false;
		this.permanent = duration == null;
		this.channel = channel;
	}
	
	public boolean isGlobal() {
		return channel == null;
	}
	
	public String getChannelIdentifier() {
		return channel;
	}

	@Override
	public boolean update() {
		try {
			GoldenApple.getInstanceDatabaseManager().execute("UPDATE Mutes SET Target=?, Admin=?, Reason=?, StartTime=?, Duration=?, Voided=?, Channel=? WHERE ID=?",
					targetId, (adminId <= 0) ? null : adminId, reason, startTime, (permanent) ? null : length.getTotalSeconds(), voided, channel, id);
			return true;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to save changes to mute " + id + ":");
			GoldenApple.log(Level.SEVERE, e);
			return false;
		}
	}

	@Override
	public boolean insert() {
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Mutes (Target, Admin, Reason, StartTime, Duration, Voided, Channel) VALUES (?, ?, ?, ?, ?, ?, ?)",
					targetId, (adminId <= 0) ? null : adminId, reason, startTime, (permanent) ? null : length.getTotalSeconds(), voided, channel);
			try {
				if (r.next()) {
					id = r.getLong(1);
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
			return true;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create new mute entry:");
			GoldenApple.log(Level.SEVERE, e);
			return false;
		}
	}

}
