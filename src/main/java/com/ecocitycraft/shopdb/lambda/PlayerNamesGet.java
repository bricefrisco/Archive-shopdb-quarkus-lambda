package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ecocitycraft.shopdb.controllers.PlayerController;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("player-names-get")
public class PlayerNamesGet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    PlayerController controller;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            List<PanacheEntityBase> playerNames = controller.getPlayerNames();
            return ResponseUtil.mapResponse(playerNames, 200);
        } catch (Exception e) {
            return ResponseUtil.mapResponse(e);
        }
    }
}
