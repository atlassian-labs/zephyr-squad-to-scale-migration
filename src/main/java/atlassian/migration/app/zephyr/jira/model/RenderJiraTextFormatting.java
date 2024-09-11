package atlassian.migration.app.zephyr.jira.model;

public record RenderJiraTextFormatting(String rendererType, String unrenderedMarkup) {

    public RenderJiraTextFormatting(String unrenderedMarkup) {
        this("atlassian-wiki-renderer", unrenderedMarkup);
    }

}
