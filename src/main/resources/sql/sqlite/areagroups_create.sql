CREATE TABLE IF NOT EXISTS AreaGroups (
	AreaID INTEGER NOT NULL,
	GroupID INTEGER NOT NULL,
	AccessLevel INTEGER NOT NULL,
	PRIMARY KEY (AreaID, GroupID),
	CONSTRAINT fk_areagroups_areaid
		FOREIGN KEY (AreaID)
		REFERENCES Areas(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_areagroups_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_areagroups_areaid ON AreaGroups (AreaID ASC);
CREATE INDEX ind_areagroups_groupid ON AreaGroups (GroupID ASC);
