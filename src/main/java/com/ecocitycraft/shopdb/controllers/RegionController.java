package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.exceptions.SDBIllegalArgumentException;
import com.ecocitycraft.shopdb.exceptions.SDBNotFoundException;
import com.ecocitycraft.shopdb.models.ShopDBMapper;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.*;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.regions.RegionDto;
import com.ecocitycraft.shopdb.models.regions.RegionRequest;
import com.ecocitycraft.shopdb.services.ChestShopBatchProcessor;
import com.ecocitycraft.shopdb.services.RegionBatchProcessor;
import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;
import com.ecocitycraft.shopdb.utils.Pagination;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@ActivateRequestContext
public class RegionController {
    Logger LOGGER = LoggerFactory.getLogger(RegionController.class);

    @Inject
    RegionBatchProcessor regionBatchProcessor;

    @Inject
    ChestShopBatchProcessor chestShopBatchProcessor;

    public PaginatedResponse<RegionDto> getRegions(
            Integer page,
            Integer pageSize,
             Server server,
            Boolean active,
           String name,
           SortBy sortBy
    ) {
        LOGGER.info("GET /regions");
        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        PanacheQuery<Region> regions = Region.find(server, active, name, sortBy);
        long totalResults = Region.find(server, active, name, SortBy.NAME).count();
        List<RegionDto> results = regions.page(page - 1, pageSize).stream().map(ShopDBMapper.INSTANCE::toRegionDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    public List<PanacheEntityBase> getRegionNames(
            Server server,
            Boolean active) {
        LOGGER.info("GET /region-names");
        return Region.findRegionNames(server, active);
    }

    public RegionDto getRegion(
            Server server,
            String name
    ) {
        LOGGER.info("GET /regions/" + server + "/" + name);

        if (name == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_REGION_NAME);
        if (server == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_SERVER_NAME);

        Region region = Region.find(server, name);
        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));

        return ShopDBMapper.INSTANCE.toRegionDto(Region.find(server, name));
    }

    public PaginatedResponse<PlayerDto> getRegionOwners(
           Server server,
           String name,
            Integer page,
           Integer pageSize) {
        LOGGER.info("GET /regions/" + server + "/" + name + "/players");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (name == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_REGION_NAME);
        if (server == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_SERVER_NAME);

        Region region = Region.find(server, name);
        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));

        List<Player> players = Pagination.getPage(region.mayors, page, pageSize);
        int totalResults = players.size();
        List<PlayerDto> results = players.stream().map(ShopDBMapper.INSTANCE::toPlayerDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    public PaginatedResponse<ChestShopDto> getRegionChestShops(
           Server server,
           String name,
            Integer page,
            Integer pageSize,
           TradeType tradeType) {
        LOGGER.info("GET /regions/" + server + "/" + name + "/chest-shops");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        Region region = Region.find(server, name);
        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));

        PanacheQuery<ChestShop> chestShops = ChestShop.findInRegion(region, tradeType);
        long totalResults = chestShops.count();
        List<ChestShopDto> results = chestShops.page(page - 1, pageSize).stream().map(ShopDBMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @Transactional
    public String processRegions(List<RegionRequest> requests) {
        return regionBatchProcessor.processRegions(requests);
    }


    @Transactional
    public String updateRegionActive(
           Server server,
            String name,
           Boolean active
    ) {
        LOGGER.info("PUT /regions/" + server + "/" + name + "/chest-shops?active=" + active);

        Region region = Region.find(server, name);

        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));
        region.active = active;

        if (active) {
            chestShopBatchProcessor.linkAndShowChestShops(region);
        } else {
            chestShopBatchProcessor.linkAndHideChestShops(region);
        }

        region.persistAndFlush();
        return "Successfully updated region '" + region.name + "' on server " + Server.toString(region.server);
    }

    @Transactional
    public String linkActiveRegionChestShops() {
        List<Region> regions = Region.find("active", Boolean.TRUE).list();
        for (Region region : regions) {
            chestShopBatchProcessor.linkAndShowChestShops(region);
        }

        return "Successfully linked active regions.";
    }

}
