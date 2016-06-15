package com.intelliware.flywayscriptgenerator.service;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.db2zos.DB2zosDbSupport;
import org.flywaydb.core.internal.dbsupport.h2.H2DbSupport;
import org.flywaydb.core.internal.dbsupport.hsql.HsqlDbSupport;
import org.flywaydb.core.internal.dbsupport.mysql.MySQLDbSupport;
import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;
import org.flywaydb.core.internal.dbsupport.redshift.RedshiftDbSupport;
import org.flywaydb.core.internal.dbsupport.sqlite.SQLiteDbSupport;
import org.flywaydb.core.internal.dbsupport.sqlserver.SQLServerDbSupport;
import org.flywaydb.core.internal.dbsupport.vertica.VerticaDbSupport;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
public enum SupportedDBTypes {

	ORACLE(new OracleDbSupport(null)), 
	POSTGRESQL(new PostgreSQLDbSupport(null)),
	// DB2(new DB2DbSupport(null)),
	DB2ZOS(new DB2zosDbSupport(null)), 
	H2(new H2DbSupport(null)), 
	HSQL(new HsqlDbSupport(null)), 
	MYSQL(new MySQLDbSupport(null)), 
	REDSHIFT(new RedshiftDbSupport(null)), 
	SQLITE(new SQLiteDbSupport(null)), 
	SQLSERVER(new SQLServerDbSupport(null)), 
	VERTICA(new VerticaDbSupport(null));

	private DbSupport dbSupport;

	private SupportedDBTypes(DbSupport dbSupport) {
		this.dbSupport = dbSupport;
	}

	public DbSupport getDbSupport() {
		return this.dbSupport;
	}

}
