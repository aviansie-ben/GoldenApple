CREATE TABLE IF NOT EXISTS PlayerGroups (
	ID INTEGER PRIMARY KEY AUTOINCREMENT,
	Name VARCHAR(128) NOT NULL,
	Creator BIGINT NOT NULL,
	CONSTRAINT fk_playergroups_creator
       FOREIGN KEY (Creator)
       REFERENCES User(ID)
       ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX ind_playergroups_name ON PlayerGroups (Creator ASC, Name ASC);