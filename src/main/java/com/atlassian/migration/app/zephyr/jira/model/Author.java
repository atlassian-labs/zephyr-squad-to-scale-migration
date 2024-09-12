package com.atlassian.migration.app.zephyr.jira.model;

public record Author(
        String self,
        String name,
        String key,
        String emailAddress,
        String displayName,
        boolean active

) {
}
