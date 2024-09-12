package com.atlassian.migration.app.zephyr.jira.model;

public record StatusCategory(
    String self,
    int id,
    String key,
    String colorName,
    String name
) {}
