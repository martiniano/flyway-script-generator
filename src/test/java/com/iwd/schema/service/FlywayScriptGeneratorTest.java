package com.iwd.schema.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;

public class FlywayScriptGeneratorTest {

	public final String POSTGRES_OUTPUT_FILENAME = "testOutput_Postgres.sql";
	public final String ORACLE_OUTPUT_FILENAME = "testOutput_Oracle.sql";

	private FlywayScriptGenerator fixture = new FlywayScriptGenerator("V",
			"filesystem:src/test/resources/dbmigrations");

	@Before
	public void deleteFiles() {
		File fileToBeDeleted = new File(POSTGRES_OUTPUT_FILENAME);
		fileToBeDeleted.setWritable(true);
		fileToBeDeleted.delete();
		fileToBeDeleted = new File(ORACLE_OUTPUT_FILENAME);
		fileToBeDeleted.setWritable(true);
		fileToBeDeleted.delete();
	}

	@Test
	public void testGenerateFile_Postgres() throws IOException {
		fixture.writeDDLToRevision("1", "1.001", POSTGRES_OUTPUT_FILENAME, new PostgreSQLDbSupport(null));
		File testFile = new File(POSTGRES_OUTPUT_FILENAME);
		assertTrue("Expected file was not created!", testFile.exists());

		// assert that the correct versions are in the created file
		assertTrue(containsString(testFile, "REVISION: 1.000"));
		assertTrue(containsString(testFile, "REVISION: 1.001"));
		assertFalse("Expected file should be read only!", testFile.canWrite());
	}

	@Test
	public void testGenerateFile_Oracle() throws IOException {
		fixture.writeDDLToRevision("1", "1.001", ORACLE_OUTPUT_FILENAME, new OracleDbSupport(null));
		File testFile = new File(ORACLE_OUTPUT_FILENAME);
		assertTrue("Expected file was not created!", testFile.exists());
		assertFalse("Expected file should be read only!", testFile.canWrite());
	}

	@Test
	public void testGenerateFile_LimitedVersions_1() throws IOException {
		fixture.writeDDLToRevision("1.001", "1.001", POSTGRES_OUTPUT_FILENAME, new PostgreSQLDbSupport(null));

		File testFile = new File(POSTGRES_OUTPUT_FILENAME);
		assertTrue("Expected file was not created!", testFile.exists());

		// assert that the correct versions are in the created file
		assertFalse(containsString(testFile, "REVISION: 1.000"));
		assertTrue(containsString(testFile, "REVISION: 1.001"));
		assertFalse("Expected file should be read only!", testFile.canWrite());
	}

	@Test
	public void testGenerateFile_LimitedVersions_2() throws IOException {
		fixture.writeDDLToRevision("1.000", "1.000", POSTGRES_OUTPUT_FILENAME, new PostgreSQLDbSupport(null));

		File testFile = new File(POSTGRES_OUTPUT_FILENAME);

		testFile = new File(POSTGRES_OUTPUT_FILENAME);
		assertTrue("Expected file was not created!", testFile.exists());

		// assert that the correct versions are in the created file
		assertTrue(containsString(testFile, "REVISION: 1.000"));
		assertFalse(containsString(testFile, "REVISION: 1.001"));
		assertFalse("Expected file should be read only!", testFile.canWrite());
	}

	// ignoring this test, as on certain environments (cmd line outside of
	// eclipse), the
	// postgres DB fails to start ...
	@Test
	@Ignore
	public void testAgainstPostgresDB() throws IOException, SQLException {
		// 0. setup the postgres database
		EmbeddedPostgres pg = EmbeddedPostgres.start();

		Connection c = pg.getPostgresDatabase().getConnection();

		// 1. generate the postgres file
		fixture.writeDDLToRevision("1.000", "1.001", POSTGRES_OUTPUT_FILENAME, new PostgreSQLDbSupport(null));
		File testFile = new File(POSTGRES_OUTPUT_FILENAME);
		assertTrue("Expected file was not created!", testFile.exists());

		// assert that the correct versions are in the created file
		assertTrue(containsString(testFile, "REVISION: 1.000"));
		assertTrue(containsString(testFile, "REVISION: 1.001"));
		assertFalse("Expected file should be read only!", testFile.canWrite());

		// 2. apply the completed DB script against the Postgres DB
		Statement s = c.createStatement();
		try {
			s.execute(getEntireFile(testFile));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed in applying DB script to postgres DB");
		}
	}

	private boolean containsString(File fileToCheck, String stringToCheck) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(fileToCheck));) {
			String lineFromFile = null;
			while ((lineFromFile = reader.readLine()) != null) {
				if (lineFromFile.contains(stringToCheck)) {
					return true;
				}
			}
			return false;
		}
	}

	private String getEntireFile(File inputFile) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));) {
			String lineFromFile = null;
			while ((lineFromFile = reader.readLine()) != null) {
				sb.append(lineFromFile);
				sb.append("\n");
			}
			return sb.toString();
		}
	}
}
