CREATE TABLE IF NOT EXISTS Groups (
	ID BIGINT PRIMARY KEY,
	Name VARCHAR(128) NOT NULL UNIQUE
) ENGINE=InnoDB