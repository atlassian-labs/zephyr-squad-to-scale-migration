package atlassian.migration.app.zephyr.squad.model;

public record SquadAttachmentItemResponse (
    String fileName,
    String dateCreated,
    String htmlComment,
    String fileSize,
    String fileIcon,
    String author,
    String fileIconAltText,
    String comment,
    String fileId
)
{}
