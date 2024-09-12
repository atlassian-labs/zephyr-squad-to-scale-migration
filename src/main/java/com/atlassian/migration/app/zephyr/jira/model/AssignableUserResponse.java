package com.atlassian.migration.app.zephyr.jira.model;

public record AssignableUserResponse(
        String key,
        String name,
        String emailAddress,
        String displayName
) {
}
