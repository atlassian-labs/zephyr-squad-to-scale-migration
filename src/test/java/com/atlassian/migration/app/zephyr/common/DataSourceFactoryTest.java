package com.atlassian.migration.app.zephyr.common;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;


class DataSourceFactoryTest {

    @Mock
    DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Test
    void isDataSourceFromPostgresqlBeingCreatedCorrectly() {

        try (MockedStatic<DataBaseConfigurationLoader> mocked = mockStatic(DataBaseConfigurationLoader.class)) {
            mocked.when(DataBaseConfigurationLoader::loadPostgresDbConfig).thenReturn(new DataBaseConfigurationLoader(
                    "jdbc:postgresql://localhost:5432/jira", "org.postgresql.Driver", "public", "user",
                    "password"
            ));


            dataSourceFactory = new DataSourceFactory();


            DriverManagerDataSource expectedDataSource = new DriverManagerDataSource();
            expectedDataSource.setUrl("jdbc:postgresql://localhost:5432/jira");
            expectedDataSource.setSchema("public");
            expectedDataSource.setUsername("user");
            expectedDataSource.setPassword("password");

            var createdDatasource = dataSourceFactory.createDataSourceFromDatabaseName("postgresql");

            assertEquals(createdDatasource.getUrl(), expectedDataSource.getUrl());
            assertEquals(createdDatasource.getSchema(), expectedDataSource.getSchema());
            assertEquals(createdDatasource.getUsername(), expectedDataSource.getUsername());
            assertEquals(createdDatasource.getPassword(), expectedDataSource.getPassword());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void isDataSourceFromMysqlBeingCreatedCorrectly() {

        try (MockedStatic<DataBaseConfigurationLoader> mocked = mockStatic(DataBaseConfigurationLoader.class)) {
            mocked.when(DataBaseConfigurationLoader::loadMysqlDbConfig).thenReturn(new DataBaseConfigurationLoader(
                    "jdbc:mysql://localhost:3306/jira", "com.mysql.jdbc.Driver", "public", "user",
                    "password"
            ));

            DriverManagerDataSource expectedDataSource = new DriverManagerDataSource();
            expectedDataSource.setUrl("jdbc:mysql://localhost:3306/jira");
            expectedDataSource.setSchema("public");
            expectedDataSource.setUsername("user");
            expectedDataSource.setPassword("password");

            var createdDatasource = dataSourceFactory.createDataSourceFromDatabaseName("mysql");

            assertEquals(createdDatasource.getUrl(), expectedDataSource.getUrl());
            assertEquals(createdDatasource.getSchema(), expectedDataSource.getSchema());
            assertEquals(createdDatasource.getUsername(), expectedDataSource.getUsername());
            assertEquals(createdDatasource.getPassword(), expectedDataSource.getPassword());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void isDataSourceFromMssqlBeingCreatedCorrectly() {

        try (MockedStatic<DataBaseConfigurationLoader> mocked = mockStatic(DataBaseConfigurationLoader.class)) {
            mocked.when(DataBaseConfigurationLoader::loadMssqlDbConfig).thenReturn(new DataBaseConfigurationLoader(
                    "jdbc:sqlserver://localhost:1433;databaseName=jira", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "public", "user",
                    "password"
            ));


            DriverManagerDataSource expectedDataSource = new DriverManagerDataSource();
            expectedDataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=jira");
            expectedDataSource.setSchema("public");
            expectedDataSource.setUsername("user");
            expectedDataSource.setPassword("password");

            var createdDatasource = dataSourceFactory.createDataSourceFromDatabaseName("mssql");

            assertEquals(createdDatasource.getUrl(), expectedDataSource.getUrl());
            assertEquals(createdDatasource.getUsername(), expectedDataSource.getUsername());
            assertEquals(createdDatasource.getSchema(), expectedDataSource.getSchema());
            assertEquals(createdDatasource.getPassword(), expectedDataSource.getPassword());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void isDataSourceFromOracleBeingCreatedCorrectly() {

        try (MockedStatic<DataBaseConfigurationLoader> mocked = mockStatic(DataBaseConfigurationLoader.class)) {
            mocked.when(DataBaseConfigurationLoader::loadOracleDbConfig).thenReturn(new DataBaseConfigurationLoader(
                    "jdbc:oracle:thin:@localhost:1521:jira", "oracle.jdbc.driver.OracleDriver", "public", "user",
                    "password"
            ));

            DriverManagerDataSource expectedDataSource = new DriverManagerDataSource();
            expectedDataSource.setUrl("jdbc:oracle:thin:@localhost:1521:jira");
            expectedDataSource.setSchema("public");
            expectedDataSource.setUsername("user");
            expectedDataSource.setPassword("password");

            var createdDatasource = dataSourceFactory.createDataSourceFromDatabaseName("oracle");

            assertEquals(createdDatasource.getUrl(), expectedDataSource.getUrl());
            assertEquals(createdDatasource.getSchema(), expectedDataSource.getSchema());
            assertEquals(createdDatasource.getUsername(), expectedDataSource.getUsername());
            assertEquals(createdDatasource.getPassword(), expectedDataSource.getPassword());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void isDataSourceFromUnknownDataBaseThrowingError() {

        assertThrows(IOException.class, () -> dataSourceFactory.createDataSourceFromDatabaseName("unknown"));
    }


}