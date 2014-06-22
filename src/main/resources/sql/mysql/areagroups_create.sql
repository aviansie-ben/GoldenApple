CREATE TABLE IF NOT EXISTS AreaGroups (
	AreaID BIGINT NOT NULL,
	GroupID BIGINT NOT NULL,
	AccessLevel INT NOT NULL,
	PRIMARY KEY (AreaId, GroupId),
	INDEX ind_areagroups_areaid (AreaID ASC),
	INDEX ind_areagroups_groupsid (GroupID ASC),
	CONSTRAINT fk_areagroups_areaid
		FOREIGN KEY (AreaID)
		REFERENCES Areas(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_areagroups_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB