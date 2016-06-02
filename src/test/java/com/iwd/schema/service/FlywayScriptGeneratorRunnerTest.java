package com.iwd.schema.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class FlywayScriptGeneratorRunnerTest {
	
	@Test
	public void testValidation_Valid_PostgreSQL() {
		// 1. generate valid input
		String[] inputArgs = {"../src/test/resources/dbMigration", "1", "1.001", "outputFile.txt", "POSTGRESQL"};
		
		assertTrue(FlywayScriptGeneratorRunner.validate(inputArgs));
	}

	
	@Test
	public void testValidation_Valid_Oracle() {
		// 1. generate valid input
		String[] inputArgs = {"../src/test/resources/dbMigration", "1", "1.001", "outputFile.txt", "ORACLE"};
		
		assertTrue(FlywayScriptGeneratorRunner.validate(inputArgs));
	}	
	
	@Test
	public void testValidation_Valid_NoDBSpecified() {
		// 1. generate valid input
		String[] inputArgs = {"../src/test/resources/dbMigration", "1", "1.001", "outputFile.txt"};
		
		assertTrue(FlywayScriptGeneratorRunner.validate(inputArgs));
	}		
	
	@Test
	public void testValidation_InvalidInputs() {
		// 1. generate valid input
		String[] invalidInputArgs = {"../src/test/resources/dbMigration", "1", "1.001"};
		
		assertFalse(FlywayScriptGeneratorRunner.validate(invalidInputArgs));
	}	
	
	
}
