package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.RegionController;
import com.ecocitycraft.shopdb.models.regions.RegionRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Named("region-post")
public class RegionPost implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    RegionController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            context.getLogger().log("Body: " + event.getBody());
            List<RegionRequest> requests = MAPPER.readValue(Base64.decode(event.getBody().getBytes(StandardCharsets.UTF_8)), new TypeReference<>(){});
            String result = controller.processRegions(requests);
            return ResponseUtil.mapResponse(result, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
