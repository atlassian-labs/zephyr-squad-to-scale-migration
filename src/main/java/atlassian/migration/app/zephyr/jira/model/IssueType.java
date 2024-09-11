package atlassian.migration.app.zephyr.jira.model;

public record IssueType(
    String self,
    String id,
    String description,
    String iconUrl,
    String name,
    boolean subtask,
    int avatarId
) {}