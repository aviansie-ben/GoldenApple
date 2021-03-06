CREATE TABLE IF NOT EXISTS AreaRegions (
	ID INTEGER PRIMARY KEY AUTOINCREMENT,
	AreaID INTEGER NOT NULL,
	World VARCHAR(128) NOT NULL,
	MinX REAL NOT NULL,
	MinY REAL NOT NULL,
	MinZ REAL NOT NULL,
	MaxX REAL NOT NULL,
	MaxY REAL NOT NULL,
	MaxZ REAL NOT NULL,
	IgnoreY REAL NOT NULL,
	Shape INTEGER NOT NULL,
	CONSTRAINT fk_arearegions_areaid
		FOREIGN KEY (AreaID)
		REFERENCES Areas(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_arearegions_loc ON AreaRegions (World ASC, MinX ASC, MinY ASC, MinZ ASC, MaxX ASC, MaxY ASC, MaxZ ASC);
