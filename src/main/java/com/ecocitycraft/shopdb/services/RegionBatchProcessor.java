package com.ecocitycraft.shopdb.services;

import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.regions.Bounds;
import com.ecocitycraft.shopdb.models.regions.RegionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class RegionBatchProcessor {
    final Logger LOGGER = LoggerFactory.getLogger(RegionBatchProcessor.class);
    private final HashMap<String, Server> servers = new HashMap<>();
    private static final Pattern p = Pattern.compile("[a-zA-Z0-9+_]{3,16}");

    public RegionBatchProcessor() {
        this.servers.put("rising", Server.MAIN);
        this.servers.put("rising_n", Server.MAIN_NORTH);
        this.servers.put("rising_e", Server.MAIN_EAST);
    }

    public String processRegions(List<RegionRequest> requests) {
        LOGGER.info("Beginning to process " + requests.size() + " region requests.");

        LOGGER.info("Filtering out invalid region requests...");
        requests = requests.stream().filter(this::regionRequestIsValid).collect(Collectors.toList());

        Set<String> playerNames = new HashSet<>();
        requests.forEach(r -> playerNames.addAll(r.getMayorNames()));
        HashMap<String, Player> players = Player.getOrAddPlayers(playerNames);

        LOGGER.info("Mapping " + requests.size() + " region requests...");

        int numInserts = 0;
        int numUpdates = 0;


        List<Region> upserts = new ArrayList<>();
        for (RegionRequest request : requests) {
            Server server = servers.get(request.getServer());
            Bounds bounds = sort(request.getiBounds(), request.getoBounds());

            Region region = Region.find(server, request.getName());

            if (region == null) {
                region = new Region();
                region.active = Boolean.FALSE;
                region.name = request.getName().toLowerCase(Locale.ROOT);
                region.server = server;
                numInserts++;
            } else {
                numUpdates++;
            }

            region.iBounds = bounds.getLowerBounds();
            region.oBounds = bounds.getUpperBounds();

            if (request.getMayorNames().size() > 0) {
                List<Player> regionMayors = new ArrayList<>();
                for (String mName : request.getMayorNames()) {
                    regionMayors.add(players.get(mName));
                }
                region.mayors = regionMayors;
            } else {
                region.mayors = new ArrayList<>();
            }

            region.lastUpdated = new Timestamp(System.currentTimeMillis());

            upserts.add(region);
        }

        Region.persist(upserts);

        String response = "Successfully updated " + numUpdates + " regions, and inserted " + numInserts + " regions.";
        LOGGER.info(response);
        return response;
    }

    private boolean regionRequestIsValid(RegionRequest regionRequest) {
        if (regionRequest == null) {
            LOGGER.warn("Filtering out null region request.");
            return false;
        }

        if (regionRequest.getName() == null || regionRequest.getName().isEmpty()) {
            LOGGER.warn("Filtering out region request with invalid name: " + regionRequest);
            return false;
        }

        if (regionRequest.getServer() == null) {
            LOGGER.warn("Filtering out region request with null server: " + regionRequest);
            return false;
        }

        Server server = servers.get(regionRequest.getServer());
        if (server == null) {
            LOGGER.warn("Filtering out region request with invalid server: " + regionRequest);
            return false;
        }

        if (regionRequest.getiBounds() == null) {
            LOGGER.warn("Filtering out region request with invalid iBounds: " + regionRequest);
            return false;
        }

        if (regionRequest.getoBounds() == null) {
            LOGGER.warn("Filtering out region request with invalid oBounds: " + regionRequest);
            return false;
        }

        if (regionRequest.getMayorNames() == null) {
            LOGGER.warn("Filtering out region request with null mayors: " + regionRequest);
            return false;
        }

        for (String mayorName : regionRequest.getMayorNames()) {
            if (!p.matcher(mayorName).matches()) {
                LOGGER.warn("Filtering out region request with invalid mayor(s): " + regionRequest);
                return false;
            }
        }

        return true;
    }

    private Bounds sort(Location l1, Location l2) {
        Location lowerBounds = new Location();
        Location upperBounds = new Location();

        if (l1.getX() <= l2.getX()) {
            lowerBounds.setX(l1.getX());
            upperBounds.setX(l2.getX());
        } else {
            lowerBounds.setX(l2.getX());
            upperBounds.setX(l1.getX());
        }

        if (l1.getY() <= l2.getY()) {
            lowerBounds.setY(l1.getY());
            upperBounds.setY(l2.getY());
        } else {
            lowerBounds.setY(l2.getY());
            upperBounds.setY(l1.getY());
        }

        if (l1.getZ() <= l2.getZ()) {
            lowerBounds.setZ(l1.getZ());
            upperBounds.setZ(l2.getZ());
        } else {
            lowerBounds.setZ(l2.getZ());
            upperBounds.setZ(l1.getZ());
        }

        return new Bounds(lowerBounds, upperBounds);
    }
}
