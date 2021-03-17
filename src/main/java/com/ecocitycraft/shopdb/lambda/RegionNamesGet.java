package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.RegionController;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;

@Named("region-names-get")
public class RegionNamesGet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    RegionController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            HashMap<String, Object> params = ParameterUtil.mapParams(
                    event.getQueryStringParameters(),
                    context.getLogger(),
                    "server", "active"
            );

            List<PanacheEntityBase> result = controller.getRegionNames(
                    (Server) params.get("server"),
                    (Boolean) params.get("active")
            );

            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
