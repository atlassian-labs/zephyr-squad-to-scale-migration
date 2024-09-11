package atlassian.migration.app.zephyr.scale.model;

public record ScaleExecutionCreationPayload(
        String status,
        String testCaseKey,
        Object executedBy,
        Object comment,
        String version,
        ScaleExecutionCustomFieldPayload customFields) {
}