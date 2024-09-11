package atlassian.migration.app.zephyr.migration.model;

public record SquadToScaleEntitiesMap (
        SquadToScaleTestCaseMap testCaseMap,

        SquadToScaleTestStepMap testStepMap,

        SquadToScaleTestExecutionMap testExecutionMap
){}
