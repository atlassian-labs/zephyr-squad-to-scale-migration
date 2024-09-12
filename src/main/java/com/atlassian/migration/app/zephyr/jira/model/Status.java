package com.atlassian.migration.app.zephyr.jira.model;

public record Status(
    String self,
    String description,
    String iconUrl,
    String name,
    String id,
    StatusCategory statusCategory
) {}