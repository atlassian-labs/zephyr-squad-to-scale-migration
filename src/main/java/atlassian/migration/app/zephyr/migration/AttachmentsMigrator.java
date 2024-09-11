package atlassian.migration.app.zephyr.migration;

import atlassian.migration.app.zephyr.common.ApiException;
import atlassian.migration.app.zephyr.common.ProgressBarUtil;
import atlassian.migration.app.zephyr.jira.api.JiraApi;
import atlassian.migration.app.zephyr.migration.model.*;
import atlassian.migration.app.zephyr.scale.api.ScaleApi;
import atlassian.migration.app.zephyr.scale.database.ScaleTestCaseRepository;
import atlassian.migration.app.zephyr.scale.model.GetProjectResponse;
import atlassian.migration.app.zephyr.scale.model.ScaleGETStepItemPayload;
import atlassian.migration.app.zephyr.squad.api.SquadApi;
import atlassian.migration.app.zephyr.squad.model.FetchSquadAttachmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttachmentsMigrator {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentsMigrator.class);

    private final JiraApi jiraApi;
    private final ScaleApi scaleApi;
    private final SquadApi squadApi;

    private final DriverManagerDataSource dataSource;

    private final AttachmentsCsvExporter attachmentsCsvExporter;

    private final AttachmentsCopier attachmentsCopier;

    private final Map<String, GetProjectResponse> projectMetadata = new HashMap<>();

    private static final String[] csvHeader = {"FILE_NAME", "FILE_SIZE", "NAME", "PROJECT_ID", "USER_KEY", "TEMPORARY",
            "CREATED_ON", "MIME_TYPE", "TEST_CASE_ID", "STEP_ID", "TEST_RESULT_ID"};

    private static final String[] csvMapping = {"FileName", "Size", "AttachmentName", "ProjectId", "AuthorKey", "Temporary",
            "CreatedOn", "MimeType", "TestCaseId", "StepId", "TestResultId"};

    public AttachmentsMigrator(JiraApi jiraApi, ScaleApi scaleApi, SquadApi squadApi,
                               DriverManagerDataSource dataSource, AttachmentsCsvExporter attachmentsCsvExporter, AttachmentsCopier attachmentsCopier) {

        this.jiraApi = jiraApi;
        this.scaleApi = scaleApi;
        this.squadApi = squadApi;
        this.attachmentsCsvExporter = attachmentsCsvExporter;
        this.dataSource = dataSource;
        this.attachmentsCopier = attachmentsCopier;

    }

    public void export(SquadToScaleEntitiesMap entitiesMap, String projectKey) throws IOException {

        var project = projectMetadata.computeIfAbsent(projectKey, key -> {
            try {
                return jiraApi.getProjectByKeyWithHistoricalKeys(key);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<List<AttachmentAssociationData>> processingTestCases = startTestCaseAsyncProcessing(project.id(),
                entitiesMap.testCaseMap());

        CompletableFuture<List<AttachmentAssociationData>> processingTestSteps = startTestStepAsyncProcessing(project.id(),
                entitiesMap.testStepMap());

        CompletableFuture<List<AttachmentAssociationData>> processingTestExecutions = startTestExecAsyncProcessing(project.id(),
                entitiesMap.testExecutionMap());

        CompletableFuture<List<AttachmentAssociationData>> attachmentsMapped = CompletableFuture
                .allOf(processingTestCases, processingTestSteps, processingTestExecutions)
                .thenApply(unused -> Stream.concat(Stream.concat(
                        processingTestCases.join().stream(),
                        processingTestSteps.join().stream()), processingTestExecutions.join().stream()).toList()
                );


        try {
            logger.info("Copying attachments to Scale directory");
            attachmentsCopier.copyAttachments(attachmentsMapped.get(), project.key(), project.projectKeys());
            logger.info("Exporting mapped attachments to csv");
            attachmentsCsvExporter.dump(attachmentsMapped.get(), csvHeader, csvMapping);
            logger.info("Exporting attachments to csv finished");
        } catch (InterruptedException | ExecutionException | IOException | URISyntaxException e) {
            logger.error("Failed to migrate attachments " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<List<AttachmentAssociationData>> startTestCaseAsyncProcessing(String projectId,
                                                                                            SquadToScaleTestCaseMap testCaseMap) {
        logger.info("Starting to process Test Cases attachments asynchronously");
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return processTestCases(projectId, testCaseMap);
                    } catch (IOException e) {
                        logger.error("Failed to map Test Cases Attachments " + e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                });
    }

    private CompletableFuture<List<AttachmentAssociationData>> startTestStepAsyncProcessing(String projectId,
                                                                                            SquadToScaleTestStepMap testStepMap) {

        logger.info("Starting to process Test Steps attachments asynchronously");
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return processTestSteps(projectId, testStepMap);
                    } catch (IOException e) {
                        logger.error("Failed to map Test Steps Attachments " + e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                });

    }

    private CompletableFuture<List<AttachmentAssociationData>> startTestExecAsyncProcessing(String projectId,
                                                                                            SquadToScaleTestExecutionMap testExecutionMap) {
        logger.info("Starting to process Test Executions attachments asynchronously");
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return processTestExecutions(projectId, testExecutionMap);
                    } catch (IOException e) {
                        logger.error("Failed to map Test Run Attachments " + e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                });
    }

    private List<AttachmentAssociationData> processTestCases(String projectId, SquadToScaleTestCaseMap testCaseMap) throws IOException {

        List<AttachmentAssociationData> attachmentsMapped = new ArrayList<>();

        ScaleTestCaseRepository testCaseRepository = new ScaleTestCaseRepository(dataSource);

        var startTimeMillis = System.currentTimeMillis();
        var testCaseIndex = 0;
        var testCaseEntrySet = testCaseMap.entrySet();

        for (var testCaseMapped : testCaseEntrySet) {

            if (testCaseIndex % 100 == 0) {
                logger.info("Test case attachment progress: "
                        + ProgressBarUtil.getProgressBar(testCaseIndex++, testCaseEntrySet.size(), startTimeMillis));
            }

            var scaleTestCaseKey = testCaseMapped.getValue();
            var squadTestCase = testCaseMapped.getKey();

            var scaleTestCaseEntity = testCaseRepository.getByKey(scaleTestCaseKey);

            if (scaleTestCaseEntity.isEmpty()) {
                logger.error("Couldn't find the Scale Test Case needed for attachment mapping");
                throw new IOException();
            }

            var issueAttachments = jiraApi.getIssueAttachmentsByIssueId(squadTestCase.testCaseId());

            attachmentsMapped.addAll(issueAttachments.stream().map(
                    attachment -> AttachmentAssociationData.createAttachmentAssociationDataFromTestCase(
                            attachment.filename(),
                            attachment.id(),
                            null,
                            attachment.size(),
                            attachment.author().key(),
                            projectId,
                            String.valueOf(scaleTestCaseEntity.get().id()),
                            new SquadOriginEntity(testCaseMapped.getKey().testCaseId(), testCaseMapped.getKey().testCaseKey()))).toList());
        }

        logger.info("Test case attachment progress: "
                + ProgressBarUtil.getProgressBar(testCaseEntrySet.size(), testCaseEntrySet.size(), startTimeMillis));
        logger.info("Test Cases attachments processing finished");
        return attachmentsMapped;
    }

    private List<AttachmentAssociationData> processTestSteps(String projectId,
                                                             SquadToScaleTestStepMap testStepMap) throws IOException {

        List<AttachmentAssociationData> attachmentsMapped = new ArrayList<>();

        var startTimeMillis = System.currentTimeMillis();
        var testStepIndex = 0;
        var testStepEntrySet = testStepMap.entrySet();

        for (var testStepMapped : testStepMap.entrySet()) {

            if (testStepIndex % 100 == 0) {
                logger.info("Step attachment progress for project: "
                        + ProgressBarUtil.getProgressBar(testStepIndex++, testStepEntrySet.size(), startTimeMillis));
            }

            var stepsFromScale = scaleApi.fetchTestStepsFromTestCaseKey(testStepMapped.getKey());

            Map<Integer, String> scaleStepData = stepsFromScale.testScript().steps.stream().collect(Collectors.toMap(
                    step -> Integer.valueOf(step.index()), ScaleGETStepItemPayload::id));

            for (var attachmentsPerOrder : testStepMapped.getValue().entrySet()) {

                attachmentsMapped.addAll(attachmentsPerOrder.getValue().stream().map(
                        attachment -> AttachmentAssociationData.createAttachmentAssociationDataFromTestStep(
                                attachment.fileName(),
                                attachment.fileId(),
                                null,
                                attachment.fileSize(),
                                attachment.author(),
                                projectId,
                                scaleStepData.get(Integer.parseInt(attachmentsPerOrder.getKey().stepOrder()) - 1),
                                new SquadOriginEntity(attachmentsPerOrder.getKey().stepId(), ""))).toList());

            }
        }

        logger.info("Test Steps attachments processing finished");
        return attachmentsMapped;

    }

    private List<AttachmentAssociationData> processTestExecutions(String projectId,
                                                                  SquadToScaleTestExecutionMap testExecutionMap) throws IOException {

        var startTimeMillis = System.currentTimeMillis();
        var testExecutionIndex = 0;
        var testExecutionEntrySet = testExecutionMap.entrySet();

        List<AttachmentAssociationData> attachmentsMapped = new ArrayList<>();

        for (var testExecutionMapped : testExecutionEntrySet) {

            if (testExecutionIndex % 100 == 0) {
                logger.info("Execution attachment progress for project: "
                        + ProgressBarUtil.getProgressBar(testExecutionIndex++, testExecutionEntrySet.size(), startTimeMillis));
            }

            FetchSquadAttachmentResponse testExecAttachments = squadApi.fetchTestExecutionAttachmentById(testExecutionMapped.getKey().testExecutionId());


            attachmentsMapped.addAll(testExecAttachments.data().stream().map(
                    attachment -> AttachmentAssociationData.createAttachmentAssociationDataFromTestExecution(
                            attachment.fileName(),
                            attachment.fileId(),
                            null,
                            attachment.fileSize(),
                            attachment.author(),
                            projectId,
                            testExecutionMapped.getValue(),
                            new SquadOriginEntity(testExecutionMapped.getKey().testExecutionId(), ""))).toList());
        }

        logger.info("Test Executions attachments processing finished");
        return attachmentsMapped;

    }
}
