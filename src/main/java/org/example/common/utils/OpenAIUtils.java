package org.example.common.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class OpenAIUtils {

    private OpenAIUtils() {}

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Calls LLM and provides its response.
     *
     * @param url url to call LLM
     * @param contentType [application/json, ...]
     * @param bodyPublisher {@link java.net.http.HttpRequest.BodyPublisher} with request body
     * @return response from LLM in String format
     */
    public static String call(String url,
                              String contentType,
                              HttpRequest.BodyPublisher bodyPublisher
    ) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + Constant.API_KEY)
            .header("Content-Type", contentType)
            .POST(bodyPublisher)
            .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
    }
}
