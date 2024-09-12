package com.atlassian.migration.app.zephyr.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GzipDecoder implements Decoder {

    public String decode(byte[] encodedData) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(encodedData));
             ByteArrayOutputStream decodedOutput = new ByteArrayOutputStream()) {

            byte[] chunk = new byte[1024];
            int bytesRead;

            while ((bytesRead = gzipInputStream.read(chunk)) != -1) {
                decodedOutput.write(chunk, 0, bytesRead);
            }

            return decodedOutput.toString(UTF_8);
        } catch (IOException e) {
            throw new IOException("Failed to decode gzipped data", e);
        }
    }
}
