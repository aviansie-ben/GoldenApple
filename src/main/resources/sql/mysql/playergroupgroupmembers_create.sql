CREATE TABLE IF NOT EXISTS PlayerGroupGroupMembers (
	GroupID BIGINT NOT NULL,
	MemberID BIGINT NOT NULL,
	INDEX ind_playergroupgroupmembers_groupid (GroupID ASC),
	CONSTRAINT fk_groupgroupmembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES PlayerGroups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	INDEX ind_playergroupgroupmembers_memberid (MemberID ASC),
	CONSTRAINT fk_playergroupgroupmembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB