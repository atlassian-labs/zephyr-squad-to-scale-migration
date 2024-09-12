package com.atlassian.migration.app.zephyr.common;

import java.nio.charset.StandardCharsets;

public class IdentityDecoder implements Decoder {
    @Override
    public String decode(byte[] encodedData) {
        return new String(encodedData, StandardCharsets.UTF_8);
    }
}
