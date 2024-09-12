package com.atlassian.migration.app.zephyr.squad.model;

public record SquadExecutionItemResponse(
        String id,
        int executionStatus,
        Object createdBy,
        String createdByUserName,
        String versionName,
        String htmlComment,
        String cycleName,
        String folderName,
        String executedOn,
        String assignedTo,
        String assignedToDisplay,
        String assignedToUserName) {
}
