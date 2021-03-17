package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.PlayerController;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.regions.RegionDto;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;

@Named("player-regions-get")
public class PlayerRegionsGet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    PlayerController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            HashMap<String, Object> params = ParameterUtil.mapParams(
                    event.getQueryStringParameters(),
                    context.getLogger(),
                    "page", "pageSize", "name"
            );

            PaginatedResponse<RegionDto> result = controller.getPlayerRegions(
                    (Integer) params.get("page"),
                    (Integer) params.get("pageSize"),
                    (String) params.get("name")
            );

            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
