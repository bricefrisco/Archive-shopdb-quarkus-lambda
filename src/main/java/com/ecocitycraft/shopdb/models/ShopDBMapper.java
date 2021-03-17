package com.ecocitycraft.shopdb.models;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopPlayerDto;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopRegionDto;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.players.PlayerRegionDto;
import com.ecocitycraft.shopdb.models.regions.RegionDto;
import com.ecocitycraft.shopdb.models.regions.RegionPlayerDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Locale;

@Mapper
public interface ShopDBMapper {
    ShopDBMapper INSTANCE = Mappers.getMapper(ShopDBMapper.class);

    @Named("serverMapping")
    default String serverMapping(Server server) {
        return Server.toString(server).replace("_", "-").toLowerCase(Locale.ROOT);
    }

    ChestShopRegionDto toChestShopRegionDto(Region region);

    ChestShopPlayerDto toChestShopPlayerDto(Player player);

    @Mapping(source = "server", target="server", qualifiedByName="serverMapping")
    ChestShopDto toChestShopDto(ChestShop chestShop);

    PlayerRegionDto toPlayerRegionDto(Region region);

    PlayerDto toPlayerDto(Player player);

    @AfterMapping
    default void mapNumChestShops(Player player, @MappingTarget PlayerDto playerDto) {
        playerDto.setNumChestShops(player.chestShops.size());
    }

    RegionPlayerDto toRegionPlayerDto(Player player);

    @Mapping(source = "server", target="server", qualifiedByName = "serverMapping")
    RegionDto toRegionDto(Region region);

    @AfterMapping
    default void mapNumChestShops(Region region, @MappingTarget RegionDto regionDto) {
        regionDto.setNumChestShops(region.chestShops.size());
    }
}
