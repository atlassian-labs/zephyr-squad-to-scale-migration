package com.atlassian.migration.app.zephyr.jira.model;

public record JiraReporterResponse(
    int id,
    String key,
    String name
) { }