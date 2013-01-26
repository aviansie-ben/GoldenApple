CREATE TABLE IF NOT EXISTS ChannelUsers (
	Channel CHAR(32) NOT NULL,
	UserID BIGINT NOT NULL,
	AccessLevel INT NOT NULL,
	CONSTRAINT pk_channelusers PRIMARY KEY (Channel ASC, UserID ASC),
	CONSTRAINT fk_channelusers_channel
		FOREIGN KEY (Channel)
		REFERENCES Channels(Identifier)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_channelusers_userid
		FOREIGN KEY (UserID)
		REFERENCES Users(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB