package atlassian.migration.app.zephyr.common;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DatabaseUtils {

    public static DatabaseType defineDatabaseType(DriverManagerDataSource datasource) {
        var dbName = DatabaseType.getDatabaseTypeByName(datasource.getUrl().split(":")[1]);
        if (dbName.isPresent()) {
            return dbName.get();
        } else {
            throw new IllegalArgumentException("Database type not supported: " + datasource.getUrl());
        }
    }
}
