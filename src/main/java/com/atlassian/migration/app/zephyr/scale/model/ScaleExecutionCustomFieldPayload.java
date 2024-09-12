package com.atlassian.migration.app.zephyr.scale.model;

import java.util.List;

public record ScaleExecutionCustomFieldPayload(
        Object executedOn,
        Object assignedTo,
        Object squadVersion,
        String squadCycleName,
        String folderName
) {
    public static final List<String> CUSTOM_FIELDS_NAMES = List.of("executedOn", "assignedTo", "squadVersion",
            "squadCycleName", "folderName");

    public static final String ENTITY_TYPE = "TEST_EXECUTION";
}