package com.bendude56.goldenapple.audit;

import java.util.HashMap;

public class ModuleEnableEvent extends AuditEvent {
	
	public String module;

	public ModuleEnableEvent() {
		super(102, AuditEventLevel.INFO, "Base");
	}
	
	public ModuleEnableEvent(String module) {
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
		return "Module '" + module + "' has been started.";
	}

}
