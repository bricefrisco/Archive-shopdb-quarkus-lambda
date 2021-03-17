package com.ecocitycraft.shopdb.models.chestshops;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum EventType {
    CREATE,
    UPDATE,
    DELETE
}
