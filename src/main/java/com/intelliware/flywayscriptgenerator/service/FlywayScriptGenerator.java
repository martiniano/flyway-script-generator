package com.intelliware.flywayscriptgenerator.service;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.Location;
import org.slf4j.LoggerFactory;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * A class to generate data scripts to be run by a DBA operations group. The
 * assumption being DBA Operations group will be much more comfortable with
 * running scripts, as opposed to running Flyway.
 */
public class FlywayScriptGenerator {
	// environment specific constants
	public static final int BUFFER_SIZE = 2048;
	public static final String SYSTEM_FILE_ENCODING = "UTF-8";
	public static final int DEFAULT_MAX_COMMAND_LINE_LENGTH = 2500; // max
																	// length a
																	// single
																	// sql
																	// command
																	// can be

	public static final String INSERT_INTO_SCHEMA_VERSION = "INSERT INTO \"schema_version\"";
	public static final String COLUMN_NAMES = "(\"version_rank\", \"installed_rank\", \"version\", \"description\", \"type\", \"script\", \"checksum\", \"installed_by\", \"installed_on\", \"execution_time\", \"success\") ";
	public static final String VALUES = " VALUES";
	public static final String TYPE = "SQL"; // the value
	public static final String USER = "manual"; // the value put into
												// 'installed_by' column in
												// 'schema_version' table

	// local variables
	private String[] locations;
	private String prefix;

	org.slf4j.Logger logger = LoggerFactory.getLogger(FlywayScriptGenerator.class);

	public FlywayScriptGenerator(String prefix, String... locations) {
		this.prefix = prefix;
		this.locations = locations;
	}

	public void writeDDLToRevision(String startingRevision, String endingRevision, String fileName, DbSupport dbSupport)
			throws IOException {

		PrintStream out;

		logger.info("Starting creating DB Script with starting revision: {}; ending revision: {}", startingRevision,
				endingRevision);
		logger.info("Using as base path to parse DB files: {}", locations);
		logger.info("Output to filename: {}", fileName);

		File outputFile = new File(fileName);

		out = new PrintStream(outputFile);

		MigrationVersion from = MigrationVersion.fromVersion(startingRevision);
		MigrationVersion to = MigrationVersion.fromVersion(endingRevision);

		Flyway flyway = createFlyway(null, locations, prefix);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		SqlMigrationResolver resolver = new SqlMigrationResolver(null, classLoader,
				new Location(flyway.getLocations()[0]), null, null, flyway.getSqlMigrationPrefix(),
				flyway.getSqlMigrationSeparator(), flyway.getSqlMigrationSuffix());
		List<ResolvedMigration> revisions = resolver.resolveMigrations();

		String sql;
		PrintWriter pw = new PrintWriter(FlywayScriptGenerator.getEncodedOutputStreamWriter(out));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		if (dbSupport instanceof PostgreSQLDbSupport) {
			preRevisionWritePostgresSQL(pw);
		} else if (dbSupport instanceof OracleDbSupport) {
			preRevisionWriteOracle(pw);
		}
		for (ResolvedMigration revision : revisions) {
			if (revision.getVersion().compareTo(from) >= 0 && revision.getVersion().compareTo(to) <= 0) {
				bytes.reset();
				pw.println("-- --------------------------------------------------------------------");
				pw.println("-- REVISION: " + revision.getVersion().getVersion());
				pw.println("-- --------------------------------------------------------------------");
				FlywayScriptGenerator.readStream(new FileInputStream(new File(revision.getPhysicalLocation())), bytes);

				sql = handleRevisionTokens(FlywayScriptGenerator.getEncodedString(bytes.toByteArray()));
				sql = adjustRevisionSQLForWrite(sql);
				pw.print(sql);
				pw.println();

				// do the insert for tracking what has been committed
				pw.println(INSERT_INTO_SCHEMA_VERSION);
				pw.println(COLUMN_NAMES);
				pw.println(VALUES);
				pw.println(generateInsertValuesForSchemaVersionTable(revision.getVersion().getVersion(),
						revision.getDescription(), revision.getScript(), revision.getChecksum(), dbSupport));
			}
		}
		postRevisionWrite(pw);
		pw.println("----------------------------------------------------------------------");
		pw.println();
		pw.flush();
		pw.close();

		outputFile.setWritable(false);

		logger.info("Have successfully written script to file.");
	}

	/**
	 * Any text that has to be added to the Beginning of the generated script
	 * For Postgres implementation
	 * 
	 * @param pw
	 */
	protected void preRevisionWritePostgresSQL(PrintWriter pw) {
		StringBuffer sb = new StringBuffer();
		sb.append("-- Create the 'schema_version' table if it doesn't exist\n");
		sb.append("-- Exists to track what changes have been previously applied\n");
		sb.append("CREATE TABLE IF NOT EXISTS \"schema_version\" \n");
		sb.append("  (version_rank int NOT NULL,\n");
		sb.append("   installed_rank int NOT NULL,\n");
		sb.append("   version varchar(50) PRIMARY KEY NOT NULL,\n");
		sb.append("   description varchar(200) NOT NULL,\n");
		sb.append("   type varchar(20) NOT NULL,\n");
		sb.append("   script varchar(1000) NOT NULL,\n");
		sb.append("   checksum int,\n");
		sb.append("   installed_by varchar(100) NOT NULL,\n");
		sb.append("   installed_on timestamp DEFAULT now() NOT NULL,\n");
		sb.append("   execution_time int NOT NULL,\n");
		sb.append("   success bool NOT NULL);\n");

		pw.println(sb.toString());
	}

	/**
	 * Any text that has to be added to the Beginning of the generated script
	 * For Oracle implementation
	 * 
	 * @param pw
	 */
	protected void preRevisionWriteOracle(PrintWriter pw) {
		StringBuffer sb = new StringBuffer();

		sb.append("-- Create the 'schema_version' table if it doesn't exist\n");
		sb.append("-- Exists to track what changes have been previously applied\n");
		sb.append("declare\n");
		sb.append("   nCount NUMBER;\n");
		sb.append("   v_sql LONG;\n");
		sb.append("begin\n");
		sb.append("select count(*) into nCount FROM dba_tables where table_name = 'schema_version';\n");
		sb.append("IF(nCount <= 0) THEN\n");
		sb.append("v_sql:='\n");
		sb.append("CREATE TABLE \"schema_version\" (\n");
		sb.append("    \"version_rank\" INT NOT NULL, \n");
		sb.append("    \"installed_rank\" INT NOT NULL, \n");
		sb.append("    \"version\" VARCHAR2(50) NOT NULL, \n");
		sb.append("    \"description\" VARCHAR2(200) NOT NULL, \n");
		sb.append("    \"type\" VARCHAR2(20) NOT NULL, \n");
		sb.append("    \"script\" VARCHAR2(1000) NOT NULL, \n");
		sb.append("    \"checksum\" INT, \n");
		sb.append("    \"installed_by\" VARCHAR2(100) NOT NULL, \n");
		sb.append("    \"installed_on\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n");
		sb.append("    \"execution_time\" INT NOT NULL,\n");
		sb.append("    \"success\" NUMBER(1) NOT NULL)");
		sb.append("';\n");
		sb.append("execute immediate v_sql;\n");
		sb.append("END IF;");
		sb.append("end;");

		pw.println(sb.toString());
	}

	/**
	 * Any text that has to be added to the End of the generated script
	 * 
	 * @param pw
	 */
	protected void postRevisionWrite(PrintWriter pw) {
		pw.println("commit;");
	}

	protected int getMaxCommandLineLength() {
		return DEFAULT_MAX_COMMAND_LINE_LENGTH;
	}

	/**
	 * Formatting
	 * 
	 * @throws IOException
	 */
	protected String adjustRevisionSQLForWrite(String revision) throws IOException {
		String sql = revision;

		StringBuilder buff = new StringBuilder();
		LineNumberReader reader = new LineNumberReader(new StringReader(sql));
		try {
			String line = reader.readLine();
			while (line != null) {
				if (line.length() > getMaxCommandLineLength()) {
					throw new IllegalStateException("Line exceeds limit of " + getMaxCommandLineLength() + ": " + line);
				}
				if (line.trim().length() > 0) {
					// remove double semicolons
					line = repairDoubleSemicolons(line);
					buff.append(line);
					buff.append("\n");
				}
				line = reader.readLine();
			}
		} finally {
			reader.close();
		}

		return buff.toString();
	}

	protected String repairDoubleSemicolons(String s) {
		String line = s;
		if (line.trim().endsWith(";;")) {
			line = line.substring(0, line.lastIndexOf(';')); // Remove the
																// second
																// semicolon.
			line += "\n/\nshow error";
		}
		return line;
	}

	protected String handleRevisionTokens(String sql) {
		return sql.replaceAll("\\$\\{semicolon\\}", ";");
	}

	protected Flyway createFlyway(Connection conn, String[] locations, String prefix) {
		Flyway flyway = new Flyway();
		if (conn != null) {
			flyway.setDataSource(wrap(conn));
		}

		if (locations != null) {
			flyway.setLocations(locations);
		}
		if (prefix != null) {
			flyway.setSqlMigrationPrefix(prefix);
		}

		Map<String, String> placeholders = new HashMap<String, String>();
		placeholders.put("semicolon", ";");
		flyway.setPlaceholders(placeholders);
		return flyway;
	}

	public static void readStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];

		while (true) {
			int length = in.read(buffer);
			if (length < 0) {
				break;
			}
			out.write(buffer, 0, length);
		}
	}

	public static Writer getEncodedOutputStreamWriter(File outputFile)
			throws UnsupportedEncodingException, FileNotFoundException {
		return getEncodedOutputStreamWriter(new FileOutputStream(outputFile));
	}

	public static Writer getEncodedOutputStreamWriter(OutputStream out) throws UnsupportedEncodingException {
		return new BufferedWriter(new OutputStreamWriter(out, SYSTEM_FILE_ENCODING));
	}

	public static String getEncodedString(ByteArrayOutputStream out) throws UnsupportedEncodingException {
		return getEncodedString(out.toByteArray());
	}

	public static byte[] getEncodedBytes(String s) throws UnsupportedEncodingException {
		return s.getBytes(FlywayScriptGenerator.SYSTEM_FILE_ENCODING);
	}

	public static String getEncodedString(byte[] b) throws UnsupportedEncodingException {
		return new String(b, FlywayScriptGenerator.SYSTEM_FILE_ENCODING);
	}

	protected DataSource wrap(final Connection conn) {
		return new DataSource() {
			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return null;
			}

			@Override
			public void setLogWriter(PrintWriter out) throws SQLException {
			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException {
			}

			@Override
			public int getLoginTimeout() throws SQLException {
				return 0;
			}

			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				return null;
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}

			@Override
			public Connection getConnection() throws SQLException {
				return conn;
			}

			@Override
			public Connection getConnection(String username, String password) throws SQLException {
				return conn;
			}
		};
	}

	public String generateInsertValuesForSchemaVersionTable(String revisionNumber, String description, String filename,
			Integer checksum, DbSupport dbSupport) {
		StringBuilder sb = new StringBuilder();

		sb.append("(");
		sb.append("(select coalesce(max(\"version_rank\"),0) from \"schema_version\")+1"); // version
																							// rank
		sb.append(",");
		sb.append("(select coalesce(max(\"installed_rank\"),0) from \"schema_version\")+1"); // installed
																								// rank
		sb.append(",");
		sb.append(addSingleQuotes(revisionNumber)); // revision number
		sb.append(",");
		sb.append(addSingleQuotes(description)); // description
		sb.append(",");
		sb.append(addSingleQuotes(TYPE)); // type
		sb.append(",");
		sb.append(addSingleQuotes(filename)); // script
		sb.append(",");
		sb.append(checksum); // checksum
		sb.append(",");
		sb.append(addSingleQuotes(USER)); // installed_by
		sb.append(",");
		sb.append("current_timestamp"); // installed_on
		sb.append(",");
		sb.append(-1); // execution_time
		sb.append(",");
		sb.append(dbSupport.getBooleanTrue()); // success
		sb.append(");");
		return sb.toString();
	}

	private static String addSingleQuotes(String inputString) {
		return "'" + inputString + "'";
	}

}
