package com.bendude56.goldenapple.punish;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class SimplePunishmentWarning extends PunishmentWarning {
    
    public SimplePunishmentWarning(ResultSet r) throws SQLException {
        this.id = r.getLong("ID");
        this.targetId = r.getLong("Target");
        this.adminId = (r.getObject("Admin") == null) ? -1 : r.getLong("Admin");
        this.reason = r.getString("Reason");
        this.startTime = r.getTimestamp("Time");
        this.length = new RemainingTime(0);
        this.voided = false;
        this.permanent = true;
    }
    
    public SimplePunishmentWarning(IPermissionUser target, IPermissionUser admin, String reason) {
        this.targetId = target.getId();
        this.adminId = admin.getId();
        this.reason = reason;
        this.startTime = new Timestamp(System.currentTimeMillis());
        this.length = new RemainingTime(0);
        this.voided = false;
        this.permanent = true;
    }

    @Override
    public boolean update() {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Warnings SET Target=?, Admin=?, Reason=?, Time=?, WHERE ID=?",
                targetId, (adminId <= 0) ? null : adminId, reason, startTime, id);
            return true;
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to save changes to warning " + id + ":");
            GoldenApple.log(Level.SEVERE, e);
            return false;
        }
    }

    @Override
    public boolean insert() {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Warnings (Target, Admin, Reason, Time) VALUES (?, ?, ?, ?)",
                targetId, (adminId <= 0) ? null : adminId, reason, startTime);
            try {
                if (r.next()) {
                    id = r.getLong(1);
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            return true;
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to create new warning entry:");
            GoldenApple.log(Level.SEVERE, e);
            return false;
        }
    }
    
    @Override
    public boolean delete() {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Warnings WHERE ID=?", id);
            return true;
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to delete warning " + id + ":");
            GoldenApple.log(Level.SEVERE, e);
            return false;
        }
    }

    @Override
    public void voidPunishment() {
        throw new UnsupportedOperationException("Cannot void a warning!");
    }
    
    @Override
    public boolean isExpired() {
        return true;
    }
    
}
