CREATE TABLE IF NOT EXISTS GroupUserMembers (
	GroupID BIGINT NOT NULL,
	MemberID BIGINT NOT NULL,
	INDEX ind_groupusermembers_groupid (GroupID ASC),
	CONSTRAINT fk_groupusermembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	INDEX ind_groupusermembers_memberid (MemberID ASC),
	CONSTRAINT fk_groupusermembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB