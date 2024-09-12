package com.atlassian.migration.app.zephyr.common;

public class PropertySanitizer {

    public static String sanitizeAttachmentsBaseFolder(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Attachments mapped CSV file path is required.");
        }

        if (!filePath.endsWith("/")) {
            return filePath + "/";
        }

        return filePath;
    }


    public static String sanitizeHostAddress(String hostAddress) {

        if (hostAddress == null || hostAddress.isBlank()) {
            throw new IllegalArgumentException("Host address is required.");
        }

        if (hostAddress.endsWith("/")) {
            return hostAddress.substring(0, hostAddress.length() - 1);
        }

        return hostAddress;
    }
}
