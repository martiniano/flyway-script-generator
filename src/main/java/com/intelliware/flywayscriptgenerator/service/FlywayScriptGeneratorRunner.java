package com.intelliware.flywayscriptgenerator.service;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
public class FlywayScriptGeneratorRunner {

	private static FlywayScriptGenerator dataMigrator;

	// private static final Map<String, DbSupport> supportedDbs = new
	// HashMap<String, DbSupport>();
	//
	// static {
	// supportedDbs.put("POSTGRESQL", new PostgreSQLDbSupport(null));
	// supportedDbs.put("DB2", new DB2DbSupport(null));
	// supportedDbs.put("DB2ZOS", new DB2zosDbSupport(null));
	// supportedDbs.put("DERBY", new DerbyDbSupport(null));
	// supportedDbs.put("H2", new H2DbSupport(null));
	// supportedDbs.put("HSQL", new HsqlDbSupport(null));
	// supportedDbs.put("MYSQL", new MySQLDbSupport(null));
	// supportedDbs.put("ORACLE", new OracleDbSupport(null));
	// supportedDbs.put("REDSHIFT", new RedshiftDbSupport(null));
	// supportedDbs.put("SQLITE", new SQLiteDbSupport(null));
	// supportedDbs.put("SQLSERVER", new SQLServerDbSupport(null));
	// supportedDbs.put("VERTICA", new VerticaDbSupport(null));
	// }

	public static void main(String[] args) {

		// check usage - validation
		if (!validate(args)) {
			System.err.println("Due to errors, stopping execution");
		} else {
			DbSupport dbSupport = null;
			// instantiate the DB
			if (args.length == 4) {
				System.out.println("Defaulting usage to Postgres!");
				dbSupport = new PostgreSQLDbSupport(null);
			} else if (args.length == 5) {
				dbSupport = SupportedDBTypes.valueOf(args[4].toUpperCase()).getDbSupport();
			}

			try {
				dataMigrator = new FlywayScriptGenerator("V", args[0]);
				dataMigrator.writeDDLToRevision(args[1], args[2], args[3], dbSupport);
			} catch (FileNotFoundException e) {
				System.err.println("Error - cannot create new file!");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error - IO exception on writing file!");
				e.printStackTrace();
			}
		}
	}

	public static boolean validate(String[] args) {
		if (args.length < 4 || args.length > 5) {
			System.err.println(
					"Usage: FlywayScriptGeneratorRunner [revision location] [start version] [end version] [filename] [database_type]");
			System.err.println("Revision Location = filesystem:[relative or absolute path]");
			System.err.println("Start Version = starting revision number");
			System.err.println("End Version = ending revision number");
			System.err.println("File name = output file name");
			System.err.println("Database Type = Database type this script is targetted for");
			System.err.println("Database Types Supported as follows:");
			for (SupportedDBTypes supportedDbType : SupportedDBTypes.values()) {
				System.err.println("   " + supportedDbType.toString());
			}
			return false;
		} else if (args.length == 5) {
			// validate if the database type is appropriate
			if (args[4] != null) {
				// see if this is a supported type
				try {
					SupportedDBTypes.valueOf(args[4].toUpperCase());
				} catch (IllegalArgumentException iae) {
					System.err.println("DB Entry type of " + args[4] + " not supported!");
					return false;
				}
			}
			return true;
		}

		return true;
	}
}
