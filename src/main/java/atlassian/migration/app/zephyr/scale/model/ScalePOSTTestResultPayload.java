package atlassian.migration.app.zephyr.scale.model;

import java.util.List;

public record ScalePOSTTestResultPayload(
        List<ScaleTestResultCreatedPayload> testResultsCreated
) {}