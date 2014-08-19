CREATE TABLE IF NOT EXISTS AreaUsers (
	AreaID INTEGER NOT NULL,
	UserID INTEGER NOT NULL,
	AccessLevel INTEGER NOT NULL,
	PRIMARY KEY (AreaID, UserID),
	CONSTRAINT fk_areausers_areaid
		FOREIGN KEY (AreaID)
		REFERENCES Areas(ID)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_areausers_userid
		FOREIGN KEY (UserID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_areausers_areaid ON AreaUsers (AreaID ASC);
CREATE INDEX ind_areausers_userid ON AreaUsers (UserID ASC);
