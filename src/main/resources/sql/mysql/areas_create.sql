CREATE TABLE IF NOT EXISTS Areas (
	ID BIGINT PRIMARY KEY AUTO_INCREMENT,
	Label VARCHAR(128),
	Priority INT NOT NULL
) ENGINE=InnoDB