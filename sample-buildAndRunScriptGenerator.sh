mvn clean install
java -jar target/flywaydb-script-generator-0.9.1.jar filesystem:src/test/resources/dbmigrations 1 1.001 migrationscript.sql POSTGRESQL
