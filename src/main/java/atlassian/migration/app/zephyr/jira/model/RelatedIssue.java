package atlassian.migration.app.zephyr.jira.model;

public record RelatedIssue(
    String id,
    String key,
    String self,
    Fields fields
) {}