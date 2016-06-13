package com.intelliware.flywayscriptgenerator.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.intelliware.flywayscriptgenerator.service.FlywayScriptGeneratorRunner;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
public class FlywayScriptGeneratorRunnerTest {

	@Test
	public void testValidation_Valid_PostgreSQL() {
		// 1. generate valid input
		String[] inputArgs = { "../src/test/resources/dbMigration", "1", "1.001", "outputFile.txt", "POSTGRESQL" };

		assertTrue(FlywayScriptGeneratorRunner.validate(inputArgs));
	}

	@Test
	public void testValidation_Valid_Oracle() {
		// 1. generate valid input
		String[] inputArgs = { "../src/test/resources/dbMigration", "1", "1.001", "outputFile.txt", "ORACLE" };

		assertTrue(FlywayScriptGeneratorRunner.validate(inputArgs));
	}

	@Test
	public void testValidation_Valid_NoDBSpecified() {
		// 1. generate valid input
		String[] inputArgs = { "../src/test/resources/dbMigration", "1", "1.001", "outputFile.txt" };

		assertTrue(FlywayScriptGeneratorRunner.validate(inputArgs));
	}

	@Test
	public void testValidation_InvalidInputs() {
		// 1. generate valid input
		String[] invalidInputArgs = { "../src/test/resources/dbMigration", "1", "1.001" };

		assertFalse(FlywayScriptGeneratorRunner.validate(invalidInputArgs));
	}

}
