CREATE TABLE IF NOT EXISTS PlayerGroupUserMembers (
	GroupID BIGINT NOT NULL,
	MemberID BIGINT NOT NULL,
	INDEX ind_playergroupusermembers_groupid (GroupID ASC),
	CONSTRAINT fk_playergroupusermembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES PlayrGroups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	INDEX ind_playergroupusermembers_memberid (MemberID ASC),
	CONSTRAINT fk_playergroupusermembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB