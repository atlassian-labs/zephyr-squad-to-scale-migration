package atlassian.migration.app.zephyr.squad.model;

import java.util.List;

public record SquadTestStepResponse(
        String id,
        String orderId,
        String htmlStep,
        String htmlData,
        String htmlResult,
        List<SquadAttachmentItemResponse> attachmentsMap

) { }