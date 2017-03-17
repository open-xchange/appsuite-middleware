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

package com.openexchange.geolocation.maxmind;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import org.osgi.framework.Bundle;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Postal;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.DefaultGeoInformation;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationExceptionCodes;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.maxmind.osgi.MaxMindGeoLocationServiceActivator;
import com.openexchange.java.Streams;
import com.openexchange.osgi.BundleResourceLoader;

/**
 * {@link MaxMindGeoLocationService} - The MaxMind Geo location service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public class MaxMindGeoLocationService implements GeoLocationService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MaxMindGeoLocationService.class);

    /**
     * Creates a new <code>MaxMindGeoLocationService</code> instance.
     *
     * @param bundle The associated bundle
     * @param configurationService The configuration service to use
     * @return Newly created <code>MaxMindGeoLocationService</code> instance
     * @throws OXException If creation fails
     */
    public static MaxMindGeoLocationService newInstance(Bundle bundle, ConfigurationService configurationService) throws OXException {
        String databasePath = configurationService.getProperty("com.openexchange.geolocation.maxmind.databasePath", "resource://geodb/GeoLite2-City.mmdb");

        InputStream in = null;
        try {
            if (databasePath.indexOf("://") > 0) {
                // Expect URI notation
                URI uri = toURI(databasePath);
                if ("resource".equals(uri.getScheme())) {
                    in = getInputStreamFromResource(uri, bundle);
                } else {
                    in = getInputStreamFromURL(uri);
                }
            } else {
                // Expect a file
                in = getInputStreamFromFile(databasePath);
            }

            // Create DatabaseReader from input stream
            DatabaseReader reader = new DatabaseReader.Builder(in).withCache(new CHMCache()).build();

            // Pass DatabaseReader to a new instance
            return new MaxMindGeoLocationService(reader);
        } catch (IOException e) {
            throw GeoLocationExceptionCodes.IO_ERROR.create(e, "Failed to read " + databasePath);
        } finally {
            Streams.close(in);
        }
    }

    private static InputStream getInputStreamFromFile(String databasePath) throws OXException {
        try {
            return new FileInputStream(databasePath);
        } catch (FileNotFoundException e) {
            throw OXException.general("No such file: " + databasePath, e);
        }
    }

    private static InputStream getInputStreamFromURL(URI uri) throws OXException {
        try {
            URL url = uri.toURL();
            return url.openStream();
        } catch (IllegalArgumentException e) {
            throw OXException.general("URI is not absolute: " + uri, e);
        } catch (MalformedURLException e) {
            throw OXException.general("No such protocol handler for URI: " + uri, e);
        } catch (IOException e) {
            throw GeoLocationExceptionCodes.IO_ERROR.create(e, "Stream could not be obtained from URI: " + uri);
        }
    }

    private static InputStream getInputStreamFromResource(URI uri, Bundle bundle) throws OXException {
        try {
            String path = uri.getPath();
            String resourceName = null != path && path.length() > 0 ? uri.getHost() + path : uri.getHost();

            InputStream in = new BundleResourceLoader(bundle).getResourceAsStream(resourceName);
            if (null == in) {
                // Retry...
                in = MaxMindGeoLocationServiceActivator.class.getClassLoader().getResourceAsStream(resourceName);
                if (null == in) {
                    throw OXException.general("No such resource available: " + resourceName);
                }
            }
            return in;
        } catch (IOException e) {
            throw GeoLocationExceptionCodes.IO_ERROR.create(e, "Stream could not be obtained from resource: " + uri);
        }
    }

    private static URI toURI(String databasePath) throws OXException {
        try {
            return new URI(databasePath);
        } catch (URISyntaxException e) {
            throw OXException.general("Invalid URI: " + databasePath, e);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final DatabaseReader reader;

    /**
     * Initializes a new {@link MaxMindGeoLocationService}.
     */
    private MaxMindGeoLocationService(DatabaseReader reader) {
        super();
        this.reader = reader;
    }

    /**
     * Determines the IP address from the specified ipAddress parameter
     *
     * @param ipAddress The IP address to convert to {@link InetAddress}
     * @return The {@link InetAddress} of the specified IP address
     * @throws OXException If the specified IP address is invalid
     */
    private InetAddress getInetAddress(String ipAddress) throws OXException {
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            throw GeoLocationExceptionCodes.UNABLE_TO_RESOLVE_HOST.create(e, ipAddress);
        }
    }

    /**
     * Retrieves the {@link CityResponse} from the GeoDB for the specified IP address
     *
     * @param ipAddress The IP address
     * @return The {@link CityResponse} that maps to the specified IP address
     * @throws OXException If the IP address is invalid or not found in the GeoDB
     */
    private CityResponse getCityResponse(String ipAddress) throws OXException {
        InetAddress inetAddress = getInetAddress(ipAddress);
        try {
            return reader.city(inetAddress);
        } catch (IOException e) {
            throw GeoLocationExceptionCodes.IO_ERROR.create(e);
        } catch (GeoIp2Exception e) {
            if (e instanceof AddressNotFoundException) {
                throw GeoLocationExceptionCodes.ADDRESS_NOT_FOUND.create(e, ipAddress);
            }
            throw GeoLocationExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

    @Override
    public GeoInformation getGeoInformation(String ipAddress) throws OXException {
        CityResponse cityResponse = getCityResponse(ipAddress);
        return parseCityResponse(cityResponse);
    }

    /**
     * Parses the specified {@link CityResponse} to a {@link GeoInformation} object
     *
     * @param cityResponse The {@link CityResponse} to parse
     * @return The {@link GeoInformation} object
     */
    private GeoInformation parseCityResponse(CityResponse cityResponse) {
        DefaultGeoInformation.Builder gi = DefaultGeoInformation.builder();

        // Get the continent
        Continent continent = cityResponse.getContinent();
        if (continent != null) {
            gi.continent(continent.getName());
        }

        Country country = cityResponse.getCountry();
        if (country != null) {
            String isoName = country.getIsoCode();
            gi.country(isoName);
        }

        // Get the city
        City city = cityResponse.getCity();
        if (city != null) {
            gi.city(city.getName());
        }

        // Get the postal code
        Postal postal = cityResponse.getPostal();
        if (postal != null) {
            try {
                int postalCode = Integer.parseInt(postal.getCode());
                gi.postalCode(postalCode);
            } catch (NumberFormatException e) {
                LOGGER.debug("{}", e.getMessage(), e);
            }
        }

        return gi.build();
    }

    /**
     * Stops this MaxMind Geo,location service.
     */
    public void stop() {
        try {
            reader.close();
        } catch (Exception e) {
            // Ignore
        }
    }
}
