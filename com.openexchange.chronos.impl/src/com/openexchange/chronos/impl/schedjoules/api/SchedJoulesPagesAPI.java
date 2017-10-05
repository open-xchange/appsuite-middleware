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

package com.openexchange.chronos.impl.schedjoules.api;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link SchedJoulesPagesAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesPagesAPI {

    private final SchedJoulesRESTClient client;

    /**
     * Initialises a new {@link SchedJoulesPagesAPI}.
     */
    public SchedJoulesPagesAPI(SchedJoulesRESTClient client) {
        super();
        this.client = client;
    }

    /**
     * Retrieves the user's current location as an entry point.
     * 
     * @return The {@link SchedJoulesResponse}
     * @throws OXException
     */
    public JSONObject getRootPage() throws OXException {
        return getRootPage("en", "us");
    }

    /**
     * Retrieves the user's current location as an entry point.
     * 
     * @return The page as a {@link JSONObject}
     * @throws OXException if an error is occurred
     */
    public JSONObject getRootPage(String locale, String location) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.pages.name());
        request.setQueryParameter(SchedJoulesCommonParameter.location.name(), location);
        request.setQueryParameter(SchedJoulesCommonParameter.locale.name(), locale);

        return parseInputStream(client.executeRequest(request));
    }

    /**
     * Retrieves the page with the specified identifier
     * 
     * @param pageId The page identifier
     * @return The page as a {@link JSONObject}
     * @throws OXException if an error is occurred
     */
    public JSONObject getPage(int pageId) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.pages.name() + "/" + pageId);
        SchedJoulesResponse response = client.executeRequest(request);

        return parseInputStream(response);
    }

    /**
     * Parses the {@link InputStream} from the specified {@link SchedJoulesResponse}
     * as a {@link JSONObject}
     * 
     * @param response The {@link SchedJoulesResponse}
     * @return The {@link JSONObject} from the response
     * @throws OXException if a JSON parsing error or an I/O error occurs
     */
    private JSONObject parseInputStream(SchedJoulesResponse response) throws OXException {
        try (InputStream inputStream = response.getStream()) {
            String body = IOUtils.toString(inputStream, "UTF-8");
            return new JSONObject(body);
        } catch (IOException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
    }
}
