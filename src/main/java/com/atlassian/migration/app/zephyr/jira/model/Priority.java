package com.atlassian.migration.app.zephyr.jira.model;

public record Priority(
    String self,
    String iconUrl,
    String name,
    String id
) {}