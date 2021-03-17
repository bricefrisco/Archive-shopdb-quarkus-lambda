package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;
import com.ecocitycraft.shopdb.exceptions.SDBIllegalArgumentException;
import com.ecocitycraft.shopdb.models.ShopDBMapper;
import com.ecocitycraft.shopdb.services.BotShopProcessor;
import com.ecocitycraft.shopdb.models.bot.BotShopRequest;
import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.*;
import com.ecocitycraft.shopdb.services.ChestShopBatchProcessor;
import com.ecocitycraft.shopdb.utils.*;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@ActivateRequestContext
public class ChestShopController {
    Logger LOGGER = LoggerFactory.getLogger(ChestShopController.class);

    @Inject
    ChestShopBatchProcessor chestShopBatchProcessor;

    @Inject
    BotShopProcessor botShopProcessor;

    public PaginatedResponse<ChestShopDto> getChestShopSigns(Integer page,Integer pageSize,String material,Server server,
            TradeType tradeType,
            Boolean hideUnavailable,
            SortBy sortBy,
            Boolean distinct) {
        LOGGER.info("GET /chest-shops");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize > 100 || pageSize < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        Sort sort = this.mapSortBy(sortBy, tradeType);
        PanacheQuery<ChestShop> chestShops = ChestShop.find(material, tradeType, server, hideUnavailable, sort);

        if (!distinct) {
            long totalResults = chestShops.count();
            List<ChestShopDto> results = chestShops.page(page - 1, pageSize)
                    .stream().map(ShopDBMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());

            return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, shuffle(results, tradeType, sortBy));
        }

        Set<ChestShop> distinctChestShops = this.findDistinctValues(chestShops, tradeType);
        long totalResults = distinctChestShops.size();
        List<ChestShopDto> results = Pagination.getPage(new LinkedList<>(distinctChestShops), page, pageSize)
                .stream().map(ShopDBMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, shuffle(results, tradeType, sortBy));
    }

    public List<String> getChestShopSignMaterialNames(
             Server server,
            TradeType tradeType) {
        LOGGER.info("GET /chest-shops/material-names");

        return ChestShop.findDistinctMaterialNames(tradeType, server);
    }

    @Transactional
    public String createChestShopSigns(BotShopRequest botShopRequest) {
        return botShopProcessor.processShopSigns(botShopRequest);
    }

    @Transactional
    public String createChestShopSigns(List<ShopEvent> shopEvents) {
        return chestShopBatchProcessor.createChestShopSigns(shopEvents);
    }

    private List<ChestShopDto> shuffle(List<ChestShopDto> dtos, TradeType tradeType, SortBy sortBy) {
        if (sortBy != SortBy.BEST_PRICE) return dtos;
        List<ChestShopDto> results = new ArrayList<>();

        HashMap<Double, List<ChestShopDto>> priceMap = new HashMap<>();

        for (ChestShopDto dto : dtos) {
            Double price = tradeType == TradeType.BUY ? dto.getBuyPriceEach() : dto.getSellPriceEach();

            List<ChestShopDto> samePrices = priceMap.get(price);
            if (samePrices == null) {
                samePrices = new ArrayList<>();
                samePrices.add(dto);
                priceMap.put(price, samePrices);
            } else {
                samePrices.add(dto);
            }
        }

        for (List<ChestShopDto> samePrices : priceMap.values()) {
            Collections.shuffle(samePrices);
            results.addAll(samePrices);
        }

        return results.stream().sorted((a, b) -> {
            if (tradeType == TradeType.BUY) {
                return Double.compare(a.getBuyPriceEach(), b.getBuyPriceEach());
            } else {
                return Double.compare(b.getSellPriceEach(), a.getSellPriceEach());
            }
        }).collect(Collectors.toList());
    }

    private Sort mapSortBy(SortBy sortBy, TradeType tradeType) {
        if (sortBy == SortBy.BEST_PRICE && tradeType == TradeType.BUY) return Sort.by("buyPriceEach").ascending();
        if (sortBy == SortBy.BEST_PRICE && tradeType == TradeType.SELL) return Sort.by("sellPriceEach").descending();
        if (sortBy == SortBy.QUANTITY_AVAILABLE) return Sort.by("quantityAvailable").descending();
        if (sortBy == SortBy.QUANTITY) return Sort.by("quantity").descending();
        return Sort.by("material").ascending();
    }

    private Set<ChestShop> findDistinctValues(PanacheQuery<ChestShop> chestShops, TradeType tradeType) {
        LinkedHashMap<ChestShop, ChestShop> distinctValues = new LinkedHashMap<>();

        chestShops.stream().forEach(cs -> {
            ChestShop cs2 = distinctValues.get(cs);
            if (
                    cs2 == null ||
                            tradeType == TradeType.BUY && cs.quantityAvailable > cs2.quantityAvailable ||
                            tradeType == TradeType.SELL && cs.quantityAvailable < cs2.quantityAvailable
            ) {
                distinctValues.remove(cs2);
                distinctValues.put(cs, cs);
            }
        });

        return distinctValues.keySet();
    }
}
