/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.geolocation.maxmind.binary;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Postal;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.DefaultGeoInformation;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationStorageService;
import com.openexchange.geolocation.exceptions.GeoLocationExceptionCodes;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link MaxMindBinaryStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class MaxMindBinaryStorage implements GeoLocationStorageService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MaxMindBinaryStorage.class);
    }

    private static final String SERVICE_PROVIDER_ID = "maxmind-bin";
    private static final String DATABASE_PATH_PROPERTY = "com.openexchange.geolocation.maxmind.databasePath";

    /**
     * Initializes the database reader
     *
     * @param databasePath The database path
     * @return The new {@link DatabaseReader}
     * @throws OXException if an error is occurred
     */
    private static DatabaseReader initReader(String databasePath) throws OXException {
        try {
            return new DatabaseReader.Builder(new FileInputStream(databasePath)).withCache(new CHMCache()).build();
        } catch (Exception e) {
            LoggerHolder.LOGGER.error("Error initializing reader for MaxMind database at {}: {}", databasePath, e.getMessage(), e);
            throw GeoLocationExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final DatabaseReader databaseReader;

    /**
     * Initializes a new {@link MaxMindBinaryStorage}.
     *
     * @param databasePath The path to the MaxMind city database to use
     * @throws OXException if the database path property is empty or any other error is occurred
     */
    public MaxMindBinaryStorage(String databasePath) throws OXException {
        super();
        if (Strings.isEmpty(databasePath)) {
            throw GeoLocationExceptionCodes.STORAGE_SERVICE_PROVIDER_NOT_CONFIGURED.create(DATABASE_PATH_PROPERTY);
        }
        this.databaseReader = initReader(databasePath);
    }

    /**
     * Safely closes the underlying database reader.
     */
    public void close() {
        Streams.close(databaseReader);
    }

    @Override
    public GeoInformation getGeoInformation(int contextId, InetAddress address) throws OXException {
        try {
            return parseCityResponse(databaseReader.city(address));
        } catch (IOException e) {
            throw GeoLocationExceptionCodes.IO_ERROR.create(e.getMessage(), e);
        } catch (AddressNotFoundException e) {
            throw GeoLocationExceptionCodes.ADDRESS_NOT_FOUND.create(e, address);
        } catch (Exception e) {
            throw GeoLocationExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

    @Override
    public String getProviderId() {
        return SERVICE_PROVIDER_ID;
    }

    /**
     * Parses the specified {@link CityResponse} to a {@link GeoInformation} object
     *
     * @param cityResponse The {@link CityResponse} to parse
     * @return The {@link GeoInformation} object
     */
    private GeoInformation parseCityResponse(CityResponse cityResponse) {
        DefaultGeoInformation.Builder builder = DefaultGeoInformation.builder();

        // Get the continent
        Continent continent = cityResponse.getContinent();
        if (continent != null) {
            builder.continent(continent.getName());
        }

        // Get the country
        Country country = cityResponse.getCountry();
        if (country != null) {
            String isoName = country.getIsoCode();
            builder.country(isoName);
        }

        // Get the city
        City city = cityResponse.getCity();
        if (city != null) {
            builder.city(city.getName());
        }

        // Get the postal code
        Postal postal = cityResponse.getPostal();
        if (postal != null) {
            builder.postalCode(postal.getCode());
        }
        return builder.build();
    }

}
