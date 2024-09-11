package atlassian.migration.app.zephyr.jira.model;

public record LinkType(
    String id,
    String name,
    String inward,
    String outward,
    String self
) {}