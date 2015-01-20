CREATE TABLE IF NOT EXISTS GroupPermissions (
	GroupID INTEGER NOT NULL,
	Permission VARCHAR(128) NOT NULL,
	CONSTRAINT fk_grouppermissions_groupid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_grouppemrissions_groupid ON GroupPermissions (GroupID ASC);
