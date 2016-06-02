package com.iwd.schema.service;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;

public enum SupportedDBTypes {

	ORACLE(new OracleDbSupport(null)), POSTGRESQL(new PostgreSQLDbSupport(null));
	private DbSupport dbSupport;
	
	private SupportedDBTypes(DbSupport dbSupport) {
		this.dbSupport = dbSupport;
	}
	
	public DbSupport getDbSupport() {
		return this.dbSupport;
	}
	
}
