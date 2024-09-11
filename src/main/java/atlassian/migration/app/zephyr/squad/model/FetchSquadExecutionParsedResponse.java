package atlassian.migration.app.zephyr.squad.model;


import java.util.List;
import java.util.Map;

public record FetchSquadExecutionParsedResponse(
        Map<String, SquadExecutionStatusResponse> status,
        String issueId,
        int recordsCount,
        int executionsToBeLogged,
        boolean isExecutionWorkflowEnabledForProject,
        boolean isTimeTrackingEnabled,
        List<SquadExecutionItemParsedResponse> executions) { }