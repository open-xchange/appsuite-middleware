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

package com.openexchange.chronos.schedjoules.api;

import java.net.URL;
import java.util.Collections;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCalendar;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTClient;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;

/**
 * {@link SchedJoulesCalendarAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCalendarAPI extends AbstractSchedJoulesAPI {

    /**
     * Initialises a new {@link SchedJoulesCalendarAPI}.
     */
    SchedJoulesCalendarAPI(SchedJoulesRESTClient client) {
        super(client);
    }

    /**
     * Retrieves the iCal from the specified {@link URL}
     *
     * @param url The {@link URL} for the iCal
     * @return The iCal parsed as a {@link SchedJoulesCalendar}
     * @throws OXException if a parsing error is occurred
     */
    public SchedJoulesCalendar getCalendar(URL url) throws OXException {
        RESTResponse response = client.executeRequest(url);
        return (SchedJoulesCalendar) response.getResponseBody();
    }

    /**
     * Retrieves the iCal from the specified {@link URL}
     *
     * @param url The {@link URL} for the iCal
     * @param eTag The last known etag
     * @param lastModified The last modified to use
     * @return The iCal parsed as a {@link SchedJoulesCalendar}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesCalendar getCalendar(URL url, String eTag, long lastModified) throws OXException {
        if (Strings.isNotEmpty(eTag) || 0 < lastModified) {
            RESTResponse response = client.executeRequest(url, RESTMethod.HEAD, eTag, lastModified);
            if (response.getStatusCode() == 304) {
                return new SchedJoulesCalendar(null, Collections.emptyList(), eTag, lastModified); // Nothing modified
            }
        }
        return getCalendar(url);
    }
}
