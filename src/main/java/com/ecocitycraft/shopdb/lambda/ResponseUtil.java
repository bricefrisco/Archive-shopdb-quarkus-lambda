package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.exceptions.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.HashMap;

public final class ResponseUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static APIGatewayProxyResponseEvent mapResponse(Object o, int statusCode) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(headers)
                    .withBody(MAPPER.writeValueAsString(o))
                    .withIsBase64Encoded(Boolean.FALSE);
        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(headers)
                    .withBody(e.getMessage())
                    .withIsBase64Encoded(Boolean.FALSE);
        }
    }

    public static APIGatewayProxyResponseEvent mapResponse(Exception e) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "Internal Server Error";
        errorResponse.status = 500;
        errorResponse.message = e.getMessage();
        errorResponse.timestamp = new Timestamp(System.currentTimeMillis());

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(headers)
                    .withBody(MAPPER.writeValueAsString(errorResponse))
                    .withIsBase64Encoded(Boolean.FALSE);
        } catch (JsonProcessingException jpe) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(headers)
                    .withBody(e.getMessage())
                    .withIsBase64Encoded(Boolean.FALSE);
        }
    }
}
