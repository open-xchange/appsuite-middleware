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

import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesAPIDefaultValues;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCommonParameter;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage.SchedJoulesPageBuilder;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTBindPoint;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTClient;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRequest;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;
import com.openexchange.rest.client.v2.RESTResponseUtil;

/**
 * {@link SchedJoulesCountriesAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCountriesAPI extends AbstractSchedJoulesAPI {

    /**
     * Initialises a new {@link SchedJoulesCountriesAPI}.
     */
    SchedJoulesCountriesAPI(SchedJoulesRESTClient client) {
        super(client);
    }

    /**
     * Retrieves a list with all available countries
     * 
     * @return A {@link JSONArray} with all available countries
     * @throws OXException if a parsing error is occurred
     */
    public SchedJoulesPage listCountries() throws OXException {
        return listCountries(SchedJoulesAPIDefaultValues.DEFAULT_LOCALE);
    }

    /**
     * Retrieves a list with all available countries
     * 
     * @param locale The locale
     * @return A {@link JSONArray} with all available countries
     * @throws OXException if a parsing error is occurred
     */
    public SchedJoulesPage listCountries(String locale) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.countries);
        request.setQueryParameter(SchedJoulesCommonParameter.locale.name(), locale);

        RESTResponse response = client.executeRequest(request);
        return new SchedJoulesPageBuilder().itemData((JSONArray) response.getResponseBody()).etag(response.getHeader(HttpHeaders.ETAG)).lastModified(RESTResponseUtil.getLastModified(response)).build();
    }

    /**
     * Checks whether a resource was modified
     * 
     * @param locale The optional locale (if absent, empty or <code>null</code> falls back to default 'en')
     * @param etag The last known etag
     * @param lastModified The last known modified timestamp
     * @return <code>true</code> if it was modified; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    public boolean isModified(String locale, String etag, long lastModified) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.pages.getAbsolutePath());
        request.setQueryParameter(SchedJoulesCommonParameter.locale.name(), Strings.isEmpty(locale) ? SchedJoulesAPIDefaultValues.DEFAULT_LOCALE : locale);
        RESTResponse response = client.executeRequest(request, RESTMethod.HEAD, etag, lastModified);
        return response.getStatusCode() != 304;
    }
}
