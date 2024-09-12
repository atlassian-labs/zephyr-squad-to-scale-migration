package com.atlassian.migration.app.zephyr.common;

public record ApiConfiguration(String host, String username, char[] password, String httpVersion) {
}
