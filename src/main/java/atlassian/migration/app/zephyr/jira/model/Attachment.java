package atlassian.migration.app.zephyr.jira.model;

public record Attachment(
        String self,
        String id,
        String filename,
        Author author,
        String created,
        String size,
        String mimetype

) {
}
