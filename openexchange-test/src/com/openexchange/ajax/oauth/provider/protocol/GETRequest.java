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

package com.openexchange.ajax.oauth.provider.protocol;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.ajax.oauth.provider.EndpointTest;

public final class GETRequest extends AbstractRequest<GETRequest> {

    public GETRequest() {
        super();
    }

    public GETResponse execute(HttpClient client) throws IOException {
        URIBuilder uriBuilder = new URIBuilder()
            .setScheme(scheme)
            .setHost(hostname)
            .setPort(port)
            .setPath(EndpointTest.AUTHORIZATION_ENDPOINT);

        for (NameValuePair param : prepareParams()) {
            uriBuilder.setParameter(param.getName(), param.getValue());
        }

        try {
            HttpGet request = new HttpGet(uriBuilder.build());
            for (String header : headers.keySet()) {
                String value = headers.get(header);
                if (value == null) {
                    request.removeHeaders(header);
                } else {
                    request.setHeader(header, value);
                }
            }

            HttpResponse loginPageResponse = client.execute(request);
            return new GETResponse(this, loginPageResponse);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
