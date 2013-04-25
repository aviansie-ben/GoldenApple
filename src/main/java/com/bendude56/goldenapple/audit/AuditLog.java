package com.bendude56.goldenapple.audit;

import java.io.FileWriter;

import com.bendude56.goldenapple.GoldenApple;

public class AuditLog {
	private AuditLog() { }
	
	private static boolean auditStarted = false;
	private static FileWriter log;
	
	public static void initAuditLog() {
		try {
			Class.forName("com.bendude56.goldenapple.audit.AuditEvent");
			
			GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("auditlog");
			GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("auditlogparams");
			
			String logLocation = GoldenApple.getInstanceMainConfig().getString("modules.audit.textAuditLog", "plugins/GoldenApple/audit.log");
			if (logLocation != "") {
				log = new FileWriter(logLocation, true);
			}
			
			auditStarted = true;
			logEvent(new AuditStartEvent());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void deinitAuditLog() {
		try {
			logEvent(new AuditStopEvent());
			
			auditStarted = false;
			log.close();
			log = null;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void logEvent(AuditEvent e) {
		if (!auditStarted)
			return;
		try {
			e.save();
			if (log != null) {
				log.append(e.toString() + "\n");
				log.flush();
			}
		} catch (Throwable ex) {
			GoldenApple.log("Failed to log an audit event (" + e.eventId + "):");
			GoldenApple.log(ex);
		}
	}
}
