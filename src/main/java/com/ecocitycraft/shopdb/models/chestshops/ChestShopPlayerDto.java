package com.ecocitycraft.shopdb.models.chestshops;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class ChestShopPlayerDto {
    private String name;

    public ChestShopPlayerDto() {
    }

    public ChestShopPlayerDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
