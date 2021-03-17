package com.ecocitycraft.shopdb.models.chestshops;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.persistence.Embeddable;

@Embeddable
@RegisterForReflection
public class Location {

    public Location() {
    }

    public Location(int x,int y,int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    private int x;


    private int y;

    private int z;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
