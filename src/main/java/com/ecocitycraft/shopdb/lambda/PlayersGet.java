package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.PlayerController;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import com.ecocitycraft.shopdb.models.players.PlayerDto;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;

@Named("players-get")
public class PlayersGet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    PlayerController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            HashMap<String, Object> params = ParameterUtil.mapParams(
                    event.getQueryStringParameters(),
                    context.getLogger(),
                    "page", "pageSize", "name", "sortBy"
            );

            PaginatedResponse<PlayerDto> result = controller.getPlayers(
                    (Integer) params.get("page"),
                    (Integer) params.get("pageSize"),
                    (String) params.get("name"),
                    (SortBy) params.get("sortBy")
            );

            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
