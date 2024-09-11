package atlassian.migration.app.zephyr.migration.service;

import atlassian.migration.app.zephyr.common.ApiException;
import atlassian.migration.app.zephyr.jira.api.JiraApi;
import atlassian.migration.app.zephyr.jira.model.AssignableUserResponse;
import atlassian.migration.app.zephyr.scale.model.ScaleExecutionCreationPayload;
import atlassian.migration.app.zephyr.scale.model.ScaleExecutionCustomFieldPayload;
import atlassian.migration.app.zephyr.squad.model.SquadExecutionItemParsedResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScaleTestExecutionPayloadFacade implements Resettable {

    private static final String DEFAULT_NONE_USER = "None";

    private static final String SQUAD_STATUS_WIP = "wip";
    private static final String SQUAD_STATUS_UNEXECUTED = "unexecuted";
    private static final String SQUAD_VERSION_UNSCHEDULED = "unscheduled";

    private static final String SCALE_EXEC_STATUS_IN_PROGRESS = "In Progress";
    private static final String SCALE_EXEC_STATUS_NOT_EXECUTED = "Not Executed";

    private final Map<String, String> statusTranslation = Map.of(
            SQUAD_STATUS_WIP, SCALE_EXEC_STATUS_IN_PROGRESS,
            SQUAD_STATUS_UNEXECUTED, SCALE_EXEC_STATUS_NOT_EXECUTED
    );

    private final Set<String> assignableUsers = new HashSet<>();
    private final Set<String> unassignableUsers = new HashSet<>(Set.of(DEFAULT_NONE_USER));
    private final JiraApi jiraApi;

    public ScaleTestExecutionPayloadFacade(JiraApi jiraApi) {
        this.jiraApi = jiraApi;
    }

    @Override
    public void reset() {
        assignableUsers.clear();

        unassignableUsers.clear();
        unassignableUsers.add(DEFAULT_NONE_USER);
    }

    public ScaleExecutionCreationPayload buildPayload(
            SquadExecutionItemParsedResponse executionData, String scaleTestCaseKey, String projectKey) throws IOException {

        var executedByValidation = validateAssignedUser(executionData.createdByUserName(), projectKey);
        var assignedToValidation = validateAssignedUser(executionData.assignedToOrStr().toString(), projectKey);

        return new ScaleExecutionCreationPayload(
                translateSquadToScaleExecStatus(executionData.status().name()),
                scaleTestCaseKey,
                executedByValidation ? executionData.createdBy() : DEFAULT_NONE_USER,
                executionData.htmlComment(),
                translateSquadToScaleVersion(executionData.versionName()),
                new ScaleExecutionCustomFieldPayload(
                        executionData.executedOnOrStr(),
                        assignedToValidation ? executionData.assignedTo() : DEFAULT_NONE_USER,
                        translateSquadToScaleVersion(executionData.versionName()),
                        executionData.cycleName(),
                        executionData.folderNameOrStr())
        );
    }

    private Boolean validateAssignedUser(String assignedUsername, String projectKey) throws IOException {
        if (assignedUsername == null
                || assignedUsername.isBlank()
                || unassignableUsers.contains(assignedUsername)) {
            return false;
        } else if (assignableUsers.contains(assignedUsername)) {
            return true;
        }

        var assignableUser = jiraApi.fetchAssignableUserByUsernameAndProject(assignedUsername,
                projectKey);

        if (assignableUser.isEmpty()) {
            unassignableUsers.add(assignedUsername);
            return false;
        }

        if (assignableUser.size() > 1) {
            throw new ApiException(-1, "Multiple users found for the same username: " + assignedUsername);
        }

        if (isSameUser(assignableUser.get(0), assignedUsername)) {
            assignableUsers.add(assignedUsername);
            return true;
        }

        throw new ApiException(-1, "Error on Assigned User checking for user: " + assignedUsername);
    }

    private boolean isSameUser(AssignableUserResponse fetchedUser, String assignedUser) {
        return fetchedUser.name().equals(assignedUser);
    }

    private String translateSquadToScaleExecStatus(String squadStatusName) {
        var squadStatusNameLower = squadStatusName.toLowerCase();
        return statusTranslation.getOrDefault(squadStatusNameLower, squadStatusName);
    }

    private String translateSquadToScaleVersion(String versionName) {
        if (versionName.equalsIgnoreCase(SQUAD_VERSION_UNSCHEDULED)) {
            return null;
        }
        return versionName;
    }

}
