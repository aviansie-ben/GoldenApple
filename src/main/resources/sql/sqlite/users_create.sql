CREATE TABLE IF NOT EXISTS Users (
	ID INTEGER PRIMARY KEY AUTOINCREMENT,
	Name VARCHAR(128) NOT NULL UNIQUE,
	UUID VARCHAR(36) NOT NULL UNIQUE
);
