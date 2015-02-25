CREATE TABLE IF NOT EXISTS PlayerGroupGroupMembers (
	GroupID INTEGER NOT NULL,
	MemberID INTEGER NOT NULL,
	CONSTRAINT fk_playergroupgroupmembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES PlayerGroups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_playergroupgroupmembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_playergroupgroupmembers_groupid ON PlayerGroupGroupMembers (GroupID ASC);
CREATE INDEX ind_playergroupgroupmembers_memberid ON PlayerGroupGroupMembers (MemberID ASC);
