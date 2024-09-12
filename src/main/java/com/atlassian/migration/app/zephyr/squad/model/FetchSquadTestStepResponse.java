package com.atlassian.migration.app.zephyr.squad.model;

import java.util.List;

public record FetchSquadTestStepResponse(
        List<SquadTestStepResponse> stepBeanCollection
) { }