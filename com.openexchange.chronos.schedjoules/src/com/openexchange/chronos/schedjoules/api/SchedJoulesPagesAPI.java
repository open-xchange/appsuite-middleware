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

import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesAPIDefaultValues;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCategory;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCommonParameter;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage.SchedJoulesPageBuilder;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesSearchParameter;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTBindPoint;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTClient;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRequest;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;
import com.openexchange.rest.client.v2.RESTResponseUtil;

/**
 * {@link SchedJoulesPagesAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesPagesAPI extends AbstractSchedJoulesAPI {

    /**
     * Initialises a new {@link SchedJoulesPagesAPI}.
     */
    SchedJoulesPagesAPI(SchedJoulesRESTClient client) {
        super(client);
    }

    /**
     * Retrieves the user's current location as an entry point.
     * 
     * @return The {@link SchedJoulesResponse}
     * @throws OXException
     */
    public SchedJoulesPage getRootPage() throws OXException {
        return getRootPage(SchedJoulesAPIDefaultValues.DEFAULT_LOCALE, SchedJoulesAPIDefaultValues.DEFAULT_LOCATION);
    }

    /**
     * Retrieves the user's current location as an entry point.
     * 
     * @param locale The user's locale
     * @param location The users' location
     * @return The page as a {@link JSONObject}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesPage getRootPage(String locale, String location) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.pages);
        request.setQueryParameter(SchedJoulesCommonParameter.location.name(), location);
        request.setQueryParameter(SchedJoulesCommonParameter.locale.name(), locale);

        return executeRequest(request);
    }

    /**
     * Retrieves the page with the specified identifier
     * 
     * @param pageId The page identifier
     * @return The page as a {@link JSONObject}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesPage getPage(int pageId) throws OXException {
        return getPage(pageId, SchedJoulesAPIDefaultValues.DEFAULT_LOCALE);
    }

    /**
     * Retrieves the page with the specified identifier
     * 
     * @param pageId The page identifier
     * @param locale The optional user's locale. If absent it defaults to 'en'
     * @return The page as a {@link JSONObject}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesPage getPage(int pageId, String locale) throws OXException {
        return fetchPage(pageId, locale);
    }

    /**
     * Performs a search with the specified parameters. If the country and/or category identifiers are
     * specified, then a search is only performed within those countries/categories respectively.
     * 
     * @param query The query (free text search query parameter)
     * @param locale The locale. Defaults to 'en' if empty
     * @param countryId The country identifier. Ignored if less than or equal to '0'.
     * @param categoryId The category identifier. Ignored if less than or equal to '0'.
     * @param maxRows The maximum amount of results. Defaults to 20.
     * @return A {@link JSONObject} with the results
     * @throws OXException if an error is occurred
     */
    public SchedJoulesPage search(String query, String locale, int countryId, int categoryId, int maxRows) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.pages.getAbsolutePath() + "/search");
        request.setQueryParameter(SchedJoulesSearchParameter.q.name(), query);
        request.setQueryParameter(SchedJoulesSearchParameter.locale.name(), Strings.isEmpty(locale) ? SchedJoulesAPIDefaultValues.DEFAULT_LOCALE : locale);
        if (countryId > 0) {
            request.setQueryParameter(SchedJoulesSearchParameter.country_id.name(), Integer.toString(countryId));
        }
        if (categoryId > 0 && categoryId <= SchedJoulesCategory.values().length) {
            request.setQueryParameter(SchedJoulesSearchParameter.category_id.name(), Integer.toString(SchedJoulesCategory.values()[categoryId - 1].getId()));
        }
        request.setQueryParameter(SchedJoulesSearchParameter.nr_results.name(), Integer.toString(maxRows <= 0 ? SchedJoulesAPIDefaultValues.MAX_ROWS : maxRows));
        return executeRequest(request);
    }

    /**
     * Checks whether a resource was modified
     * 
     * @param pageId The item identifier
     * @param locale The optional locale (if absent, empty or <code>null</code> falls back to default 'en')
     * @param etag The last known etag
     * @param lastModified The last known modified timestamp
     * @return <code>true</code> if it was modified; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    public boolean isModified(int pageId, String locale, String etag, long lastModified) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.pages.getAbsolutePath() + "/" + pageId);
        request.setQueryParameter(SchedJoulesCommonParameter.locale.name(), Strings.isEmpty(locale) ? SchedJoulesAPIDefaultValues.DEFAULT_LOCALE : locale);
        RESTResponse response = client.executeRequest(request, RESTMethod.HEAD, etag, lastModified);
        return response.getStatusCode() != 304;
    }

    //////////////////////////////////////////// HELPERS /////////////////////////////////////////////

    /**
     * Fetches the requested {@link SchedJoulesPage} in the specified translation. First looks up in the cache
     * and if not present it fetches it live from the SchedJoules servers
     * 
     * @param pageId The page identifier of the SchedJoules page to fetch
     * @param locale The locale of the translation
     * @return The {@link SchedJoulesPage}
     * @throws OXException if an error is occurred
     */
    private SchedJoulesPage fetchPage(int pageId, String locale) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.pages.getAbsolutePath() + "/" + pageId);
        request.setQueryParameter(SchedJoulesCommonParameter.locale.name(), Strings.isEmpty(locale) ? SchedJoulesAPIDefaultValues.DEFAULT_LOCALE : locale);

        return executeRequest(request);
    }

    /**
     * Executes the specified request and returns the {@link JSONObject} payload of the response
     * 
     * @param request the {@link SchedJoulesRequest}
     * @return The {@link JSONObject} of the response payload
     * @throws OXException if an error is occurred
     */
    private SchedJoulesPage executeRequest(SchedJoulesRequest request) throws OXException {
        RESTResponse response = client.executeRequest(request);
        return new SchedJoulesPageBuilder().itemData((JSONObject) response.getResponseBody()).etag(response.getHeader(HttpHeaders.ETAG)).lastModified(RESTResponseUtil.getLastModified(response)).build();
    }
}
