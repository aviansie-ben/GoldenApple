CREATE TABLE IF NOT EXISTS ChannelGroups (
	Channel CHAR(32) NOT NULL,
	GroupID BIGINT NOT NULL,
	AccessLevel INT NOT NULL,
	CONSTRAINT pk_channelgroups PRIMARY KEY (Channel ASC, GroupID ASC),
	CONSTRAINT fk_channelgroups_channel
		FOREIGN KEY (Channel)
		REFERENCES Channels(Identifier)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_channelgroups_userid
		FOREIGN KEY (GroupID)
		REFERENCES Groups(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB