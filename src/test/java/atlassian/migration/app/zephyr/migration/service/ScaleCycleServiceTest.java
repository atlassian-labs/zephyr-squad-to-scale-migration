package atlassian.migration.app.zephyr.migration.service;


import atlassian.migration.app.zephyr.common.ZephyrApiException;
import atlassian.migration.app.zephyr.scale.api.ScaleApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScaleCycleServiceTest {

    private final String testCaseKeyMock = "TEST";

    @Mock
    private ScaleApi scaleApi;

    private ScaleCycleService sutScaleCycleService;

    @BeforeEach
    void setup() {
        sutScaleCycleService = new ScaleCycleService(scaleApi, "");
    }

    @Test
    void shouldReturnScaleCycleKey() throws ZephyrApiException {

        var squadCycleNameMock = "SQUAD_CYCLE";
        var versionMock = "VERSION";
        var expectedScaleCycleKey = "CYCLE-1";

        when(scaleApi.createMigrationTestCycle(testCaseKeyMock, squadCycleNameMock, versionMock))
                .thenReturn(expectedScaleCycleKey);

        var receivedScaleCycleKey = sutScaleCycleService
                .getCycleKeyBySquadCycleName(squadCycleNameMock, testCaseKeyMock, versionMock);

        assertEquals(expectedScaleCycleKey, receivedScaleCycleKey);
    }

    @Test
    void shouldUseCacheForCycleAlreadyCreated() throws ZephyrApiException {

        var squadCycleNameMock = "SQUAD_CYCLE";
        var versionMock = "VERSION";
        var scaleCycleKeyMock = "CYCLE-1";

        when(scaleApi.createMigrationTestCycle(testCaseKeyMock, squadCycleNameMock, versionMock))
                .thenReturn(scaleCycleKeyMock);

        sutScaleCycleService
                .getCycleKeyBySquadCycleName(squadCycleNameMock, testCaseKeyMock, versionMock);


        sutScaleCycleService
                .getCycleKeyBySquadCycleName(squadCycleNameMock, testCaseKeyMock, versionMock);


        verify(scaleApi, times(1))
                .createMigrationTestCycle(testCaseKeyMock, squadCycleNameMock, versionMock);


    }
}
