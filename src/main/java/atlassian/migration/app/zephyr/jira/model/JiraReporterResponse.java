package atlassian.migration.app.zephyr.jira.model;

public record JiraReporterResponse(
    int id,
    String key,
    String name
) { }