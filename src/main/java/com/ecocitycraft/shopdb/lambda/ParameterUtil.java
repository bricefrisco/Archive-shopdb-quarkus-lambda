package com.ecocitycraft.shopdb.lambda;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;

import java.util.HashMap;
import java.util.Map;

public final class ParameterUtil {
    public static HashMap<String, Object> mapParams(Map<String, String> args, LambdaLogger logger, String... params) {
        if (args == null) args = new HashMap<>();

        HashMap<String, Object> result = new HashMap<>();

        String v;
        for (String param : params) {
            logger.log(param + ": " + args.get(param) + " | ");

            switch(param) {
                case "page":
                    v = args.get(param);
                    if (v == null) v = "1";
                    result.put(param, Integer.parseInt(v));
                    break;
                case "pageSize":
                    v = args.get(param);
                    if (v == null) v = "6";
                    result.put(param, Integer.parseInt(v));
                    break;
                case "server":
                    v = args.get(param);
                    if (v == null) break;
                    result.put(param, Server.fromString(v));
                    break;
                case "tradeType":
                    v = args.get(param);
                    if (v == null) v = "buy";
                    result.put(param, TradeType.fromString(v));
                    break;
                case "hideUnavailable":
                case "distinct":
                case "active":
                    v = args.get(param);
                    if (v == null) v = "false";
                    result.put(param, Boolean.parseBoolean(v));
                    break;
                case "sortBy":
                    v = args.get(param);
                    if (v == null) v = "name";
                    result.put(param, SortBy.fromString(v));
                    break;
                case "name":
                case "material":
                case "uuid":
                    v = args.get(param);
                    if (v == null) v = "";
                    result.put(param, v);
                    break;
            }
        }

        return result;
    }
}
