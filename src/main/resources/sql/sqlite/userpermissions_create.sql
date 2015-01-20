CREATE TABLE IF NOT EXISTS UserPermissions (
	UserID INTEGER NOT NULL,
	Permission VARCHAR(128) NOT NULL,
	CONSTRAINT fk_userpermissions_userid
		FOREIGN KEY (UserID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_userpermissions_userid (UserID ASC);
