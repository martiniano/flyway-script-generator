package com.iwd.schema.service;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.flywaydb.core.internal.dbsupport.DbSupport;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
public class FlywayScriptGeneratorRunner {

	private static FlywayScriptGenerator dataMigrator;

	public static void main(String[] args) {

		// check usage - validation
		if (!validate(args)) {
			System.err.println("Due to errors, stopping execution");
		} else {
			DbSupport dbSupport = null;
			// instantiate the DB
			if (args.length == 4) {
				System.out.println("Defaulting usage to Postgres!");
				dbSupport = SupportedDBTypes.POSTGRESQL.getDbSupport();
			} else if (args.length == 5) {
				if (args[4].equalsIgnoreCase(SupportedDBTypes.POSTGRESQL.toString())) {
					dbSupport = SupportedDBTypes.POSTGRESQL.getDbSupport();
				} else if (args[4].equalsIgnoreCase(SupportedDBTypes.ORACLE.toString())) {
					dbSupport = SupportedDBTypes.ORACLE.getDbSupport();
				}
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
			System.err.println("Database Type = 'PostGresSQL' or 'Oracle' - if omitted, default is PostGres");
			return false;
		} else if (args.length == 5) {
			// validate if the database type is appropriate
			if (!args[4].equalsIgnoreCase(SupportedDBTypes.ORACLE.toString())
					&& !args[4].equalsIgnoreCase(SupportedDBTypes.POSTGRESQL.toString())) {
				System.err.println("The only supported DB types are Oracle and PostgreSQL!");
				return false;
			}
		}

		return true;
	}
}
