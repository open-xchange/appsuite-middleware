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
 *    trademarks of the OX Software GmbH group of companies.
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
