package com.atlassian.migration.app.zephyr.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.atlassian.migration.app.zephyr.common.DatabaseType.*;

public record DataBaseConfigurationLoader(String url, String driverClassName, String schema, String userName,
                                          String password) {

    private static final Logger logger = LoggerFactory.getLogger(DataBaseConfigurationLoader.class);
    private static final String POSTGRESQL_DATASOURCE_PROPERTY = POSTGRESQL + ".datasource.";
    private static final String ORACLE_DATASOURCE_PROPERTY = ORACLE + ".datasource.";
    private static final String MSSQL_DATASOURCE_PROPERTY = MSSQL + ".datasource.";
    private static final String MYSQL_DATASOURCE_PROPERTY = MYSQL + ".datasource.";
    private static final String URL_PROPERTY = "url";
    private static final String DRIVER_CLASS_NAME_PROPERTY = "driver.class.name";
    private static final String SCHEMA_PROPERTY = "schema";
    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";

    public static DataBaseConfigurationLoader loadPostgresDbConfig() {
        return loadDbConfig(POSTGRESQL_DATASOURCE_PROPERTY);
    }

    public static DataBaseConfigurationLoader loadOracleDbConfig() {
        return loadDbConfig(ORACLE_DATASOURCE_PROPERTY);
    }

    public static DataBaseConfigurationLoader loadMssqlDbConfig() {
        return loadDbConfig(MSSQL_DATASOURCE_PROPERTY);
    }

    public static DataBaseConfigurationLoader loadMysqlDbConfig() {
        return loadDbConfig(MYSQL_DATASOURCE_PROPERTY);
    }

    private static DataBaseConfigurationLoader loadDbConfig(String prefixDbType) {

        Properties props = new Properties();

        try (FileInputStream input = new FileInputStream("database.properties")) {
            props.load(input);

            var dbUrl = props.getProperty(prefixDbType + URL_PROPERTY);
            var dbDriver = props.getProperty(prefixDbType + DRIVER_CLASS_NAME_PROPERTY);
            var schema = props.getProperty(prefixDbType + SCHEMA_PROPERTY);
            var dbUsername = props.getProperty(prefixDbType + USERNAME_PROPERTY);
            var dbPassword = props.getProperty(prefixDbType + PASSWORD_PROPERTY);

            return new DataBaseConfigurationLoader(dbUrl, dbDriver, schema, dbUsername, dbPassword);

        } catch (IOException e) {
            logger.error("Error while trying to load database configuration from database.properties: "
                    + e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

}
