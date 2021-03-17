package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.ChestShopController;
import com.ecocitycraft.shopdb.models.chestshops.ShopEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Named("chest-shop-post")
public class ChestShopPost implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    ChestShopController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            context.getLogger().log("Body: " + event.getBody());
            List<ShopEvent> events = MAPPER.readValue(Base64.getDecoder().decode(event.getBody().getBytes(StandardCharsets.UTF_8)), new TypeReference<>(){});
            String result = controller.createChestShopSigns(events);
            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
