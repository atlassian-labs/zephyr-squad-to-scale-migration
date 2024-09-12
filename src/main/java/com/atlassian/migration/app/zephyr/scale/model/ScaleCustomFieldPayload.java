package com.atlassian.migration.app.zephyr.scale.model;

public record ScaleCustomFieldPayload(
        String name,
        String category,
        String projectKey,
        String type
) {
    public static final String TYPE_SINGLE_LINE_TEXT = "SINGLE_LINE_TEXT";
}
