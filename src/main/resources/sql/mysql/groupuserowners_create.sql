CREATE TABLE IF NOT EXISTS GroupUserOwners (
	GroupID BIGINT NOT NULL,
	OwnerID BIGINT NOT NULL,
	INDEX ind_groupuserowners_groupid (GroupID ASC),
	CONSTRAINT fk_groupuserowners_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	INDEX ind_groupuserowners_ownerid (OwnerID ASC),
	CONSTRAINT fk_groupuserowners_ownerid
		FOREIGN KEY (OwnerID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB