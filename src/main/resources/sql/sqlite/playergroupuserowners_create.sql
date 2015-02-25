CREATE TABLE IF NOT EXISTS PlayerGroupUserOwners (
	GroupID BIGINT NOT NULL,
	OwnerID BIGINT NOT NULL,
	CONSTRAINT fk_playergroupuserowners_groupid
		FOREIGN KEY (GroupID)
		REFERENCES PlayerGroups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_playergroupuserowners_ownerid
		FOREIGN KEY (OwnerID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_playergroupuserowners_groupid ON PlayerGroupUserOwners (GroupID ASC);
CREATE INDEX ind_playergroupuserowners_ownerid ON PlayerGroupUserOwners (OwnerID ASC);