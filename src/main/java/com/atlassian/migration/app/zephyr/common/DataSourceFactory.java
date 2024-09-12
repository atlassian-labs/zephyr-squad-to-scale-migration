package com.atlassian.migration.app.zephyr.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.util.Optional;

public class DataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

    public DriverManagerDataSource createDataSourceFromDatabaseName(String dbName) throws IOException {

        Optional<DatabaseType> databaseTypeByName = DatabaseType.getDatabaseTypeByName(dbName.toLowerCase());

        if (databaseTypeByName.isPresent()) {

            DriverManagerDataSource datasource = new DriverManagerDataSource();

            var databaseConfiguration = switch (databaseTypeByName.get()) {
                case POSTGRESQL -> DataBaseConfigurationLoader.loadPostgresDbConfig();
                case ORACLE -> DataBaseConfigurationLoader.loadOracleDbConfig();
                case MSSQL, SQLSERVER -> DataBaseConfigurationLoader.loadMssqlDbConfig();
                case MYSQL -> DataBaseConfigurationLoader.loadMysqlDbConfig();
            };

            datasource.setUrl(databaseConfiguration.url());
            datasource.setDriverClassName(databaseConfiguration.driverClassName());

            if (databaseConfiguration.schema() != null
                    && !databaseConfiguration.schema().isBlank()
                    && !databaseConfiguration.schema().isEmpty()) {
                datasource.setSchema(databaseConfiguration.schema());
            }

            datasource.setUsername(databaseConfiguration.userName());
            datasource.setPassword(databaseConfiguration.password());

            return datasource;
        }

        logger.error("Trying to connect to unknown or unsupported database: \"" + dbName + "\"." +
                "\n Supported databases:" +
                "\n * postgresql" +
                "\n * oracle" +
                "\n * mssql" +
                "\n * mysql");

        throw new IOException();
    }


}
