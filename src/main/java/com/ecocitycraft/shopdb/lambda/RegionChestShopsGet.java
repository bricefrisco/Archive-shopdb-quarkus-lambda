package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.RegionController;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;

@Named("region-chest-shops-get")
public class RegionChestShopsGet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    RegionController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            HashMap<String, Object> params = ParameterUtil.mapParams(
                    event.getQueryStringParameters(),
                    context.getLogger(),
                    "server", "name", "page", "pageSize", "tradeType"
            );

            PaginatedResponse<ChestShopDto> result = controller.getRegionChestShops(
                    (Server) params.get("server"),
                    (String) params.get("name"),
                    (Integer) params.get("page"),
                    (Integer) params.get("pageSize"),
                    (TradeType) params.get("tradeType")
            );

            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
