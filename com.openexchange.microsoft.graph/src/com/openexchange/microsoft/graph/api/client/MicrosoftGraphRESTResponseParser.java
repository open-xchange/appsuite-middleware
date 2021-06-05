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

package com.openexchange.microsoft.graph.api.client;

import org.apache.http.HttpResponse;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.v2.RESTMimeType;
import com.openexchange.rest.client.v2.parser.AbstractRESTResponseParser;
import com.openexchange.rest.client.v2.parser.ImageRESTResponseBodyParser;

/**
 * {@link MicrosoftGraphRESTResponseParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MicrosoftGraphRESTResponseParser extends AbstractRESTResponseParser {

    private static final String REMOTE_SERVICE = "Microsoft Graph";

    /**
     * Initialises a new {@link MicrosoftGraphRESTResponseParser}.
     */
    public MicrosoftGraphRESTResponseParser() {
        super();
        responseBodyParsers.put(RESTMimeType.IMAGE, new ImageRESTResponseBodyParser());
    }

    @Override
    protected String getRemoteServiceName() {
        return REMOTE_SERVICE;
    }

    @Override
    protected int assertStatusCode(HttpResponse httpResponse) throws OXException {
        // No assertion of the status code, we will check the response body later
        // and throw an exception (if necessary) on a higher API level, i.e.
        // in {@link MicrosoftGraphRESTClient#checkForErrors(org.json.JSONObject)}
        // and {@link MicrosoftGraphRESTClient#assertStatusCode(int)}
        return httpResponse.getStatusLine().getStatusCode();
    }
}
