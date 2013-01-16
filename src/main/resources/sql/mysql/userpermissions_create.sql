CREATE TABLE IF NOT EXISTS UserPermissions (
	UserID BIGINT NOT NULL,
	Permission VARCHAR(128) NOT NULL,
	INDEX ind_userpermissions_userid (UserID ASC),
	CONSTRAINT fk_userpermissions_userid
		FOREIGN KEY (UserID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB