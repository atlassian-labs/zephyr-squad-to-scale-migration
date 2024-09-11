> Please remove this quote block and replace the others with meaningful content. If you're looking at this on Bitbucket,
> be assured that it looks great on GitHub.

# Zephyr Squad to Scale Migration script

[![Atlassian license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](CONTRIBUTING.md)

This script executes a Migration From Zephyr Squad to Zephyr Scale on Jira DC/Server, with both apps o the same
instance, aiming Customer that wishes to migrate from app to another before migrating to Jira to Cloud.
It uses Jira, Squad and Scale APIs to read and insert entities, executes some queries on Zephyr Scale Database
to fetch complementary data to help the migration and generates a CSV file with the mapping of the attachments to be
latter inserted

## Usage

This script can be run in two different modes:

1. **Single Project Mode**: where you define the project key and the script will migrate all entities from that project
    ```bash
    java -jar zephyr-squad-to-scale-migration.jar <username> <password> <projectKey>
    ```
2. **All projects Mode**: where the script will migrate all entities from all projects that hold Zephyr Squad data in
   the instance
    ```bash
    java -jar zephyr-squad-to-scale-migration.jar <username> <password>
    ``` 

When the script finishes running, it will have migrated Squad Entities to Scale, copied all Attachments from Zephyr
Squad Entities to Zephyr Scale and generated a CSV file with the
attachments mapping. This file must be imported in the Zephyr Scale table `AO_4D28DD_ATTACHMENT` and to do so you can
use a third party tool like [DBeaver](https://dbeaver.io/)
or [MySQL Workbench](https://dev.mysql.com/downloads/workbench/) or a command line, like so:

_Postgresql only_

```sql
psql
-U <username> -d <db_name> -c "\COPY \"AO_4D28DD_ATTACHMENT\" (\"FILE_NAME\",\"FILE_SIZE\",\"NAME\",\"PROJECT_ID\",\"USER_KEY\",\"TEMPORARY\",\"CREATED_ON\",\"MIME_TYPE\",\"TEST_CASE_ID\",\"STEP_ID\",\"TEST_RESULT_ID\") FROM /<CSV_FILE_NAME>.csv delimiter ',' CSV HEADER"
```

## Installation

### Prerequisites

**Java 17**

Ensure Java 17 is installed on your machine. Verify by running:

```bash
   java -version
```

**Java Installation**

<details>
    <summary>Linux</summary>

1. Update the package
   ```bash
   sudo apt-get update
   ```
2. Install Java 17
    ```bash
        sudo apt-get install openjdk-17-jdk
    ```
3. Check the installation
    ```bash
        java -version
    ```

</details>

<details>
    <summary>macOS</summary>

1. Install Homebrew
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
2. Install Java 17
    ```bash
        brew install openjdk@17
    ```

</details>

<details>
    <summary>Windows</summary>

1. Download the installer from the [Oracle](https://www.oracle.com/java/technologies/downloads/#java17) website.
2. Run the installer.
3. Set the `JAVA_HOME` environment variable:
    * Right-click on the `My Computer` icon on the desktop and select `Properties`.
    * Click on the `Advanced system settings` link.
    * Click on the `Environment Variables` button.
    * Under `System Variables`, click `New` and set the variable name as `JAVA_HOME` and the value as the path to the
      JDK installation directory.
    * Click `OK` to save.
4. Add the `JAVA_HOME\bin` directory to the `PATH` environment variable:
    * Find the `Path` variable in the `System Variables` section and click `Edit`.
    * Click `New` and add `%JAVA_HOME%\bin`.
    * Click `OK` to save.
5. Verify the installation:
    ```cmd
    java -version
    ```

</details>

### Script configuration

#### Properties setup

##### app.properties

| Fields                   | Used For                                                                                                    |
|--------------------------|-------------------------------------------------------------------------------------------------------------|
| host                     | Public address of Jira Instance                                                                             |
| batchSize                | How many Test Cases are processed per batch. Default is 100.                                                |
| attachmentsMappedCsvFile | The name of the resulting csv generated during the migration                                                |
| database                 | Name of the database used in the instance. Supported values are `postgresql`, `oracle`, `mssql` and `mysql` |
| attachmentsBaseFolder    | Where the attachments are located in the Instance Machine                                                   | 
| httpVersion              | Http version to be used in REST API Calls. Supported values `1.1`, `1`, `2`, `2.0`                          |

Example:

```
host=https://my-jira-instance-url.com
batchSize=100
attachmentsMappedCsvFile=AO_4D28DD_ATTACHMENT.csv
database=postgresql
attachmentsBaseFolder=/home/ubuntu/jira/data/attachments/
```

##### database.properties

| Fields                                       | Used for                                         |
|----------------------------------------------|--------------------------------------------------|
| <database type>.datasource.url               | Database url to access it                        |
| <database type>.datasource.driver.class.name | Database Driver. **You don't have to modify it** |
| <database type>.datasource.schema            | Schema holding Jira tables (Optional)            |
| <database type>.datasource.username          | database `username`                              |
| <database type>.datasource.password          | database `password`                              |

Example:

```
postgresql.datasource.url=jdbc:postgresql://localhost:5432/jira
postgresql.datasource.driver.class.name=org.postgresql.Driver
postgresql.datasource.username=some_username
postgresql.datasource.password=some_password
```

#### Running in the right place

The Script must be run inside an Instance/Node running Jira. That is the case because it directly copies Attachments
from one directory to another. To do, you must move the JAR file alongside both `app.properties`
and `database.properties` (already configured) to the Jira host.

## Documentation

### How it does it

The script uses Jira, Squad and Scale APIs to read and insert entities, executes some queries on Zephyr Scale Database.
APIs documentation:

- [Jira API](https://docs.atlassian.com/software/jira/docs/api/REST/9.11.0/)
- [Zephyr Squad API](https://zephyrsquadserver.docs.apiary.io/#reference)
- [Zephyr Scale API](https://support.smartbear.com/zephyr-scale-server/api-docs/v1/)

### What it does

This script is capable of migrating the Zephyr Squad entities, along with their attachments, to Zephyr Scale. The
following entities are supported:

- Test Cases and attachments
- Test Steps and attachments
- Cycles
- Test Executions and attachments

### What it doesn't do

- **Fetch data only through public APIs**: Some of the data needed during the migration is not accessible through the
  public APIs
  and must be fetched directly from the database
- **Run remotely**: The script directly copies attachments from Squad directories to Scale directory, so it need access
  to both.
- **Check if a project was already migrated**: The script does not check if a project was already migrated, so it may
  duplicate data if run multiple times on the same project
- **Clean Zephyr Scale data**: Currently, there is no easy way to clean Zephyr Scale after an unsuccessful/undesirable
  migration. It must be done manually through the UI or Database.
- **Automated attachments import**: The script generates a CSV file with the attachments mapping, but it does not import
  it
  automatically. It must be done manually through a third-party tool or command line.

## Contributions

Contributions to Zephyr Squad to Scale Migration script are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for
details.

## License

Copyright (c) 2024 Atlassian US., Inc.
Apache 2.0 licensed, see [LICENSE](LICENSE) file.

[![With â¤ï¸ from Atlassian](https://raw.githubusercontent.com/atlassian-internal/oss-assets/master/banner-with-thanks.png)](https://www.atlassian.com)



