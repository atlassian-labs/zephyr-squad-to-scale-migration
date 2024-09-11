package atlassian.migration.app.zephyr.squad.model;

import java.util.List;

public record FetchSquadAttachmentResponse (
        List<SquadAttachmentItemResponse> data
){}
