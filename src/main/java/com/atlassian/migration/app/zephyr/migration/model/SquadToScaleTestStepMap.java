package com.atlassian.migration.app.zephyr.migration.model;

import com.atlassian.migration.app.zephyr.squad.model.SquadAttachmentItemResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SquadToScaleTestStepMap extends HashMap<String, Map<SquadToScaleTestStepMap.TestStepMapKey, List<SquadAttachmentItemResponse>>> {
    public record TestStepMapKey(String stepId, String stepOrder){}
}
