package com.bendude56.goldenapple.punish.audit;

import java.util.HashMap;

public class MuteEvent extends PunishEvent {
	public String duration;
	public String reason;
	public String channel;

	public MuteEvent(String authorizingUser, String target, String duration, String reason, String channel) {
		super(301, AuditEventLevel.WARNING, authorizingUser, target);
		
		this.duration = duration;
		this.reason = reason;
		this.channel = channel;
	}

	@Override
	protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
		super.loadMetadata(metadata);
		
		this.duration = metadata.get("duration").valueString;
		this.reason = metadata.get("reason").valueString;
		this.channel = metadata.get("channel").valueString;
	}

	@Override
	protected HashMap<String, AuditMetadata> saveMetadata() {
		HashMap<String, AuditMetadata> metadata = super.saveMetadata();
		
		metadata.put("duration", createMetadata("duration", duration));
		metadata.put("reason", createMetadata("reason", reason));
		metadata.put("channel", createMetadata("channel", channel));
		
		return metadata;
	}

	@Override
	public String formatMessage() {
		return this.authorizingUser + " has muted " + this.target + " from " + channel + " (Duration: " + this.duration + "): " + this.reason;
	}

}
