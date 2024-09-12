package com.atlassian.migration.app.zephyr.jira.model;

public record JiraIssuesResponse(
    String id,
    String key,
    JiraIssueFieldResponse fields
) { }