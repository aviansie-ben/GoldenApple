CREATE TABLE IF NOT EXISTS AreaUsers (
	AreaID BIGINT NOT NULL,
	UserID BIGINT NOT NULL,
	AccessLevel INT NOT NULL,
	PRIMARY KEY (AreaId, UserId),
	INDEX ind_areausers_areaid (AreaID ASC),
	INDEX ind_areausers_userid (UserID ASC),
	CONSTRAINT fk_areausers_areaid
		FOREIGN KEY (AreaID)
		REFERENCES Areas(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_areausers_userid
		FOREIGN KEY (UserID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB