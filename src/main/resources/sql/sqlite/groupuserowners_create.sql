CREATE TABLE IF NOT EXISTS GroupUserOwners (
	GroupID BIGINT NOT NULL,
	OwnerID BIGINT NOT NULL,
	CONSTRAINT fk_groupuserowners_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_groupuserowners_ownerid
		FOREIGN KEY (OwnerID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_groupuserowners_groupid ON GroupUserOwners (GroupID ASC);
CREATE INDEX ind_groupuserowners_ownerid ON GroupUserOwners (OwnerID ASC);