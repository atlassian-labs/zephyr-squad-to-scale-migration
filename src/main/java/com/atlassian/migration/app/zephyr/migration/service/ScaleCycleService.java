package com.atlassian.migration.app.zephyr.migration.service;

import com.atlassian.migration.app.zephyr.scale.api.ScaleApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScaleCycleService implements Resettable {

    private static final Logger logger = LoggerFactory.getLogger(ScaleCycleService.class);

    private final Map<String, String> mapCreatedScaleCycles = new HashMap<>();
    private final ScaleApi scaleApi;
    private final String defaultCycleKey;

    public ScaleCycleService(ScaleApi scaleApi, String defaultCycleKey) {
        this.scaleApi = scaleApi;
        this.defaultCycleKey = defaultCycleKey;
    }

    @Override
    public void reset() {
        mapCreatedScaleCycles.clear();
    }

    public String getCycleKeyBySquadCycleName(String squadCycleName, String projectKey, String versionName) {

        var squadCycleVersion = translateSquadToScaleVersion(versionName);

        if (!mapCreatedScaleCycles.containsKey(squadCycleName)) {
            var newScaleCycleKey = createNewScaleCycle(squadCycleName, projectKey, squadCycleVersion);
            mapCreatedScaleCycles.put(squadCycleName, newScaleCycleKey);
        }

        return mapCreatedScaleCycles.get(squadCycleName);
    }

    private String translateSquadToScaleVersion(String versionName) {
        if (versionName.equalsIgnoreCase("unscheduled")) {
            return null;
        }
        return versionName;
    }

    private String createNewScaleCycle(String squadCycleName, String projectKey, String cycleVersion) {
        try {
            var scaleCycleName = defaultCycleKey.isBlank() ? squadCycleName : defaultCycleKey;

            logger.info("Creating test cycle...");

            var scaleCycleKey = scaleApi.createMigrationTestCycle(projectKey, scaleCycleName, cycleVersion);

            logger.info("Test Cycle created successfully");

            return scaleCycleKey;
        } catch (IOException exception) {
            logger.error("Failed to create new Scale cycle." + exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }

    }
}
