package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.exceptions.SDBIllegalArgumentException;
import com.ecocitycraft.shopdb.exceptions.SDBNotFoundException;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.ShopDBMapper;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.regions.RegionDto;
import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;
import com.ecocitycraft.shopdb.utils.Pagination;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@ActivateRequestContext
public class PlayerController {
    Logger LOGGER = LoggerFactory.getLogger(PlayerController.class);

    public PaginatedResponse<PlayerDto> getPlayers(
            Integer page,
             Integer pageSize,
            String name,
            SortBy sortBy
    ) {
        LOGGER.info("GET /players");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        PanacheQuery<Player> players = Player.find(name, sortBy);
        long totalResults = Player.find(name, SortBy.NAME).count();
        List<PlayerDto> results = players.page(page - 1, pageSize).stream().map(ShopDBMapper.INSTANCE::toPlayerDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    public List<PanacheEntityBase> getPlayerNames() {
        return Player.findPlayerNames();
    }

    public PlayerDto getPlayer(String name) {
        LOGGER.info("GET /players/" + name);
        return ShopDBMapper.INSTANCE.toPlayerDto(Player.findByName(name));
    }

    public PaginatedResponse<RegionDto> getPlayerRegions(
            Integer page,
            Integer pageSize,
            String name
    ) {
        LOGGER.info("GET /players/" + name + "/regions");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (name == null || name.isEmpty()) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_PLAYER_NAME);

        Player p = Player.findByName(name);
        if (p == null) throw new SDBNotFoundException(String.format(ExceptionMessage.PLAYER_NOT_FOUND, name));

        List<Region> regions = p.towns;
        int totalResults = regions.size();
        regions = Pagination.getPage(p.towns, page, pageSize);
        List<RegionDto> results = regions.stream().map(ShopDBMapper.INSTANCE::toRegionDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    public PaginatedResponse<ChestShopDto> getPlayerChestShops(
             Integer page,
             Integer pageSize,
            TradeType tradeType,
             String name) {
        LOGGER.info("GET /players/" + name + "/chest-shops");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (name == null || name.isEmpty()) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_PLAYER_NAME);

        Player p = Player.findByName(name);
        if (p == null) throw new SDBNotFoundException(String.format(ExceptionMessage.PLAYER_NOT_FOUND, name));

        PanacheQuery<ChestShop> chestShops = ChestShop.findOwnedBy(p, tradeType);

        long totalResults = chestShops.count();
        List<ChestShopDto> results = chestShops.page(page - 1, pageSize).stream().map(ShopDBMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    public String setPlayerUuid(String name, String uuid) {
        Player p = Player.findByName(name);
        if (p == null) throw new SDBNotFoundException(String.format(ExceptionMessage.PLAYER_NOT_FOUND, name));
        p.uuid = uuid;
        p.persist();
        return "Successfully updated player '" + name + "' with UUID '" + uuid + "'";
    }
}
