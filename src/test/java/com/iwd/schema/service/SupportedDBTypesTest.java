package com.iwd.schema.service;

import static org.junit.Assert.*;

import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;
import org.junit.Test;

public class SupportedDBTypesTest {

	@Test
	public void testOracleDB() {
		assertNotNull(SupportedDBTypes.ORACLE.getDbSupport());
		assertEquals(OracleDbSupport.class, SupportedDBTypes.ORACLE.getDbSupport().getClass());
	}
	
	@Test
	public void testPostgreSQLDB() {
		assertNotNull(SupportedDBTypes.POSTGRESQL.getDbSupport());
		assertEquals(PostgreSQLDbSupport.class, SupportedDBTypes.POSTGRESQL.getDbSupport().getClass());
		
	}

}
