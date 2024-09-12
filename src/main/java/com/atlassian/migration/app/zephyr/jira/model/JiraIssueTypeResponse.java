package com.atlassian.migration.app.zephyr.jira.model;

public record JiraIssueTypeResponse(
        int id,
        String name
) { }