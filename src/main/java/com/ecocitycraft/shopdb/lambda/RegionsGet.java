package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.RegionController;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import com.ecocitycraft.shopdb.models.regions.RegionDto;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;

@Named("regions-get")
public class RegionsGet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    RegionController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            HashMap<String, Object> params = ParameterUtil.mapParams(
                    event.getQueryStringParameters(),
                    context.getLogger(),
                    "page", "pageSize", "server", "active", "name", "sortBy"
            );

            PaginatedResponse<RegionDto> result = controller.getRegions(
                    (Integer) params.get("page"),
                    (Integer) params.get("pageSize"),
                    (Server) params.get("server"),
                    (Boolean) params.get("active"),
                    (String) params.get("name"),
                    (SortBy) params.get("sortBy")
            );

            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
