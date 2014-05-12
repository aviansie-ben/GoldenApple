CREATE TABLE IF NOT EXISTS GroupGroupMembers (
	GroupID INTEGER NOT NULL,
	MemberID INTEGER NOT NULL,
	CONSTRAINT fk_groupgroupmembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_groupgroupmembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_groupgroupmembers_groupid ON GroupGroupMembers (GroupID ASC);
CREATE INDEX ind_groupgroupmembers_memberid ON GroupGroupMembers (MemberID ASC);
