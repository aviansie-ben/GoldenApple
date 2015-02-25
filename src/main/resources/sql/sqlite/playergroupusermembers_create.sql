CREATE TABLE IF NOT EXISTS PlayerGroupUserMembers (
	GroupID INTEGER NOT NULL,
	MemberID INTEGER NOT NULL,
	CONSTRAINT fk_playergroupusermembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES PlayerGroups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_playergroupusermembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_playergroupusermembers_groupid ON PlayerGroupUserMembers (GroupID ASC);
CREATE INDEX ind_playergroupusermembers_memberid ON PlayerGroupUserMembers (MemberID ASC);
