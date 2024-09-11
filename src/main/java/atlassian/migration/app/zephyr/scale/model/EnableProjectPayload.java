package atlassian.migration.app.zephyr.scale.model;

public record EnableProjectPayload(
        String projectKey,
        Boolean enabled) { }

