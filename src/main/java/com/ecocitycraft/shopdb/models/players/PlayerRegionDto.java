package com.ecocitycraft.shopdb.models.players;

import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class PlayerRegionDto {
    private String name;
    private String server;

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
