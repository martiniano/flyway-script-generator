package com.intelliware.flywayscriptgenerator.service;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class NuclearisFlywayMigrationsMerge {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}

				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Migrations Folder");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				String location = null;
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					location = chooser.getSelectedFile().toString();
					//System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
					//System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
				} else {
					System.out.println("No Selection ");
					return;
				}

				if (location == null || location.isEmpty()) {
					return;
				}

				String initialMigration = null;
				while (initialMigration == null || initialMigration.equals("")) {
					initialMigration = JOptionPane.showInputDialog("Initial Migration (Pattern: 0.0.0.0_0): ", "1.0.0.3_10");
					if (initialMigration == null || initialMigration.equals("")) {
						JOptionPane.showMessageDialog(null, "Entry with Initial Migration");
					}
				}

				String finalMigration = null;
				while (finalMigration == null || finalMigration.equals("")) {
					finalMigration = JOptionPane.showInputDialog("Final Migration (Pattern: 0.0.0.0_0): ", "1.0.0.4_10");
					if (finalMigration == null || finalMigration.equals("")) {
						JOptionPane.showMessageDialog(null, "Entry with Final Migration");
					}
				}

				String destination = null;
				while (destination == null || destination.equals("")) {
					destination = JOptionPane.showInputDialog("Migration destination file: ", "migrations/MIGRATION_FROM_" + initialMigration + "_TO_" + finalMigration + ".SQL");
					if (destination == null || destination.equals("")) {
						JOptionPane.showMessageDialog(null, "Entry with migration destination file");
					}
				}

				String prefix = null;
				while (prefix == null || prefix.equals("")) {
					prefix = JOptionPane.showInputDialog("Migrations prefix: ", "NUCLEARIS_");
					if (prefix == null || prefix.equals("")) {
						JOptionPane.showMessageDialog(null, "Entry with migrations prefix");
					}
				}

				String database = null;
				while (database == null || database.equals("")) {
					database = JOptionPane.showInputDialog("Database: ", "PostgreSQL");
					if (database == null || database.equals("")) {
						JOptionPane.showMessageDialog(null, "Entry with database");
					}
				}

				location = "filesystem:" + location + "/" + (database.toLowerCase().equals("postgresql") ? "postgres" : "oracle");

				String[] arguments = new String[] { location, initialMigration, finalMigration, destination, prefix, database };
				FlywayScriptGeneratorRunner.main(arguments);
			}
		});

	}

}
