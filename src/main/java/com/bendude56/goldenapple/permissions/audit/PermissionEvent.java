package com.bendude56.goldenapple.permissions.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.audit.AuditEvent;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public abstract class PermissionEvent extends AuditEvent {
	
	public String authorizingUser;
	public TargetType targetType;
	public String targetName;
	public long targetId;

	public PermissionEvent(int eventId, AuditEventLevel severity, String authorizingUser, IPermissionObject target) {
		super(eventId, severity, "Permissions");
		
		this.authorizingUser = authorizingUser;
		
		if (target instanceof IPermissionUser) {
			this.targetType = TargetType.USER;
			this.targetName = ((IPermissionUser) target).getName();
			this.targetId = target.getId();
		} else if (target instanceof IPermissionGroup) {
			this.targetType = TargetType.GROUP;
			this.targetName = ((IPermissionGroup) target).getName();
			this.targetId = target.getId();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
		authorizingUser = metadata.get("authorizingUser").valueString;
		targetType = TargetType.fromId(metadata.get("targetType").valueInt);
		targetName = metadata.get("targetName").valueString;
		targetId = metadata.get("targetId").valueInt;
	}

	@Override
	protected HashMap<String, AuditMetadata> saveMetadata() {
		HashMap<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
		
		metadata.put("authorizingUser", createMetadata("authorizingUser", authorizingUser));
		metadata.put("targetType", createMetadata("targetType", targetType.getId()));
		metadata.put("targetName", createMetadata("targetName", targetName));
		metadata.put("targetId", createMetadata("targetId", targetId));
		
		return metadata;
	}
	
	public enum TargetType {
		USER(0), GROUP(1);
		
		private final long id;
		
		private TargetType(long id) {
			this.id = id;
		}
		
		public long getId() {
			return id;
		}
		
		public static TargetType fromId(long id) {
			return (id == 0) ? USER : GROUP;
		}
	}

}
