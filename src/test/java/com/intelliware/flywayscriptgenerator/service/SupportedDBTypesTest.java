package com.intelliware.flywayscriptgenerator.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;
import org.junit.Test;

import com.intelliware.flywayscriptgenerator.service.SupportedDBTypes;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
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
