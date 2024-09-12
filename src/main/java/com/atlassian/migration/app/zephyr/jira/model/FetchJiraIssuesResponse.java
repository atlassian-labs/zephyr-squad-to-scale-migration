package com.atlassian.migration.app.zephyr.jira.model;

import java.util.List;

public record FetchJiraIssuesResponse(
        int startAt,
        int total,
        List<JiraIssuesResponse> issues
) { }