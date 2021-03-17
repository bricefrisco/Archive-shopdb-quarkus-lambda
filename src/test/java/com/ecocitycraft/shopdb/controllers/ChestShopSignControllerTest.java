package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChestShopSignControllerTest {
    private static final String CHEST_SHOP_ID = "abc";
    private static final String CHEST_SHOP_ID_2 = "abcd";

    @Inject
    ChestShopController chestShopController;

    @Test
    @Order(1)
    @Transactional
    public void chest_shops_shown() {
        Player player = new Player();
        player.name = "kozz";

        Region region = new Region();
        region.name = "browntown";
        region.server = Server.MAIN;
        region.iBounds.setX(0);
        region.iBounds.setY(0);
        region.iBounds.setZ(0);
        region.oBounds.setX(200);
        region.oBounds.setY(200);
        region.oBounds.setZ(200);
        region.active = Boolean.TRUE;


        ChestShop chestShop = new ChestShop();
        chestShop.id = CHEST_SHOP_ID;
        chestShop.server = Server.MAIN;
        chestShop.location.setX(1);
        chestShop.location.setY(1);
        chestShop.location.setZ(1);
        chestShop.material = "test";
        chestShop.isHidden = Boolean.FALSE;
        chestShop.buyPrice = 1.0;
        chestShop.buyPriceEach = 1.0;
        chestShop.sellPrice = 1.0;
        chestShop.sellPriceEach = 1.0;
        chestShop.quantity = 0;
        chestShop.isBuySign = Boolean.TRUE;
        chestShop.isSellSign = Boolean.TRUE;
        chestShop.owner = player;
        chestShop.town = region;

        Player.persist(player);
        Region.persist(region);
        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );


        assertEquals(chestShops.getTotalElements(), 1);
        assertEquals(chestShops.getPage(), 1);
        assertEquals(chestShops.getTotalPages(), 1);
        assertEquals(chestShops.getResults().get(0).getMaterial(), "test");
    }

    @Test
    @Order(2)
    @Transactional
    public void hidden_chest_shops_not_shown() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.isHidden = Boolean.TRUE;
        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getResults().size());
        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    @Order(3)
    @Transactional
    public void test_trade_type_buy_doesnt_show_items_purchased() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.isHidden = Boolean.FALSE;
        chestShop.isBuySign = Boolean.FALSE;
        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getResults().size());
        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    @Order(4)
    @Transactional
    public void test_trade_type_sell_doesnt_show_items_sold() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.isBuySign = Boolean.TRUE;
        chestShop.isSellSign = Boolean.FALSE;

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.SELL, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    @Order(5)
    @Transactional
    public void test_server_filter() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.server = Server.MAIN_NORTH;

        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getTotalElements());

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN_NORTH, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(1, chestShops.getTotalElements());
    }

    @Test
    @Order(6)
    @Transactional
    public void test_item_filter() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.server = Server.MAIN;
        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(1, chestShops.getTotalElements());

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test2", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    @Order(8)
    @Transactional
    public void test_distinct_filter() {
        Region region = Region.find(Server.MAIN, "browntown");
        Player player = Player.find("name", "kozz").firstResult();

        ChestShop chestShop1 = ChestShop.findById(CHEST_SHOP_ID);
        chestShop1.id = CHEST_SHOP_ID;
        chestShop1.material = "iron ore";
        chestShop1.server = Server.MAIN;
        chestShop1.location = new Location(11, 11, 11);
        chestShop1.quantity = 100;
        chestShop1.buyPrice = 500.0;
        chestShop1.sellPrice = 500.0;
        chestShop1.buyPriceEach = 10.0;
        chestShop1.sellPriceEach = 10.0;
        chestShop1.town = region;
        chestShop1.owner = player;
        chestShop1.isHidden = Boolean.FALSE;
        chestShop1.isBuySign = Boolean.TRUE;
        chestShop1.isSellSign = Boolean.TRUE;
        chestShop1.quantityAvailable = 25;

        ChestShop chestShop2 = new ChestShop();
        chestShop2.id = CHEST_SHOP_ID_2;
        chestShop2.material = "iron ore";
        chestShop2.server = Server.MAIN;
        chestShop2.location = new Location(10, 10, 10);
        chestShop2.quantity = 100;
        chestShop2.sellPrice = 500.0;
        chestShop2.buyPrice = 500.0;
        chestShop2.sellPriceEach = 10.0;
        chestShop2.buyPriceEach = 10.0;
        chestShop2.owner = player;
        chestShop2.town = region;
        chestShop2.isFull = Boolean.FALSE;
        chestShop2.isHidden = Boolean.FALSE;
        chestShop2.isBuySign = Boolean.TRUE;
        chestShop2.isSellSign = Boolean.TRUE;
        chestShop2.quantityAvailable = 50;

        ChestShop.persist(chestShop1);
        ChestShop.persist(chestShop2);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.FALSE
                );

        assertEquals(2, chestShops.getTotalElements());

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(1, chestShops.getTotalElements());
        ChestShopDto higherQuantity = chestShops.getResults().get(0);
        assertEquals(50, higherQuantity.getQuantityAvailable());

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.SELL, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(1, chestShops.getTotalElements());
        ChestShopDto lowerQuantity = chestShops.getResults().get(0);
        assertEquals(25, lowerQuantity.getQuantityAvailable());
    }

    @Test
    @Order(9)
    @Transactional
    public void test_sort_by_best_price() {
        ChestShop chestShop1 = ChestShop.findById(CHEST_SHOP_ID);
        ChestShop chestShop2 = ChestShop.findById(CHEST_SHOP_ID_2);

        chestShop1.buyPriceEach = 2.00;
        chestShop2.buyPriceEach = 1.00;

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.FALSE
                );

        assertEquals(chestShops.getTotalElements(), 2);
        assertEquals(chestShops.getResults().get(0).getBuyPriceEach(), 1.00);
        assertEquals(chestShops.getResults().get(1).getBuyPriceEach(), 2.00);

        chestShop2.sellPriceEach = 50.00;
        chestShop1.sellPriceEach = 10.00;

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.SELL, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.FALSE
                );

        assertEquals(chestShops.getTotalElements(), 2);
        assertEquals(chestShops.getResults().get(0).getSellPriceEach(), 50.00);
        assertEquals(chestShops.getResults().get(1).getSellPriceEach(), 10.00);
    }

    @Test
    @Order(10)
    @Transactional
    public void test_buy_hide_unavailable_filter() {
        ChestShop chestShop1 = ChestShop.findById(CHEST_SHOP_ID);
        ChestShop chestShop2 = ChestShop.findById(CHEST_SHOP_ID_2);

        chestShop1.quantityAvailable = 0;
        chestShop2.quantityAvailable = 0;

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.FALSE
                );

        assertEquals(chestShops.getTotalElements(), 2);

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.BUY, Boolean.TRUE, SortBy.BEST_PRICE, Boolean.FALSE
                );

        assertEquals(chestShops.getTotalElements(), 0);
    }

    @Test
    @Order(11)
    @Transactional
    public void test_sell_hide_unavailable_filter() {
        ChestShop chestShop1 = ChestShop.findById(CHEST_SHOP_ID);
        ChestShop chestShop2 = ChestShop.findById(CHEST_SHOP_ID_2);

        chestShop1.isFull = Boolean.TRUE;
        chestShop2.isFull = Boolean.TRUE;

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.FALSE
                );

        assertEquals(chestShops.getTotalElements(), 2);

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "iron ore", Server.MAIN, TradeType.BUY, Boolean.TRUE, SortBy.BEST_PRICE, Boolean.FALSE
                );

        assertEquals(chestShops.getTotalElements(), 0);
    }

    @Test
    @Order(12)
    @Transactional
    public void test_get_chest_shop_material_names() {
        ChestShop chestShop1 = ChestShop.findById(CHEST_SHOP_ID);
        ChestShop chestShop2 = ChestShop.findById(CHEST_SHOP_ID_2);
        chestShop1.isBuySign = Boolean.TRUE;
        chestShop2.isBuySign = Boolean.TRUE;

        chestShop1.material = "diamond ore";
        chestShop2.material = "diamond ore";

        List<String> materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN, TradeType.BUY);
        assertEquals(1, materials.size());
        assertEquals("diamond ore", materials.get(0));

        chestShop1.material = "iron ore";
        materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN, TradeType.BUY);
        assertEquals(2, materials.size());
        assertEquals("diamond ore", materials.get(0));
        assertEquals("iron ore", materials.get(1));
    }

    @Test
    @Order(13)
    @Transactional
    public void test_get_chest_shops_material_names_trade_type_filter() {
        ChestShop chestShop1 = ChestShop.findById(CHEST_SHOP_ID);
        ChestShop chestShop2 = ChestShop.findById(CHEST_SHOP_ID_2);
        chestShop1.isBuySign = Boolean.TRUE;
        chestShop2.isBuySign = Boolean.TRUE;
        chestShop1.isSellSign = Boolean.FALSE;
        chestShop2.isSellSign = Boolean.FALSE;

        List<String> materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN, TradeType.BUY);
        assertEquals(2, materials.size());

        materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN, TradeType.SELL);
        assertEquals(0, materials.size());

        chestShop1.isBuySign = Boolean.FALSE;
        chestShop2.isBuySign = Boolean.FALSE;
        chestShop1.isSellSign = Boolean.TRUE;
        chestShop2.isSellSign = Boolean.TRUE;

        materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN, TradeType.SELL);
        assertEquals(2, materials.size());

        materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN, TradeType.BUY);
        assertEquals(0, materials.size());
    }

    @Test
    @Order(14)
    @Transactional
    public void test_get_chest_shops_material_names_server_filter() {
        ChestShop chestShop1 = ChestShop.findById(CHEST_SHOP_ID);
        ChestShop chestShop2 = ChestShop.findById(CHEST_SHOP_ID_2);
        chestShop1.isBuySign = Boolean.TRUE;
        chestShop2.isBuySign = Boolean.TRUE;
        chestShop1.server = Server.MAIN_NORTH;
        chestShop2.server = Server.MAIN_NORTH;

        List<String> materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN, TradeType.BUY);
        assertEquals(0, materials.size());

        materials = chestShopController.getChestShopSignMaterialNames(Server.MAIN_NORTH, TradeType.BUY);
        assertEquals(2, materials.size());
    }


//    @Test
//    public void test_api_get_chest_shop_signs() {
//        given().when().get("/chest-shops").then().statusCode(200);
//    }
//
//    @Test
//    public void test_api_get_chest_shop_signs_invalid_page_sizes() {
//        given().when().get("/chest-shops?page=1&pageSize=10").then().statusCode(200);
//        given().when().get("/chest-shops?page=-1").then().statusCode(400);
//        given().when().get("/chest-shops?pageSize=101").then().statusCode(400);
//        given().when().get("/chest-shops?pageSize=-1").then().statusCode(400);
//    }
//
//    @Test
//    public void test_api_get_chest_shop_signs_invalid_trade_types() {
//        given().when().get("/chest-shops?sortBy=best-price").then().statusCode(200);
//        given().when().get("/chest-shops?server=main").then().statusCode(200);
//        given().when().get("/chest-shops?tradeType=buy").then().statusCode(200);
//        given().when().get("/chest-shops?sortBy=invalid").then().statusCode(400);
//        given().when().get("/chest-shops?server=invalid").then().statusCode(400);
//        given().when().get("/chest-shops?tradeType=invalid").then().statusCode(400);
//    }
}
