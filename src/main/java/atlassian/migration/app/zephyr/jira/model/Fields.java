package atlassian.migration.app.zephyr.jira.model;

public record Fields(
    String summary,
    Status status,
    Priority priority,
    IssueType issuetype
) {}