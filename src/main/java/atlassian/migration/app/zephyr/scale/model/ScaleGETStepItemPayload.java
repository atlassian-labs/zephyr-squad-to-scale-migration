package atlassian.migration.app.zephyr.scale.model;

public record ScaleGETStepItemPayload(
        String description,
        String testData,
        String expectedResult,
        String id,
        String index
) {

    //To Create a test step on Scale we don't need ID and Index, but we must set it to null, otherwise Scale
    //API will try to process these fields and throw an ERROR 500
    public static ScaleGETStepItemPayload createScaleGETStepItemPayloadForCreation(
            String description,
            String testData,
            String expectedResult
    ){
        return new ScaleGETStepItemPayload(
                description,
                testData,
                expectedResult,
                null,
                null
        );
    }

}