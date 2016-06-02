# Executive Summary
FlywayDB is an excellent mechanism to enable automated deployment of database changes and content.  It also comes 'out of the box' with mechanisms for an audit trail, checking past deployed versions against current version, etc.  

However, some organizations operationally are not structured to perform production deployments, aside from manually.  Thus, the impetus for this project.

The code in this repository allows for:

* Generation of a script that can be run manually by DBAs
    * Utilizes existing Flyway scripts to make the content of the generated script
    * Allows for selected portions of Flyway scripts to comprise the generated scripts - i.e. all the scripts needed since the last release
* Using the same Flyway audit table, migrations scripts generated with this program will also leave an audit trail of what has been run, and as to when it was last run

Missing from normal Flyway run operations will be:

* the audit field as to how long a script took to run
* audit trail for a script failing
    * this script will cease to run if errors are encountered


## Precursor Readings
This documents assumes you have read the following:

* flywaydb [http://www.flywaydb.org]


## Why is this needed?
* Many Intelliware development projects utilize FlywayBD as a mechanism for controlling + deploying database changes in an application, and tying said changes to the release of code
* Operations groups often times aren't comfortable with pointing a DB ugrade tool (i.e. Flyway, liquibase) against a deployed DB
    * Thus, this is intended to provide a mechanism to build upon the work done for Flyway, but not force Flyway usage in certain 'controlled' environments


## What does this do?

* Allows the user to specify a range of Flyway revisions
* It will generate a runnable script, to be passed onto DB operations teams
* Tracks what scripts have been applied against the DB
* Does this via a table called 'schema_versions' - the same table that Flyway utilizes
    * Current implementation supports Oracle and Postgres - specified at runtime 
    * Can be easily expanded to other DB implementations
* Should be compatible with Flyway's automated usage of the 'schema_version' table (i.e. automated can be used after manual deployment processs) - this should be verified

## What does this NOT do?

* Error handling is intended to be manual, and addressed on a case-by-case basis


## Usage

The following sections detail two mechanisms to generate the flyway script.  

### Parameters required for running

When you run the code without parameters, you will receive the following feedback:

```
Usage: FlywayScriptGeneratorRunner [revision location] [start version] [end version] [filename] [database_type]
Revision Location = filesystem:[relative or absolute path]
Start Version = starting revision number
End Version = ending revision number
File name = output file name
Database Type = 'PostGresSQL' or 'Oracle' - if omitted, default is PostGres
```

An example of valid parameters is as follows:

```
filesystem:src/test/resources/dbmigrations 1.000 1.001 migrationscript.sql POSTGRESQL
```

### Option 1 - creating within Eclipse
1. Open up the Eclipse launch file 'FlywayScriptGeneratorRunner.launch'.
2. Change the command line parameters passed to the launcher.
3. From Eclipse, run the file, and review the output file specified in your launch parameters.

### Option 2 - command line via running in jar
1. Run the shell script 'sample-buildAndRunScriptGenerator.sh'.  This script both builds the jar, and then executes it.  Runtime parameters are contained in said shell script, and use the same values as illustrated above.
This shell script should be modified for your own inputs / purposes / etc.


#### Example Expected Output

Find below an example of the expected generated output - this is for a PostgreSQL DB.

```

-- Create the 'schema_version' table if it doesn't exist
-- Exists to track what changes have been previously applied
CREATE TABLE IF NOT EXISTS "schema_version" 
  (version_rank int NOT NULL,
   installed_rank int NOT NULL,
   version varchar(50) PRIMARY KEY NOT NULL,
   description varchar(200) NOT NULL,
   type varchar(20) NOT NULL,
   script varchar(1000) NOT NULL,
   checksum int,
   installed_by varchar(100) NOT NULL,
   installed_on timestamp DEFAULT now() NOT NULL,
   execution_time int NOT NULL,
   success bool NOT NULL);

-- --------------------------------------------------------------------
-- REVISION: 1.000
-- --------------------------------------------------------------------
--
-- Copyright 2010-2014 Axel Fontaine
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
CREATE TABLE test_user (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

INSERT INTO schema_version 
(version_rank, installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) 
 VALUES
((select coalesce(max(version_rank),0) from schema_version)+1,(select coalesce(max(installed_rank),0) from schema_version)+1,'1.000','InitialScript','SQL','V1_000__InitialScript.sql',49631887,'manual',current_timestamp,-1,TRUE);
-- --------------------------------------------------------------------
-- REVISION: 1.001
-- --------------------------------------------------------------------
--
-- Copyright 2010-2014 Axel Fontaine
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
INSERT INTO test_user (name) VALUES ('Mr. T');

INSERT INTO schema_version 
(version_rank, installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) 
 VALUES
((select coalesce(max(version_rank),0) from schema_version)+1,(select coalesce(max(installed_rank),0) from schema_version)+1,'1.001','MoreTables','SQL','V1_001__MoreTables.sql',-1511390255,'manual',current_timestamp,-1,TRUE);
commit;
----------------------------------------------------------------------

```

## Potential Enhancements

* Upgrade to be compatible with the latest version of Flyway
    * At time of writing, Flyway was on version 4.1
    * This program was designed for Flyway 3.1
    * 4.x features changes to the 'schema_version' table, that stores audit / history of DB changes
* Augment code to be able to accommodate schema prefixes
* (Detailed code:) build in the way Flyway creates the 'schema_version' table into this mechanism
