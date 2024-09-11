package atlassian.migration.app.zephyr.jira.model;

public record IssueLink(
    String id,
    String self,
    LinkType type,
    RelatedIssue inwardIssue,
    RelatedIssue outwardIssue
) {}