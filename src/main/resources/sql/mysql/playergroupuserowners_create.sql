CREATE TABLE IF NOT EXISTS PlayerGroupUserOwners (
	GroupID BIGINT NOT NULL,
	OwnerID BIGINT NOT NULL,
	INDEX ind_playergroupuserowners_groupid (GroupID ASC),
	CONSTRAINT fk_playergroupuserowners_groupid
		FOREIGN KEY (GroupID)
		REFERENCES PlayerGroups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	INDEX ind_playergroupuserowners_ownerid (OwnerID ASC),
	CONSTRAINT fk_playergroupuserowners_ownerid
		FOREIGN KEY (OwnerID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB