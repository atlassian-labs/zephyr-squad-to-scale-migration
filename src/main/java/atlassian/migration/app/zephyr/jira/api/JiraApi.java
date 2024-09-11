package atlassian.migration.app.zephyr.jira.api;

import atlassian.migration.app.zephyr.common.ApiConfiguration;
import atlassian.migration.app.zephyr.common.ApiException;
import atlassian.migration.app.zephyr.common.BaseApi;
import atlassian.migration.app.zephyr.jira.model.*;
import atlassian.migration.app.zephyr.scale.model.GetProjectResponse;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JiraApi extends BaseApi {

    public static final String JIRA_SEARCH_ISSUES_ENDPOINT = "/rest/api/2/search";
    public static final String JIRA_SEARCH_ASSIGNABLE_USERS = "/rest/api/2/user/assignable/search";
    public static final String GET_PROJECT_BY_KEY_OR_ID_ENDPOINT = "/rest/api/2/project/%s";
    public static final String GET_PROJECT_WITH_HISTORICAL_KEYS = GET_PROJECT_BY_KEY_OR_ID_ENDPOINT + "?expand=projectKeys";
    public static final String GET_ISSUE_BY_ID_ENDPOINT = "/rest/api/2/issue/%s";
    public static final String RENDER_JIRA_TEXT_FORMATTING = "/rest/api/1.0/render";

    public JiraApi(ApiConfiguration config) {
        super(config);
    }

    public int fetchTotalIssuesByProjectName(String projectName) throws IOException {
        return fetchTestCreatedOrderEntry(projectName, null, null).total();
    }

    public GetProjectResponse getProjectById(String projectId) throws IOException {
        return getProject(projectId);
    }

    public GetProjectResponse getProjectByKey(String projectKey) throws IOException {
        return getProject(projectKey);
    }

    public GetProjectResponse getProjectByKeyWithHistoricalKeys(String projectKey) throws ApiException {
        var response = sendHttpGet(getUri(urlPath(GET_PROJECT_WITH_HISTORICAL_KEYS, projectKey)));
        return gson.fromJson(response, GetProjectResponse.class);
    }

    public JiraIssuesResponse getIssueById(String id) throws IOException {
        var response = sendHttpGet(getUri(urlPath(GET_ISSUE_BY_ID_ENDPOINT, id)));
        return gson.fromJson(response, JiraIssuesResponse.class);
    }

    public List<Attachment> getIssueAttachmentsByIssueId(String id) throws IOException {
        return getIssueById(id).fields().attachment;
    }

    public GetProjectResponse getProject(String idOrKey) throws IOException {
        var response = sendHttpGet(getUri(urlPath(GET_PROJECT_BY_KEY_OR_ID_ENDPOINT, idOrKey)));
        return gson.fromJson(response, GetProjectResponse.class);
    }

    public List<JiraIssuesResponse> fetchIssuesOrderedByCreatedDate(String projectName, Integer startAt, Integer maxResults) throws IOException {
        return fetchTestCreatedOrderEntry(projectName, startAt, maxResults).issues();
    }

    public FetchJiraIssuesResponse fetchTestCreatedOrderEntry(String projectName, Integer startAt, Integer maxResults) throws IOException {
        return fetchIssuesByJql(startAt, maxResults, String.format("project = %s AND issuetype = Test ORDER BY createdDate ASC", projectName));
    }


    public String convertJiraTextFormattingToHtml(String textToConvert) throws IOException {
        return sendHttpPost(RENDER_JIRA_TEXT_FORMATTING, new RenderJiraTextFormatting(textToConvert));
    }

    public List<AssignableUserResponse> fetchAssignableUserByUsernameAndProject(String username, String projectKey) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("project", projectKey);

        var response = sendHttpGet(uri(JIRA_SEARCH_ASSIGNABLE_USERS, params));

        Type listType = new TypeToken<List<AssignableUserResponse>>() {
        }.getType();

        return gson.fromJson(response, listType);
    }

    private FetchJiraIssuesResponse fetchIssuesByJql(Integer startAt, Integer maxResults, String jql) throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("jql", jql);
        params.put("startAt", startAt);
        params.put("maxResults", maxResults);

        var response = sendHttpGet(
                uri(JIRA_SEARCH_ISSUES_ENDPOINT, params)
        );

        return gson.fromJson(response, FetchJiraIssuesResponse.class);
    }

}
