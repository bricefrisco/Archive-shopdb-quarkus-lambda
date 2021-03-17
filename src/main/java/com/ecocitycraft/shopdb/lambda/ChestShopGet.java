package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.ChestShopController;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;

@Named("chest-shop-get")
public class ChestShopGet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    ChestShopController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            HashMap<String, Object> params = ParameterUtil.mapParams(
                    event.getQueryStringParameters(),
                    context.getLogger(),
                    "page", "pageSize", "material", "server", "tradeType", "hideUnavailable", "sortBy", "distinct"
            );

            PaginatedResponse<ChestShopDto> result = controller.getChestShopSigns(
                    (Integer) params.get("page"),
                    (Integer) params.get("pageSize"),
                    (String) params.get("material"),
                    (Server) params.get("server"),
                    (TradeType) params.get("tradeType"),
                    (Boolean) params.get("hideUnavailable"),
                    (SortBy) params.get("sortBy"),
                    (Boolean) params.get("distinct")
            );

            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
