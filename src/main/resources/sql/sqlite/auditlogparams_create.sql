CREATE TABLE IF NOT EXISTS AuditLogParams (
	AuditID BIGINT NOT NULL,
	Param VARCHAR(32) NOT NULL,
	ValueInt BIGINT NULL,
	ValueString VARCHAR(128) NULL,
	CONSTRAINT fk_auditlogparams_auditid
		FOREIGN KEY (AuditID)
		REFERENCES AuditLog(ID)
		ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX ind_auditlogparams_auditid ON AuditLogParams (AuditID ASC);
