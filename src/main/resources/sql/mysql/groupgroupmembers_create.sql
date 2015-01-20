CREATE TABLE IF NOT EXISTS GroupGroupMembers (
	GroupID BIGINT NOT NULL,
	MemberID BIGINT NOT NULL,
	INDEX ind_groupgroupmembers_groupid (GroupID ASC),
	CONSTRAINT fk_groupgroupmembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	INDEX ind_groupgroupmembers_memberid (MemberID ASC),
	CONSTRAINT fk_groupgroupmembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB