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
import org.json.JSONArray;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage.SchedJoulesPageBuilder;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTBindPoint;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTClient;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRequest;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;
import com.openexchange.rest.client.v2.RESTResponseUtil;

/**
 * {@link SchedJoulesLanguagesAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesLanguagesAPI extends AbstractSchedJoulesAPI {

    /**
     * Initialises a new {@link SchedJoulesLanguagesAPI}.
     */
    SchedJoulesLanguagesAPI(SchedJoulesRESTClient client) {
        super(client);
    }

    /**
     * Retrieves a list with all available languages
     * 
     * @return A {@link JSONArray} with all available languages
     * @throws OXException if a parsing error is occurred
     */
    public SchedJoulesPage listLanguages() throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.languages);

        RESTResponse response = client.executeRequest(request);
        return new SchedJoulesPageBuilder().itemData((JSONArray) response.getResponseBody()).etag(response.getHeader(HttpHeaders.ETAG)).lastModified(RESTResponseUtil.getLastModified(response)).build();
    }

    /**
     * Checks whether a resource was modified
     * 
     * @param etag The last known etag
     * @param lastModified The last known modified timestamp
     * @return <code>true</code> if it was modified; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    public boolean isModified(String etag, long lastModified) throws OXException {
        SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.languages.getAbsolutePath());
        RESTResponse response = client.executeRequest(request, RESTMethod.HEAD, etag, lastModified);
        return response.getStatusCode() != 304;
    }
}
