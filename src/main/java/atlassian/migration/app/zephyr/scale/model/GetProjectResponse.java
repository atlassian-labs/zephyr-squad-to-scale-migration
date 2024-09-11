package atlassian.migration.app.zephyr.scale.model;

import java.util.List;

public record GetProjectResponse(
        String key,
        String id,
        List<String> projectKeys
) {
}

