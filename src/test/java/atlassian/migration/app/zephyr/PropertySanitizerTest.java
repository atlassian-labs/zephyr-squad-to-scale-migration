package atlassian.migration.app.zephyr;

import atlassian.migration.app.zephyr.common.PropertySanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PropertySanitizerTest {

    @Test
    void shouldSanitizeAttachmentsBaseFolder() {
        var pathWithoutSlash = "path/to/attachments";
        var pathWithSlash = "path/to/attachments/";

        var result = PropertySanitizer.sanitizeAttachmentsBaseFolder(pathWithoutSlash);

        assertEquals("path/to/attachments/", result);

        result = PropertySanitizer.sanitizeAttachmentsBaseFolder(pathWithSlash);

        assertEquals("path/to/attachments/", result);
    }

    @Test
    void shouldThrowExceptionWhenAttachmentsBaseFolderIsNullOrEmpty() {
        var path = "";

        try {
            PropertySanitizer.sanitizeAttachmentsBaseFolder(path);
        } catch (IllegalArgumentException e) {
            assertEquals("Attachments mapped CSV file path is required.", e.getMessage());
        }

        try {
            PropertySanitizer.sanitizeAttachmentsBaseFolder(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Attachments mapped CSV file path is required.", e.getMessage());
        }

    }


    @Test
    void shouldSanitizeHostAddress() {
        var hostAddressWithoutSlash = "http://localhost:8080";
        var hostAddressWithSlash = "http://localhost:8080/";

        var result = PropertySanitizer.sanitizeHostAddress(hostAddressWithoutSlash);

        assertEquals("http://localhost:8080", result);

        result = PropertySanitizer.sanitizeHostAddress(hostAddressWithSlash);

        assertEquals("http://localhost:8080", result);
    }

    @Test
    void shouldThrowExceptionWhenHostAddressIsNullOrEmpty() {
        var hostAddress = "";

        try {
            PropertySanitizer.sanitizeHostAddress(hostAddress);
        } catch (IllegalArgumentException e) {
            assertEquals("Host address is required.", e.getMessage());
        }

        try {
            PropertySanitizer.sanitizeHostAddress(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Host address is required.", e.getMessage());
        }
    }
}
