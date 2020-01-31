package com.quantify.avory.plugins;

import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class to send HTTP requests to predefined sources
 */
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private static final String EXTERNAL_SERVICE = "https://postman-echo.com/post";

    /**
     * Async HTTP Post request to send message to service
     */
    public static void sendToExternalService(String messageHeader, String messageBody) {

        CompletableFuture<HttpResponse<JsonNode>> future = Unirest.post(EXTERNAL_SERVICE)
                .header("accept", "application/json")
                .field(messageHeader, messageBody)
                .asJsonAsync(new Callback<JsonNode>() {

                    @Override
                    public void failed(UnirestException e) {
                        log.error("POST Failure! UUID: " + messageBody + " not sent!");
                    }

                    @Override
                    public void completed(HttpResponse<JsonNode> httpResponse) {
                        log.debug("POST Success! UUID: " + messageBody + " sent!");
                    }

                    @Override
                    public void cancelled() {
                        log.debug("POST Cancelled!! UUID: " + messageBody + " not sent!");
                    }

                });
    }
}
