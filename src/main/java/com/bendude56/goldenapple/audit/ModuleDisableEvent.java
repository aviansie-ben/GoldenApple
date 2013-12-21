package com.bendude56.goldenapple.audit;

import java.util.HashMap;

public class ModuleDisableEvent extends AuditEvent {
	
	public String module;
	public String authorizingUser;

	public ModuleDisableEvent() {
		super(103, AuditEventLevel.INFO, "Base");
	}
	
	@Deprecated
	public ModuleDisableEvent(String module) {
		this(module, "Unknown");
	}
	
	public ModuleDisableEvent(String module, String authorizingUser) {
		this();
		this.module = module;
		this.authorizingUser = authorizingUser;
	}

	@Override
	protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
		this.module = metadata.get("module").valueString;
		this.authorizingUser = (metadata.containsKey("authorizingUser")) ? metadata.get(authorizingUser).valueString : "Unknown";
	}

	@Override
	protected HashMap<String, AuditMetadata> saveMetadata() {
		HashMap<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
		metadata.put("module", createMetadata("module", module));
		metadata.put("authorizingUser", createMetadata("authorizingUser", authorizingUser));
		return metadata;
	}

	@Override
	public String formatMessage() {
		return "Module '" + module + "' has been stopped by " + authorizingUser + ".";
	}

}
