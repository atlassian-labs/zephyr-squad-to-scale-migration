package com.atlassian.migration.app.zephyr.scale.model;

public record ScaleGETStepsPayload(
        String key,
        String projectKey,

        SquadGETStepItemPayload testScript

) {
}
