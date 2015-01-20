CREATE TABLE IF NOT EXISTS GroupPermissions (
	GroupID BIGINT NOT NULL,
	Permission VARCHAR(128) NOT NULL,
	INDEX ind_grouppermissions_groupid (GroupID ASC),
	CONSTRAINT fk_grouppermissions_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB