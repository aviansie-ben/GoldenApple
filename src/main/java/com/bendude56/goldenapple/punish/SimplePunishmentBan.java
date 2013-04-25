package com.bendude56.goldenapple.punish;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class SimplePunishmentBan extends PunishmentBan {

	public SimplePunishmentBan(ResultSet r) throws SQLException {
		this.id = r.getLong("ID");
		this.targetId = r.getLong("Target");
		this.adminId = (r.getObject("Admin") == null) ? -1 : r.getLong("Admin");
		this.reason = r.getString("Reason");
		this.startTime = r.getTimestamp("StartTime");
		this.length = new RemainingTime(r.getLong("Duration"));
		this.voided = r.getBoolean("Voided");
		this.permanent = r.getObject("Duration") == null;
	}
	
	public SimplePunishmentBan(IPermissionUser target, IPermissionUser admin, String reason, RemainingTime duration) {
		this.targetId = target.getId();
		this.adminId = admin.getId();
		this.reason = reason;
		this.startTime = new Timestamp(System.currentTimeMillis());
		this.length = duration;
		this.voided = false;
		this.permanent = duration == null;
	}

	@Override
	public boolean update() {
		try {
			GoldenApple.getInstanceDatabaseManager().execute("UPDATE Bans SET Target=?, Admin=?, Reason=?, StartTime=?, Duration=?, Voided=? WHERE ID=?",
					targetId, (adminId <= 0) ? null : adminId, reason, startTime, (permanent) ? null : length.getTotalSeconds(), voided, id);
			return true;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to save changes to ban " + id + ":");
			GoldenApple.log(Level.SEVERE, e);
			return false;
		}
	}

	@Override
	public boolean insert() {
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Bans (Target, Admin, Reason, StartTime, Duration, Voided) VALUES (?, ?, ?, ?, ?, ?)",
					targetId, (adminId <= 0) ? null : adminId, reason, startTime, (permanent) ? null : length.getTotalSeconds(), voided);
			try {
				if (r.next()) {
					id = r.getLong(1);
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
			return true;
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create new ban entry:");
			GoldenApple.log(Level.SEVERE, e);
			return false;
		}
	}

}
