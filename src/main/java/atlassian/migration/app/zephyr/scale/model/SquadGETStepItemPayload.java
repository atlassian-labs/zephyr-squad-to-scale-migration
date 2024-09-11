package atlassian.migration.app.zephyr.scale.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SquadGETStepItemPayload {
    public String type = "STEP_BY_STEP";
    public List<ScaleGETStepItemPayload> steps = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SquadGETStepItemPayload that = (SquadGETStepItemPayload) o;
        return Objects.equals(type, that.type) && Objects.equals(steps, that.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, steps);
    }
}