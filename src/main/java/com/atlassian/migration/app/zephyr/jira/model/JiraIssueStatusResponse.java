package com.atlassian.migration.app.zephyr.jira.model;

public record JiraIssueStatusResponse(
    String id,
    String name
) { }