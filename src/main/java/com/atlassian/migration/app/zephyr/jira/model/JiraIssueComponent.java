package com.atlassian.migration.app.zephyr.jira.model;


public record JiraIssueComponent(
    int id,
    String name,
    String self
) {}
