/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.geolocation.maxmind.binary;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
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

/**
 * {@link MaxMindBinaryStorage2}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class MaxMindBinaryStorage implements GeoLocationStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaxMindBinaryStorage.class);
    private static final String SERVICE_PROVIDER_ID = "maxmind-bin";

    private final DatabaseReader databaseReader;

    /**
     * Initialises a new {@link MaxMindBinaryStorage}.
     * 
     * @param databasePath The path to the MaxMind city database to use
     */
    public MaxMindBinaryStorage(String databasePath) throws OXException {
        super();
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
        } catch (GeoIp2Exception e) {
            if (e instanceof AddressNotFoundException) {
                throw GeoLocationExceptionCodes.ADDRESS_NOT_FOUND.create(e, address);
            }
            throw GeoLocationExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

    @Override
    public String getProviderId() {
        return SERVICE_PROVIDER_ID;
    }

    private static DatabaseReader initReader(String databasePath) throws OXException {
        try {
            return new DatabaseReader.Builder(new FileInputStream(databasePath)).withCache(new CHMCache()).build();
        } catch (Exception e) {
            LOGGER.error("Error initializing reader for MaxMind database at {}: {}", databasePath, e.getMessage(), e);
            throw GeoLocationExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
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
