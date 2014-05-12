CREATE TABLE IF NOT EXISTS GroupUserMembers (
	GroupID INTEGER NOT NULL,
	MemberID INTEGER NOT NULL,
	CONSTRAINT fk_groupusermembers_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_groupusermembers_memberid
		FOREIGN KEY (MemberID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_groupusermembers_groupid ON GroupUserMembers (GroupID ASC);
CREATE INDEX ind_groupusermembers_memberid ON GroupUserMembers (MemberID ASC);
