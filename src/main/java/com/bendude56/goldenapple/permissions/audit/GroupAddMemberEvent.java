package com.bendude56.goldenapple.permissions.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionObject;

public class GroupAddMemberEvent extends PermissionEvent {
	public String groupName;
	public long groupId;
	
	public GroupAddMemberEvent(String authorizingUser, IPermissionObject target, IPermissionGroup group) {
		super(402, AuditEventLevel.SEVERE, authorizingUser, target);
		
		this.groupName = group.getName();
		this.groupId = group.getId();
	}

	@Override
	public String formatMessage() {
		return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been added to group " + groupName + " (ID: " + groupId + ") by " + authorizingUser;
	}

	@Override
	protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
		super.loadMetadata(metadata);
		
		groupName = metadata.get("groupName").valueString;
		groupId = metadata.get("groupId").valueInt;
	}

	@Override
	protected HashMap<String, AuditMetadata> saveMetadata() {
		HashMap<String, AuditMetadata> metadata = super.saveMetadata();
		
		metadata.put("groupName", createMetadata("groupName", groupName));
		metadata.put("groupId", createMetadata("groupId", groupId));
		
		return metadata;
	}

}
