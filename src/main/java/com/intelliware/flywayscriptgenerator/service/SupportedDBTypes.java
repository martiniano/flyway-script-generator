package com.intelliware.flywayscriptgenerator.service;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
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
