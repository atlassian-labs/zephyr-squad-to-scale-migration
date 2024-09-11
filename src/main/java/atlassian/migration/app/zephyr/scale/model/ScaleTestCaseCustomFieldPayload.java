package atlassian.migration.app.zephyr.scale.model;

import java.util.List;

public record ScaleTestCaseCustomFieldPayload(
        Object components,
        Object squadStatus,
        Object squadPriority

) {
    public static final List<String> CUSTOM_FIELDS_NAMES = List.of("components", "squadStatus", "squadPriority");
    public static final String ENTITY_TYPE = "TEST_CASE";
}
