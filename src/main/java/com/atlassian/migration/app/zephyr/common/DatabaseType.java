package com.atlassian.migration.app.zephyr.common;

import java.util.Arrays;
import java.util.Optional;

public enum DatabaseType {
    POSTGRESQL("postgresql"),
    ORACLE("oracle"),
    MSSQL("mssql"),
    SQLSERVER("sqlserver"),
    MYSQL("mysql");

    private final String dbTypeName;

    DatabaseType(String value) {
        this.dbTypeName = value;
    }

    public static Optional<DatabaseType> getDatabaseTypeByName(String name) {
        return Arrays.stream(values()).filter(databaseType -> databaseType.dbTypeName.equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public String toString() {
        return dbTypeName;
    }

}
