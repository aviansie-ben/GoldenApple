package com.bendude56.goldenapple.audit;

import java.util.HashMap;

public class ModuleDisableEvent extends AuditEvent {
	
	public String module;

	public ModuleDisableEvent() {
		super(103, AuditEventLevel.INFO, "Base");
	}
	
	public ModuleDisableEvent(String module) {
		this();
		this.module = module;
	}

	@Override
	protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
		this.module = metadata.get("module").valueString;
	}

	@Override
	protected HashMap<String, AuditMetadata> saveMetadata() {
		HashMap<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
		metadata.put("module", createMetadata("module", module));
		return metadata;
	}

	@Override
	public String formatMessage() {
		return "Module '" + module + "' has been stopped.";
	}

}
