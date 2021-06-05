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

package com.openexchange.geolocation;

import java.net.InetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link GeoLocationService} - Provides Geographical information for a given IP address
 * or a set of GPS coordinates.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.4
 */
@SingletonService
public interface GeoLocationService {

    /**
     * Retrieves the {@link GeoInformation} of the specified IP address
     * 
     * @param contextId The context identifier
     * @param ipAddress The IP address as string
     *
     * @return The Geographical information for the specified IP address
     * @throws OXException If the specified IP address is invalid or Geographical information cannot be returned
     */
    GeoInformation getGeoInformation(int contextId, InetAddress ipAddress) throws OXException;
}
