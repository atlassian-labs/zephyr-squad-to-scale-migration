package com.atlassian.migration.app.zephyr.common;

import com.google.gson.Gson;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class BaseApi {
    private static final Logger logger = LoggerFactory.getLogger(BaseApi.class);

    private static final List<String> http1Versions = List.of("1.1", "1");
    private static final List<String> http2Versions = List.of("2", "2.0");

    protected final Gson gson;
    protected final HttpClient client;
    protected final ApiConfiguration config;

    private static final long TIMEOUT = 10L;
    private static final int MAX_RETRIES = 3;
    private static final int BACKOFF = 1000;
    private static final int BACKOFF_MULTIPLIER = 2;
    private static final List<Integer> retryEnabledCodes = List.of(408, 504, 500, 503);
    private static final Map<String, Decoder> decodersMap = Map.of(
            "gzip", new GzipDecoder(),
            "identity", new IdentityDecoder()
    );

    public BaseApi(ApiConfiguration config) {
        this.config = config;
        this.client = createClientWithVersion(config.httpVersion());
        this.gson = new Gson();
    }

    private HttpClient createClientWithVersion(String httpVersion) {

        if (http1Versions.contains(httpVersion)) {
            return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(TIMEOUT)).build();
        } else if (http2Versions.contains(httpVersion)) {
            return HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(TIMEOUT)).build();
        } else {
            throw new IllegalArgumentException("Unsupported HTTP version: "
                    + httpVersion
                    + ", Supported values: "
                    + "Http/1.1: " + http1Versions + " Http/2: " + http2Versions);
        }
    }

    private void runBackoffTimer(int tries) throws ApiException {

        var retry_backoff = BACKOFF * Math.max(BACKOFF_MULTIPLIER * tries, 1);

        logger.info("New attempt in " + retry_backoff + "ms...");
        try {
            Thread.sleep(retry_backoff);
        } catch (InterruptedException e) {
            logger.error("Failed to sleep", e);
            throw new ApiException(e);
        }

        logger.info("Retry triggered");
    }

    private String sendRequest(HttpRequest request) throws ApiException {

        HttpResponse<byte[]> response;

        for (int tries = 0; tries < MAX_RETRIES; tries++) {

            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            } catch (HttpConnectTimeoutException e) {
                logger.info("Http Connection Timed out, retrying...");
                runBackoffTimer(tries);
                continue;
            } catch (IOException | InterruptedException e) {
                logger.error("Failed to send API request", e);
                throw new ApiException(e);
            }

            var statusCode = response.statusCode();

            if (retryEnabledCodes.contains(statusCode)) {
                logger.info("Received code " + statusCode + ", retrying...");
                runBackoffTimer(tries);
                continue;
            }

            String body = decodeBody(response.body(),
                    response.headers().firstValue("Content-Encoding").orElse(""));

            if (statusCode / 100 != 2) {
                logger.error("Failed to send API request with status code: " + response.statusCode() + " and body: " + body);
                throw new ApiException(response.statusCode(), body);
            }

            return body;

        }

        throw new ApiException(-1, "Failed to execute API request after " + MAX_RETRIES + " retries. No answer from server.");
    }

    protected String decodeBody(byte[] encodedBody, String encoding) throws ApiException {

        var decoder = decodersMap.getOrDefault(encoding, new IdentityDecoder());
        try {
            return decoder.decode(encodedBody);
        } catch (Exception e) {
            throw new ApiException(-1, "Failed to decode response body\n");
        }

    }

    private HttpRequest httpRequest(
            BiFunction<HttpRequest.Builder, URI, HttpRequest.Builder> requestMethod,
            URI uri,
            Object data) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header("Authorization", auth())
                .header("Accept", "application/json, text/html")
                .header("Accept-Encoding", "identity;q=1.0");

        if (data != null) {
            builder.header("Content-Type", "application/json;charset=UTF-8");
        }

        return requestMethod.apply(builder, uri).build();
    }

    protected String sendHttpGet(URI uri) throws ApiException {
        var request = httpRequest(
                (builder, uriParam) -> builder.uri(uriParam).GET(),
                uri,
                null
        );

        return sendRequest(request);
    }

    protected String sendHttpPost(String query, Object data) throws ApiException {
        URI uri = getUri(urlPath(query));

        var request = httpRequest(
                (builder, uriParam) -> builder.uri(uriParam).POST(
                        HttpRequest.BodyPublishers.ofString(gson.toJson(data), UTF_8)
                ),
                uri,
                data
        );

        return sendRequest(request);
    }

    protected void sendHttpPut(String query, Object data) throws ApiException {
        URI uri = getUri(urlPath(query));

        var request = httpRequest(
                (builder, uriParam) -> builder.uri(uriParam).PUT(
                        HttpRequest.BodyPublishers.ofString(gson.toJson(data), UTF_8)
                ),
                uri,
                data
        );

        sendRequest(request);
    }

    protected String urlPath(String query, String... strings) {
        return String.format(config.host() + query, strings);
    }

    protected URI uri(String query, Map<String, Object> params) throws ApiException {
        try {
            var uri = new URIBuilder(urlPath(query));

            params.forEach((k, v) -> {
                if (v != null) {
                    uri.addParameter(k, String.valueOf(v));
                }
            });

            return uri.build();
        } catch (URISyntaxException e) {
            throw new ApiException(e);
        }
    }

    protected String auth() {
        return "Basic " + Base64.getEncoder().encodeToString((
                config.username() + ":" + String.valueOf(config.password())).getBytes());
    }

    protected static URI getUri(String a) throws ApiException {
        try {
            return new URIBuilder(a).build();
        } catch (URISyntaxException e) {
            throw new ApiException(e);
        }
    }
}
