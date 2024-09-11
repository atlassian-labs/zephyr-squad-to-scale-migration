package atlassian.migration.app.zephyr.scale.model;

import java.util.List;

public record ScaleTestCaseCreationPayload(
        String projectKey,
        String name,
        String objective,
        List<String> labels,
        String owner,
        List<String> issueLinks,
        ScaleTestCaseCustomFieldPayload customFields
) {
}