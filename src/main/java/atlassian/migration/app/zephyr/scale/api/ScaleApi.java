package atlassian.migration.app.zephyr.scale.api;

import atlassian.migration.app.zephyr.common.ApiConfiguration;
import atlassian.migration.app.zephyr.common.ApiException;
import atlassian.migration.app.zephyr.common.BaseApi;
import atlassian.migration.app.zephyr.common.ZephyrApiException;
import atlassian.migration.app.zephyr.scale.model.*;
import com.google.gson.JsonSyntaxException;
import org.openqa.selenium.json.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScaleApi extends BaseApi {

    private static final Logger logger = LoggerFactory.getLogger(ScaleApi.class);

    public static final String ENABLE_PROJECT_ENDPOINT = "/rest/atm/1.0/project";
    public static final String CREATE_CUSTOM_FIELD_ENDPOINT = "/rest/atm/1.0/customfield";
    public static final String CREATE_SCALE_TEST_CASE_ENDPOINT = "/rest/atm/1.0/testcase";
    public static final String SCALE_TEST_STEP_ENDPOINT = "/rest/atm/1.0/testcase/%s";
    public static final String CREATE_SCALE_MIGRATION_TEST_CYCLE_ENDPOINT = "/rest/atm/1.0/testrun";
    public static final String CREATE_SCALE_TEST_RESULTS_ENDPOINT = "/rest/atm/1.0/testrun/%s/testresults";

    public static final String CUSTOM_FIELD_DUPLICATED_EXPECTED_MESSAGE = "Custom field name is duplicated";

    public ScaleApi(ApiConfiguration config) {
        super(config);
    }

    public String createTestCases(ScaleTestCaseCreationPayload testCaseCreationPayload) throws ZephyrApiException {

        String response = "";
        Map<String, Object> result = new HashMap<>();
        try {
            response = sendHttpPost(CREATE_SCALE_TEST_CASE_ENDPOINT, testCaseCreationPayload);

            result = gson.fromJson(response, Map.class);
        } catch (ApiException e) {
            ScaleApiErrorLogger.logAndThrow(ScaleApiErrorLogger.ERROR_CREATE_TEST_CASE, e);
        } catch (JsonSyntaxException e) {
            ScaleApiErrorLogger.logAndThrow(
                    String.format(ScaleApiErrorLogger.ERROR_CREATE_TEST_CASE_PAYLOAD_PARSE, response),
                    new ApiException(e));

        }

        return (String) result.get("key");
    }

    public void updateTestStep(String key, SquadUpdateStepPayload step) throws ZephyrApiException {
        try {
            sendHttpPut(String.format(SCALE_TEST_STEP_ENDPOINT, key), step);
        } catch (ApiException e) {
            ScaleApiErrorLogger.logAndThrow(String.format(ScaleApiErrorLogger.ERROR_CREATE_TEST_STEP, key), e);
        }
    }

    public ScaleGETStepsPayload fetchTestStepsFromTestCaseKey(String key) throws ZephyrApiException {

        try {
            var response = sendHttpGet(getUri(urlPath(SCALE_TEST_STEP_ENDPOINT, key)));

            return gson.fromJson(response, ScaleGETStepsPayload.class);
        } catch (ApiException e) {
            ScaleApiErrorLogger.logAndThrow(String.format(ScaleApiErrorLogger.ERROR_FETCHING_TEST_STEP, key), e);
        }
        return new ScaleGETStepsPayload("", "", new SquadGETStepItemPayload());
    }

    public String createMigrationTestCycle(String projectKey, String cycleName, String cycleVersion) throws ZephyrApiException {

        Map<String, Object> params = new HashMap<>();
        params.put("version", cycleVersion);
        params.put("name", cycleName);
        params.put("projectKey", projectKey);

        String response = "";
        try {
            response = sendHttpPost(CREATE_SCALE_MIGRATION_TEST_CYCLE_ENDPOINT, params);
        } catch (ApiException e) {
            ScaleApiErrorLogger.logAndThrow(ScaleApiErrorLogger.ERROR_CREATE_TEST_CYCLE, e);

        }

        Map<String, Object> result = gson.fromJson(response, Map.class);
        return (String) result.get("key");
    }

    public ScalePOSTTestResultPayload createTestResults(String cycleKey, List<ScaleExecutionCreationPayload> datas) throws ZephyrApiException {
        try {
            var response = sendHttpPost(String.format(CREATE_SCALE_TEST_RESULTS_ENDPOINT, cycleKey), datas);
            Type testResultCreatedPayload = new TypeToken<List<ScaleTestResultCreatedPayload>>() {
            }.getType();
            return new ScalePOSTTestResultPayload(gson.fromJson(response, testResultCreatedPayload));

        } catch (ApiException e) {
            ScaleApiErrorLogger.logAndThrow(String.format(ScaleApiErrorLogger.ERROR_CREATE_TEST_RESULTS, cycleKey), e);
        }
        return new ScalePOSTTestResultPayload(Collections.emptyList());
    }

    public ScaleTestResultCreatedPayload createTestExecution(String cycleKey, ScaleExecutionCreationPayload data)
            throws ZephyrApiException {
        //Test Results creation endpoint only accepts a List of Test Results as payload
        return createTestResults(cycleKey, List.of(data)).testResultsCreated().get(0);
    }

    public void enableProject(EnableProjectPayload enableProjectPayload) throws ZephyrApiException {
        try {
            sendHttpPost(ENABLE_PROJECT_ENDPOINT, enableProjectPayload);
        } catch (ApiException e) {
            ScaleApiErrorLogger.logAndThrow(ScaleApiErrorLogger.ERROR_ENABLE_PROJECT, e);
        }
    }

    public void createCustomField(ScaleCustomFieldPayload scaleCustomFieldPayload) throws ZephyrApiException {

        try {
            sendHttpPost(CREATE_CUSTOM_FIELD_ENDPOINT, scaleCustomFieldPayload);
        } catch (ApiException e) {
            //While creating a custom field that already exists, Scale API returns a 400 status with the
            //message "Custom field name is duplicated". Catching it here and ignoring this status
            if (e.code == 400 && e.getMessage().contains(CUSTOM_FIELD_DUPLICATED_EXPECTED_MESSAGE)) {
                return;
            }

            ScaleApiErrorLogger.logAndThrow(
                    ScaleApiErrorLogger.ERROR_CREATE_CUSTOM_FIELD, e
            );

        }
    }

    private static class ScaleApiErrorLogger {

        public static final String ERROR_CREATE_TEST_CASE = "Error while creating Test Case at " +
                CREATE_SCALE_TEST_CASE_ENDPOINT;

        public static final String ERROR_CREATE_TEST_CASE_PAYLOAD_PARSE = "Error while creating Test Case at " +
                CREATE_SCALE_TEST_CASE_ENDPOINT + " - Unexpected Payload received: %s \n";

        public static final String ERROR_CREATE_TEST_STEP = "Error while creating Test Steps at " +
                SCALE_TEST_STEP_ENDPOINT;

        public static final String ERROR_FETCHING_TEST_STEP = "Error while fetching Test Steps at " +
                SCALE_TEST_STEP_ENDPOINT;

        public static final String ERROR_CREATE_TEST_CYCLE = "Error while creating Test Cycle at "
                + CREATE_SCALE_MIGRATION_TEST_CYCLE_ENDPOINT;

        public static final String ERROR_CREATE_TEST_RESULTS = "Error while creating Test Results at "
                + CREATE_SCALE_TEST_RESULTS_ENDPOINT;

        public static final String ERROR_CREATE_CUSTOM_FIELD = "Error while creating Custom Fields at "
                + CREATE_CUSTOM_FIELD_ENDPOINT;
        public static final String ERROR_ENABLE_PROJECT = "Error while enabling Project as Scale project at "
                + ENABLE_PROJECT_ENDPOINT;

        public static void logAndThrow(String message, ApiException e) throws ZephyrApiException {
            logger.error(message + " " + e.getMessage(), e);

            throw new ZephyrApiException(e);
        }

    }
}
