package com.ecocitycraft.shopdb.models.chestshops;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class ChestShopRegionDto {
    private String name;

    public ChestShopRegionDto() {
    }

    public ChestShopRegionDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
